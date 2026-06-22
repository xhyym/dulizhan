package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品简介 */
    private String description;

    /** 分类ID */
    private Long categoryId;

    /** 原价 */
    private BigDecimal price;

    /** 折后价 */
    private BigDecimal discountPrice;

    /** 默认SKU编码 */
    private String skuCode;

    /** 主图URL */
    private String mainImage;

    /** 海报图URL */
    private String posterImage;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
