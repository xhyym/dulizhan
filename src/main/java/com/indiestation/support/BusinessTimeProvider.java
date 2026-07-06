package com.indiestation.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 业务时间提供器
 *
 * 统一管理项目中的业务时区，避免定时任务、日期统计、编号生成等逻辑
 * 因服务器部署时区不同而出现跨天边界不一致的问题。
 *
 * @author IndieStation
 */
@Component
public class BusinessTimeProvider {

    /**
     * 默认业务时区，当前按澳洲悉尼时间处理。
     */
    public static final String DEFAULT_TIME_ZONE = "Australia/Sydney";

    private final ZoneId zoneId;

    public BusinessTimeProvider(
            @Value("${app.business-time-zone:" + DEFAULT_TIME_ZONE + "}") String businessTimeZone) {
        this.zoneId = ZoneId.of(businessTimeZone);
    }

    /**
     * 获取当前业务时区。
     */
    public ZoneId getZoneId() {
        return zoneId;
    }

    /**
     * 获取当前业务时间的本地日期时间。
     */
    public LocalDateTime now() {
        return LocalDateTime.now(zoneId);
    }

    /**
     * 获取当前业务日期。
     */
    public LocalDate today() {
        return LocalDate.now(zoneId);
    }
}
