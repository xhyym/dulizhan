package com.indiestation.entity.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 访客统计详情数据
 *
 * @author IndieStation
 */
@Data
public class VisitorStatsVO {

    /** 总PV */
    private long totalPv;

    /** 总UV */
    private long totalUv;

    /** 每日趋势 [{date, pv, uv}] */
    private List<Map<String, Object>> dailyTrend;

    /** 国家Top10 [{country, pv, uv}] */
    private List<Map<String, Object>> countryTop10;

    /** 城市Top10 [{country, city, pv, uv}] */
    private List<Map<String, Object>> cityTop10;

    /** 设备分布 [{deviceType, pv, uv}] */
    private List<Map<String, Object>> deviceDistribution;

    /** 热门页面Top10 [{pageUrl, pv, uv}] */
    private List<Map<String, Object>> topPages;

    /** 时段分布热力图数据 [{hour, dayOfWeek, pv}] */
    private List<Map<String, Object>> hourlyHeatmap;
}
