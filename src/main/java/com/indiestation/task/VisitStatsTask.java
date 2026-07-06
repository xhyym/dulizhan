package com.indiestation.task;

import com.indiestation.service.VisitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 访客统计定时任务
 *
 * @author IndieStation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VisitStatsTask {

    private final VisitService visitService;

    /**
     * 每天凌晨 2:00 执行昨日数据聚合
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "${app.business-time-zone:Australia/Sydney}")
    public void aggregateDaily() {
        log.info("开始执行访客数据每日聚合任务...");
        try {
            visitService.aggregateDailyStats();
            log.info("访客数据每日聚合任务完成");
        } catch (Exception e) {
            log.error("访客数据每日聚合任务失败", e);
        }
    }
}
