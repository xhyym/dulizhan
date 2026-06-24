package com.indiestation.entity.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计数据
 *
 * @author IndieStation
 */
@Data
public class DashboardVO {

    /** 今日PV */
    private long todayPv;

    /** 今日UV */
    private long todayUv;

    /** 已上架商品数 */
    private long productCount;

    /** 待处理询盘数 */
    private long pendingInquiryCount;

    /** 本月新客户数 */
    private long monthNewCustomerCount;

    /** 本月询盘数 */
    private long monthInquiryCount;

    /** 近30天访客趋势 [{date, pv, uv}] */
    private List<Map<String, Object>> visitTrend;

    /** 国家Top10 [{country, pv, uv}] */
    private List<Map<String, Object>> countryTop10;

    /** 设备分布 [{deviceType, pv, uv}] */
    private List<Map<String, Object>> deviceDistribution;

    /** 最新5条询盘 */
    private List<Map<String, Object>> recentInquiries;
}
