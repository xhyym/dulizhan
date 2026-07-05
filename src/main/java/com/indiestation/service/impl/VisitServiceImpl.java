package com.indiestation.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.Product;
import com.indiestation.entity.User;
import com.indiestation.entity.VisitLog;
import com.indiestation.entity.VisitStats;
import com.indiestation.entity.vo.DashboardVO;
import com.indiestation.entity.vo.GeoLocation;
import com.indiestation.entity.vo.VisitorStatsVO;
import com.indiestation.mapper.InquiryMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.mapper.UserMapper;
import com.indiestation.mapper.VisitLogMapper;
import com.indiestation.mapper.VisitStatsMapper;
import com.indiestation.service.GeoIpService;
import com.indiestation.service.VisitService;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 访客统计服务实现
 *
 * @author IndieStation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    /** 访客日志异步队列容量 */
    private static final int VISIT_QUEUE_CAPACITY = 10_000;

    /** 每批次最大落库数量 */
    private static final int VISIT_BATCH_SIZE = 200;

    /** 每轮定时任务最多处理的批次数 */
    private static final int MAX_BATCHES_PER_FLUSH = 10;

    /** 访客地理位置缓存容量，避免重复查询同一 IP */
    private static final int GEO_CACHE_CAPACITY = 2_048;

    /** Redis Stream Key */
    private static final String VISIT_STREAM_KEY = "portal:visit:stream";

    /** Redis Stream 消费组 */
    private static final String VISIT_STREAM_GROUP = "portal:visit:group";

    /** Redis Stream 消费者名称 */
    private static final String VISIT_STREAM_CONSUMER = "portal-visit-consumer";

    /** Stream 初始化占位字段 */
    private static final String VISIT_STREAM_INIT_FIELD = "__event";

    /** Stream 初始化占位值 */
    private static final String VISIT_STREAM_INIT_VALUE = "init";

    /** 地理位置缓存 Key 前缀 */
    private static final String GEO_CACHE_KEY_PREFIX = "portal:visit:geo:";

    /** 地理位置缓存时长 */
    private static final Duration GEO_CACHE_TTL = Duration.ofDays(30);

    private final VisitLogMapper visitLogMapper;
    private final VisitStatsMapper visitStatsMapper;
    private final ProductMapper productMapper;
    private final InquiryMapper inquiryMapper;
    private final UserMapper userMapper;
    private final GeoIpService geoIpService;
    private final StringRedisTemplate stringRedisTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final BlockingQueue<VisitRecordTask> visitRecordQueue = new LinkedBlockingQueue<>(VISIT_QUEUE_CAPACITY);
    private final AtomicLong droppedVisitCounter = new AtomicLong(0);
    private final AtomicLong redisFallbackCounter = new AtomicLong(0);
    private final AtomicLong redisCacheFallbackCounter = new AtomicLong(0);
    private volatile boolean streamGroupInitialized = false;
    /**
     * 仅在门户实际产生访客流量后才触发 Redis Stream 初始化，避免应用空跑启动时误告警。
     */
    private volatile boolean streamAccessActivated = false;

    /**
     * 这里保留一个轻量 LRU 缓存，减少高频访问时对外部 IP 服务的重复调用。
     */
    private final Map<String, GeoLocation> geoLocationCache = Collections.synchronizedMap(
            new LinkedHashMap<>(GEO_CACHE_CAPACITY, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, GeoLocation> eldest) {
                    return size() > GEO_CACHE_CAPACITY;
                }
            });

    @Override
    public void recordVisit(String ip, String pageUrl, String userAgent, GeoLocation headerGeo) {
        VisitRecordTask recordTask = new VisitRecordTask(
                normalizeIp(ip),
                normalizePageUrl(pageUrl),
                normalizeUserAgent(userAgent),
                LocalDateTime.now(),
                headerGeo
        );

        if (!appendVisitRecordToRedisStream(recordTask)) {
            enqueueVisitRecordToMemoryQueue(recordTask);
        }
    }

    @Override
    public DashboardVO getDashboardStats() {
        DashboardVO vo = new DashboardVO();
        LocalDate today = LocalDate.now();
        String todayStart = today.atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String todayEnd = today.plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 今日 PV/UV
        List<Map<String, Object>> todayStats = visitLogMapper.statsByDate(todayStart, todayEnd);
        if (!todayStats.isEmpty()) {
            Map<String, Object> row = todayStats.get(0);
            vo.setTodayPv(toLong(row.get("pv")));
            vo.setTodayUv(toLong(row.get("uv")));
        }

        // 已上架商品数
        vo.setProductCount(productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));

        // 待处理询盘数
        vo.setPendingInquiryCount(inquiryMapper.selectCount(
                new LambdaQueryWrapper<Inquiry>().eq(Inquiry::getStatus, 0)));

        // 本月新客户数
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        vo.setMonthNewCustomerCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, monthStart)));

        // 本月询盘数
        vo.setMonthInquiryCount(inquiryMapper.selectCount(
                new LambdaQueryWrapper<Inquiry>().ge(Inquiry::getCreateTime, monthStart)));

        // 近30天访客趋势
        String thirtyDaysAgo = today.minusDays(29).format(DATE_FMT);
        String tomorrow = today.plusDays(1).format(DATE_FMT);
        vo.setVisitTrend(visitLogMapper.statsByDate(thirtyDaysAgo, tomorrow));

        // 国家Top10
        vo.setCountryTop10(visitLogMapper.statsByCountry(thirtyDaysAgo, tomorrow, 10));

        // 设备分布
        vo.setDeviceDistribution(visitLogMapper.statsByDevice(thirtyDaysAgo, tomorrow));

        // 最新5条询盘
        vo.setRecentInquiries(getRecentInquiries(5));

        return vo;
    }

    @Override
    public VisitorStatsVO getVisitorStats(LocalDate startDate, LocalDate endDate) {
        VisitorStatsVO vo = new VisitorStatsVO();
        String start = startDate.format(DATE_FMT);
        String end = endDate.plusDays(1).format(DATE_FMT);

        // 总 PV/UV
        List<Map<String, Object>> dailyTrend = visitLogMapper.statsByDate(start, end);
        long totalPv = 0, totalUv = 0;
        for (Map<String, Object> row : dailyTrend) {
            totalPv += toLong(row.get("pv"));
            totalUv += toLong(row.get("uv"));
        }
        vo.setTotalPv(totalPv);
        vo.setTotalUv(totalUv);
        vo.setDailyTrend(dailyTrend);

        // 国家Top10
        vo.setCountryTop10(visitLogMapper.statsByCountry(start, end, 10));

        // 城市Top10
        vo.setCityTop10(visitLogMapper.statsByCity(start, end, 10));

        // 设备分布
        vo.setDeviceDistribution(visitLogMapper.statsByDevice(start, end));

        // 热门页面Top10
        vo.setTopPages(visitLogMapper.statsByPage(start, end, 10));

        // 时段热力图
        vo.setHourlyHeatmap(visitLogMapper.statsByHour(start, end));

        return vo;
    }

    @Override
    public void aggregateDailyStats() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 检查是否已聚合
        Long count = visitStatsMapper.selectCount(
                new LambdaQueryWrapper<VisitStats>().eq(VisitStats::getStatDate, yesterday));
        if (count > 0) {
            log.info("日期 {} 已聚合，跳过", yesterday);
            return;
        }

        List<Map<String, Object>> rows = visitLogMapper.aggregateByDate(yesterday);
        for (Map<String, Object> row : rows) {
            VisitStats stats = new VisitStats();
            stats.setStatDate(yesterday);
            stats.setCountry((String) row.getOrDefault("country", ""));
            stats.setCity((String) row.getOrDefault("city", ""));
            stats.setPageUrl((String) row.getOrDefault("pageUrl", ""));
            stats.setPv(toInt(row.get("pv")));
            stats.setUv(toInt(row.get("uv")));
            stats.setMobilePv(toInt(row.get("mobilePv")));
            stats.setPcPv(toInt(row.get("pcPv")));
            stats.setTabletPv(toInt(row.get("tabletPv")));
            visitStatsMapper.insert(stats);
        }

        log.info("日期 {} 聚合完成，共 {} 条记录", yesterday, rows.size());
    }

    /**
     * 定时批量消费访客日志队列，避免在请求线程里直接落库。
     */
    @Scheduled(fixedDelay = 1000)
    public void flushVisitRecordQueue() {
        flushRedisStreamRecords();
        flushMemoryQueueRecords();
    }

    /**
     * Redis Stream 可用时优先消费 Redis 中的访问日志。
     */
    private void flushRedisStreamRecords() {
        if (!streamAccessActivated && !streamGroupInitialized) {
            return;
        }

        if (!ensureRedisStreamGroup()) {
            return;
        }

        for (int batchIndex = 0; batchIndex < MAX_BATCHES_PER_FLUSH; batchIndex++) {
            RedisVisitBatch pendingBatch = readRedisVisitBatch(ReadOffset.from("0"));
            if (!pendingBatch.recordIds().isEmpty()) {
                if (!persistRedisVisitBatch(pendingBatch)) {
                    return;
                }
                continue;
            }

            RedisVisitBatch newBatch = readRedisVisitBatch(ReadOffset.lastConsumed());
            if (newBatch.recordIds().isEmpty()) {
                return;
            }

            if (!persistRedisVisitBatch(newBatch)) {
                return;
            }

            if (newBatch.recordIds().size() < VISIT_BATCH_SIZE) {
                return;
            }
        }
    }

    /**
     * Redis 不可用时，再消费兜底的内存队列。
     */
    private void flushMemoryQueueRecords() {
        for (int batchIndex = 0; batchIndex < MAX_BATCHES_PER_FLUSH; batchIndex++) {
            List<VisitRecordTask> batchTasks = pollBatch();
            if (batchTasks.isEmpty()) {
                return;
            }
            if (!persistVisitBatch(batchTasks)) {
                return;
            }
            if (batchTasks.size() < VISIT_BATCH_SIZE) {
                return;
            }
        }
    }

    /**
     * 服务关闭前尽量清空剩余访客日志，减少开发重启时的数据丢失。
     */
    @PreDestroy
    public void flushRemainingVisitRecords() {
        int totalFlushed = 0;
        while (true) {
            List<VisitRecordTask> batchTasks = pollBatch();
            if (batchTasks.isEmpty()) {
                break;
            }
            totalFlushed += batchTasks.size();
            persistVisitBatch(batchTasks);
        }

        if (totalFlushed > 0) {
            log.info("应用关闭前已补刷剩余访客日志: flushedCount={}", totalFlushed);
        }
    }

    /**
     * 获取最新询盘列表
     */
    private List<Map<String, Object>> getRecentInquiries(int limit) {
        List<Inquiry> inquiries = inquiryMapper.selectList(
                new LambdaQueryWrapper<Inquiry>()
                        .orderByDesc(Inquiry::getCreateTime)
                        .last("LIMIT " + limit));

        return inquiries.stream().map(inquiry -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", inquiry.getId());
            map.put("inquiryNo", inquiry.getInquiryNo());
            map.put("userName", inquiry.getUserName());
            map.put("userEmail", inquiry.getUserEmail());
            map.put("totalAmount", inquiry.getTotalAmount());
            map.put("status", inquiry.getStatus());
            map.put("createTime", inquiry.getCreateTime());
            return map;
        }).toList();
    }

    /**
     * 解析设备类型
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "PC";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        }
        if (ua.contains("tablet") || ua.contains("ipad")) {
            return "Tablet";
        }
        return "PC";
    }

    /**
     * 从队列中提取一批待处理记录。
     */
    private List<VisitRecordTask> pollBatch() {
        List<VisitRecordTask> batchTasks = new ArrayList<>(VISIT_BATCH_SIZE);
        visitRecordQueue.drainTo(batchTasks, VISIT_BATCH_SIZE);
        return batchTasks;
    }

    /**
     * 将一批访客记录补齐信息后批量写入数据库。
     */
    private boolean persistVisitBatch(List<VisitRecordTask> batchTasks) {
        long startTime = System.nanoTime();
        Map<String, GeoLocation> batchGeoCache = new HashMap<>();
        List<VisitLog> visitLogs = new ArrayList<>(batchTasks.size());

        for (VisitRecordTask recordTask : batchTasks) {
            // Cloudflare Header 已解析到地理位置时直接使用，否则降级到 IP 查询
            GeoLocation geoLocation = recordTask.headerGeo() != null
                    ? recordTask.headerGeo()
                    : resolveGeoLocation(recordTask.ip(), batchGeoCache);
            VisitLog visitLog = new VisitLog();
            visitLog.setIp(recordTask.ip());
            visitLog.setCountry(geoLocation.getCountry());
            visitLog.setProvince(geoLocation.getProvince());
            visitLog.setCity(geoLocation.getCity());
            visitLog.setPageUrl(recordTask.pageUrl());
            visitLog.setUserAgent(recordTask.userAgent());
            visitLog.setDeviceType(parseDeviceType(recordTask.userAgent()));
            visitLog.setVisitTime(recordTask.visitTime());
            visitLogs.add(visitLog);
        }

        if (visitLogs.isEmpty()) {
            return true;
        }

        try {
            visitLogMapper.batchInsert(visitLogs);
            long costMs = Duration.ofNanos(System.nanoTime() - startTime).toMillis();
            if (costMs > 1000) {
                log.warn("访客日志批量落库耗时较长: batchSize={}, costMs={}, queueSize={}",
                        visitLogs.size(), costMs, visitRecordQueue.size());
            }
            return true;
        } catch (Exception e) {
            log.error("访客日志批量落库失败: batchSize={}, queueSize={}",
                    visitLogs.size(), visitRecordQueue.size(), e);
            return false;
        }
    }

    /**
     * 解析地理位置时优先命中批次缓存和全局缓存，降低外部服务调用次数。
     */
    private GeoLocation resolveGeoLocation(String ip, Map<String, GeoLocation> batchGeoCache) {
        if (ip == null || ip.isBlank()) {
            return GeoLocation.unknown();
        }

        GeoLocation cachedFromBatch = batchGeoCache.get(ip);
        if (cachedFromBatch != null) {
            return cachedFromBatch;
        }

        GeoLocation cachedFromGlobal;
        synchronized (geoLocationCache) {
            cachedFromGlobal = geoLocationCache.get(ip);
        }
        if (cachedFromGlobal != null) {
            batchGeoCache.put(ip, cachedFromGlobal);
            return cachedFromGlobal;
        }

        GeoLocation cachedFromRedis = getGeoLocationFromRedis(ip);
        if (cachedFromRedis != null) {
            batchGeoCache.put(ip, cachedFromRedis);
            synchronized (geoLocationCache) {
                geoLocationCache.put(ip, cachedFromRedis);
            }
            return cachedFromRedis;
        }

        GeoLocation resolvedLocation;
        try {
            resolvedLocation = geoIpService.resolve(ip);
        } catch (Exception e) {
            log.warn("解析访客地理位置失败，已使用默认值: ip={}", ip, e);
            resolvedLocation = GeoLocation.unknown();
        }

        batchGeoCache.put(ip, resolvedLocation);
        synchronized (geoLocationCache) {
            geoLocationCache.put(ip, resolvedLocation);
        }
        cacheGeoLocationToRedis(ip, resolvedLocation);
        return resolvedLocation;
    }

    /**
     * 优先写入 Redis Stream，Redis 异常时返回 false 让上层走内存兜底。
     */
    private boolean appendVisitRecordToRedisStream(VisitRecordTask recordTask) {
        streamAccessActivated = true;

        if (!ensureRedisStreamGroup()) {
            return false;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("ip", recordTask.ip());
        payload.put("pageUrl", recordTask.pageUrl());
        payload.put("userAgent", recordTask.userAgent());
        payload.put("visitTime", recordTask.visitTime().toString());
        if (recordTask.headerGeo() != null) {
            payload.put("geoCountry", recordTask.headerGeo().getCountry());
            payload.put("geoProvince", recordTask.headerGeo().getProvince());
            payload.put("geoCity", recordTask.headerGeo().getCity());
        }

        try {
            stringRedisTemplate.opsForStream().add(
                    StreamRecords.string(payload).withStreamKey(VISIT_STREAM_KEY)
            );
            return true;
        } catch (Exception e) {
            streamGroupInitialized = false;
            logRedisFallback("Redis Stream 写入失败，已切换到内存队列兜底", e);
            return false;
        }
    }

    /**
     * 将访问记录写入内存队列，作为 Redis 不可用时的兜底方案。
     */
    private void enqueueVisitRecordToMemoryQueue(VisitRecordTask recordTask) {
        if (!visitRecordQueue.offer(recordTask)) {
            long droppedCount = droppedVisitCounter.incrementAndGet();
            if (droppedCount <= 5 || droppedCount % 100 == 0) {
                log.warn("访客日志内存队列已满，已丢弃访问记录: droppedCount={}, ip={}, pageUrl={}",
                        droppedCount, recordTask.ip(), recordTask.pageUrl());
            }
        }
    }

    /**
     * 读取一批 Redis Stream 消息。
     */
    private RedisVisitBatch readRedisVisitBatch(ReadOffset readOffset) {
        try {
            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                    Consumer.from(VISIT_STREAM_GROUP, VISIT_STREAM_CONSUMER),
                    StreamReadOptions.empty().count(VISIT_BATCH_SIZE),
                    StreamOffset.create(VISIT_STREAM_KEY, readOffset)
            );

            if (records == null || records.isEmpty()) {
                return RedisVisitBatch.empty();
            }

            List<VisitRecordTask> tasks = new ArrayList<>(records.size());
            List<RecordId> recordIds = new ArrayList<>(records.size());
            for (MapRecord<String, Object, Object> record : records) {
                recordIds.add(record.getId());
                VisitRecordTask visitRecordTask = toVisitRecordTask(record);
                if (visitRecordTask != null) {
                    tasks.add(visitRecordTask);
                }
            }
            return new RedisVisitBatch(tasks, recordIds);
        } catch (Exception e) {
            streamGroupInitialized = false;
            logRedisFallback("Redis Stream 读取失败，本轮将由内存队列继续处理", e);
            return RedisVisitBatch.empty();
        }
    }

    /**
     * Redis 批次落库成功后，确认并删除对应的 Stream 消息，避免 Stream 无限增长。
     */
    private boolean persistRedisVisitBatch(RedisVisitBatch redisVisitBatch) {
        if (!redisVisitBatch.tasks().isEmpty()) {
            if (!persistVisitBatch(redisVisitBatch.tasks())) {
                return false;
            }
        }

        try {
            RecordId[] recordIds = redisVisitBatch.recordIds().toArray(RecordId[]::new);
            if (recordIds.length > 0) {
                stringRedisTemplate.opsForStream().acknowledge(VISIT_STREAM_KEY, VISIT_STREAM_GROUP, recordIds);
                stringRedisTemplate.opsForStream().delete(VISIT_STREAM_KEY, recordIds);
            }
            return true;
        } catch (Exception e) {
            streamGroupInitialized = false;
            log.error("确认 Redis Stream 访客消息失败，消息将保留待后续重试: batchSize={}",
                    redisVisitBatch.recordIds().size(), e);
            return false;
        }
    }

    /**
     * Redis 中存在有效缓存时直接返回，避免重复访问外部地理位置接口。
     */
    private GeoLocation getGeoLocationFromRedis(String ip) {
        try {
            String cacheValue = stringRedisTemplate.opsForValue().get(GEO_CACHE_KEY_PREFIX + ip);
            if (!StringUtils.hasText(cacheValue)) {
                return null;
            }
            return JSONUtil.toBean(cacheValue, GeoLocation.class);
        } catch (Exception e) {
            logRedisCacheFallback("读取访客地理位置缓存失败，已回退到本地缓存/外部解析", e);
            return null;
        }
    }

    /**
     * 将地理位置写入 Redis，后续同一 IP 直接命中缓存。
     */
    private void cacheGeoLocationToRedis(String ip, GeoLocation geoLocation) {
        try {
            stringRedisTemplate.opsForValue().set(
                    GEO_CACHE_KEY_PREFIX + ip,
                    JSONUtil.toJsonStr(geoLocation),
                    GEO_CACHE_TTL
            );
        } catch (Exception e) {
            logRedisCacheFallback("写入访客地理位置缓存失败，已仅保留本地缓存", e);
        }
    }

    /**
     * 初始化 Redis Stream 消费组。Redis 异常时不抛出，交给内存队列兜底。
     */
    private boolean ensureRedisStreamGroup() {
        if (streamGroupInitialized) {
            return true;
        }

        try {
            if (hasVisitStreamConsumerGroup()) {
                streamGroupInitialized = true;
                return true;
            }

            if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(VISIT_STREAM_KEY))) {
                Map<String, String> initPayload = new HashMap<>();
                initPayload.put(VISIT_STREAM_INIT_FIELD, VISIT_STREAM_INIT_VALUE);
                initPayload.put("visitTime", LocalDateTime.now().toString());
                stringRedisTemplate.opsForStream().add(
                        StreamRecords.string(initPayload).withStreamKey(VISIT_STREAM_KEY)
                );
            }

            try {
                stringRedisTemplate.opsForStream().createGroup(
                        VISIT_STREAM_KEY,
                        ReadOffset.latest(),
                        VISIT_STREAM_GROUP
                );
            } catch (Exception e) {
                if (!isBusyGroupException(e) && !hasVisitStreamConsumerGroup()) {
                    throw e;
                }
            }

            streamGroupInitialized = true;
            return true;
        } catch (Exception e) {
            streamGroupInitialized = false;
            logRedisFallback("初始化 Redis Stream 消费组失败，已切换到内存队列兜底", e);
            return false;
        }
    }

    /**
     * 查询消费组是否已存在，避免将 Redis 的正常幂等场景误判成异常。
     */
    private boolean hasVisitStreamConsumerGroup() {
        try {
            StreamInfo.XInfoGroups groups = stringRedisTemplate.opsForStream().groups(VISIT_STREAM_KEY);
            if (groups == null || groups.isEmpty()) {
                return false;
            }
            return groups.stream().anyMatch(group -> VISIT_STREAM_GROUP.equals(group.groupName()));
        } catch (Exception e) {
            if (isNoSuchStreamException(e)) {
                return false;
            }
            throw e;
        }
    }

    /**
     * 将 Redis Stream 消息转换成内部访问记录对象。
     */
    private VisitRecordTask toVisitRecordTask(MapRecord<String, Object, Object> record) {
        Map<Object, Object> payload = record.getValue();
        if (VISIT_STREAM_INIT_VALUE.equals(stringValue(payload.get(VISIT_STREAM_INIT_FIELD)))) {
            return null;
        }

        String visitTimeValue = stringValue(payload.get("visitTime"));
        LocalDateTime visitTime;
        try {
            visitTime = StringUtils.hasText(visitTimeValue)
                    ? LocalDateTime.parse(visitTimeValue)
                    : LocalDateTime.now();
        } catch (Exception e) {
            log.warn("解析 Redis Stream 访客时间失败，已使用当前时间兜底: recordId={}, visitTime={}",
                    record.getId().getValue(), visitTimeValue, e);
            visitTime = LocalDateTime.now();
        }

        GeoLocation headerGeo = null;
        String geoCountry = stringValue(payload.get("geoCountry"));
        String geoCity = stringValue(payload.get("geoCity"));
        if (StringUtils.hasText(geoCountry) || StringUtils.hasText(geoCity)) {
            headerGeo = new GeoLocation();
            headerGeo.setCountry(geoCountry != null ? geoCountry : "");
            headerGeo.setProvince(stringValue(payload.get("geoProvince")) != null
                    ? stringValue(payload.get("geoProvince")) : "");
            headerGeo.setCity(geoCity != null ? geoCity : "");
        }

        return new VisitRecordTask(
                normalizeIp(stringValue(payload.get("ip"))),
                normalizePageUrl(stringValue(payload.get("pageUrl"))),
                normalizeUserAgent(stringValue(payload.get("userAgent"))),
                visitTime,
                headerGeo
        );
    }

    /**
     * 判断是否为消费组已存在的正常异常。
     */
    private boolean isBusyGroupException(Exception e) {
        return containsRedisErrorKeyword(e, "BUSYGROUP");
    }

    /**
     * 判断当前异常是否仅表示 Stream 尚未创建。
     */
    private boolean isNoSuchStreamException(Exception e) {
        return containsRedisErrorKeyword(e, "NOGROUP")
                || containsRedisErrorKeyword(e, "no such key")
                || containsRedisErrorKeyword(e, "ERR no such key");
    }

    /**
     * 递归检查 Redis 异常链，兼容 Spring 对底层异常的包装。
     */
    private boolean containsRedisErrorKeyword(Throwable throwable, String keyword) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(keyword)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Redis Stream 不可用时做限频日志，避免刷满日志文件。
     */
    private void logRedisFallback(String message, Exception e) {
        long fallbackCount = redisFallbackCounter.incrementAndGet();
        if (fallbackCount <= 5 || fallbackCount % 100 == 0) {
            log.warn("{}: fallbackCount={}", message, fallbackCount, e);
        }
    }

    /**
     * Redis 缓存不稳定时做限频日志，避免影响主链路观察。
     */
    private void logRedisCacheFallback(String message, Exception e) {
        long fallbackCount = redisCacheFallbackCounter.incrementAndGet();
        if (fallbackCount <= 5 || fallbackCount % 100 == 0) {
            log.warn("{}: fallbackCount={}", message, fallbackCount, e);
        }
    }

    /**
     * 统一清洗请求中的 IP，避免空值影响统计。
     */
    private String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return "unknown";
        }
        return ip.trim();
    }

    /**
     * 统一清洗访问页面地址。
     */
    private String normalizePageUrl(String pageUrl) {
        if (pageUrl == null || pageUrl.isBlank()) {
            return "/";
        }
        return pageUrl.trim();
    }

    /**
     * 统一清洗浏览器 UA。
     */
    private String normalizeUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "";
        }
        return userAgent.trim();
    }

    /**
     * 将 Redis Stream 字段值安全转换成字符串。
     */
    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private long toLong(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }

    /**
     * 访客记录入队载荷，只保留落库必需的轻量字段。
     */
    private record VisitRecordTask(String ip, String pageUrl, String userAgent, LocalDateTime visitTime,
                                   GeoLocation headerGeo) {
    }

    /**
     * Redis Stream 消费批次。
     */
    private record RedisVisitBatch(List<VisitRecordTask> tasks, List<RecordId> recordIds) {

        private static RedisVisitBatch empty() {
            return new RedisVisitBatch(List.of(), List.of());
        }
    }
}
