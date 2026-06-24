package com.indiestation.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访客统计服务实现
 *
 * @author IndieStation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitServiceImpl implements VisitService {

    private final VisitLogMapper visitLogMapper;
    private final VisitStatsMapper visitStatsMapper;
    private final ProductMapper productMapper;
    private final InquiryMapper inquiryMapper;
    private final UserMapper userMapper;
    private final GeoIpService geoIpService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Async
    public void recordVisit(String ip, String pageUrl, String userAgent) {
        try {
            // 解析地理位置
            GeoLocation geo = geoIpService.resolve(ip);

            // 解析设备类型
            String deviceType = parseDeviceType(userAgent);

            VisitLog log = new VisitLog();
            log.setIp(ip);
            log.setCountry(geo.getCountry());
            log.setProvince(geo.getProvince());
            log.setCity(geo.getCity());
            log.setPageUrl(pageUrl);
            log.setUserAgent(userAgent);
            log.setDeviceType(deviceType);
            visitLogMapper.insert(log);
        } catch (Exception e) {
            log.error("记录访问日志失败: ip={}, pageUrl={}", ip, pageUrl, e);
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
}
