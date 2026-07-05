package com.indiestation.service;

import com.indiestation.entity.vo.DashboardVO;
import com.indiestation.entity.vo.GeoLocation;
import com.indiestation.entity.vo.VisitorStatsVO;

import java.time.LocalDate;

/**
 * 访客统计服务
 *
 * @author IndieStation
 */
public interface VisitService {

    /**
     * 记录一次访问
     *
     * @param ip        访客IP
     * @param pageUrl   访问页面
     * @param userAgent 浏览器UA
     * @param headerGeo 从 Cloudflare Header 解析的地理位置（可为 null，null 时降级 IP 查询）
     */
    void recordVisit(String ip, String pageUrl, String userAgent, GeoLocation headerGeo);

    /**
     * 获取仪表盘统计数据
     */
    DashboardVO getDashboardStats();

    /**
     * 获取访客统计详情
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     */
    VisitorStatsVO getVisitorStats(LocalDate startDate, LocalDate endDate);

    /**
     * 执行每日聚合任务（由定时任务调用）
     */
    void aggregateDailyStats();
}
