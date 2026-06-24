package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访客访问日志实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_visit_log")
public class VisitLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 访客IP */
    private String ip;

    /** 国家 */
    private String country;

    /** 省/州 */
    private String province;

    /** 城市 */
    private String city;

    /** 访问页面路径 */
    private String pageUrl;

    /** 浏览器UA */
    private String userAgent;

    /** 设备类型: PC/Mobile/Tablet */
    private String deviceType;

    /** 访问时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime visitTime;
}
