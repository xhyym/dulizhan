package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * 访客统计聚合实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_visit_stats")
public class VisitStats {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 统计日期 */
    private LocalDate statDate;

    /** 国家 */
    private String country;

    /** 城市 */
    private String city;

    /** 页面路径 */
    private String pageUrl;

    /** 页面访问量 */
    private Integer pv;

    /** 独立访客数 */
    private Integer uv;

    /** 移动端PV */
    private Integer mobilePv;

    /** PC端PV */
    private Integer pcPv;

    /** 平板端PV */
    private Integer tabletPv;
}
