package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.VisitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 访客日志 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface VisitLogMapper extends BaseMapper<VisitLog> {

    /**
     * 按日期统计 PV/UV
     */
    @Select("SELECT DATE(visit_time) AS date, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
            "FROM t_visit_log WHERE visit_time >= #{startDate} AND visit_time < #{endDate} " +
            "GROUP BY DATE(visit_time) ORDER BY date")
    List<Map<String, Object>> statsByDate(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 按国家统计 PV/UV（Top N）
     */
    @Select("SELECT country, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
            "FROM t_visit_log WHERE visit_time >= #{startDate} AND visit_time < #{endDate} " +
            "AND country IS NOT NULL AND country != '' " +
            "GROUP BY country ORDER BY uv DESC LIMIT #{limit}")
    List<Map<String, Object>> statsByCountry(@Param("startDate") String startDate,
                                              @Param("endDate") String endDate,
                                              @Param("limit") int limit);

    /**
     * 按城市统计 PV/UV（Top N）
     */
    @Select("SELECT country, city, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
            "FROM t_visit_log WHERE visit_time >= #{startDate} AND visit_time < #{endDate} " +
            "AND city IS NOT NULL AND city != '' " +
            "GROUP BY country, city ORDER BY uv DESC LIMIT #{limit}")
    List<Map<String, Object>> statsByCity(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("limit") int limit);

    /**
     * 按设备类型统计
     */
    @Select("SELECT device_type AS deviceType, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
            "FROM t_visit_log WHERE visit_time >= #{startDate} AND visit_time < #{endDate} " +
            "GROUP BY device_type")
    List<Map<String, Object>> statsByDevice(@Param("startDate") String startDate,
                                             @Param("endDate") String endDate);

    /**
     * 按页面统计（Top N）
     */
    @Select("SELECT page_url AS pageUrl, COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv " +
            "FROM t_visit_log WHERE visit_time >= #{startDate} AND visit_time < #{endDate} " +
            "GROUP BY page_url ORDER BY pv DESC LIMIT #{limit}")
    List<Map<String, Object>> statsByPage(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate,
                                           @Param("limit") int limit);

    /**
     * 按小时统计（时段分布）
     */
    @Select("SELECT HOUR(visit_time) AS hour, DAYOFWEEK(visit_time) AS dayOfWeek, COUNT(*) AS pv " +
            "FROM t_visit_log WHERE visit_time >= #{startDate} AND visit_time < #{endDate} " +
            "GROUP BY HOUR(visit_time), DAYOFWEEK(visit_time)")
    List<Map<String, Object>> statsByHour(@Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

    /**
     * 统计指定日期的 PV/UV（用于聚合写入）
     */
    @Select("SELECT country, city, page_url AS pageUrl, " +
            "COUNT(*) AS pv, COUNT(DISTINCT ip) AS uv, " +
            "SUM(CASE WHEN device_type = 'Mobile' THEN 1 ELSE 0 END) AS mobilePv, " +
            "SUM(CASE WHEN device_type = 'PC' THEN 1 ELSE 0 END) AS pcPv, " +
            "SUM(CASE WHEN device_type = 'Tablet' THEN 1 ELSE 0 END) AS tabletPv " +
            "FROM t_visit_log WHERE DATE(visit_time) = #{date} " +
            "GROUP BY country, city, page_url")
    List<Map<String, Object>> aggregateByDate(@Param("date") LocalDate date);
}
