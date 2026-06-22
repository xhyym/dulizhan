package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品SKU实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_product_sku")
public class ProductSku {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** SKU编码 */
    private String skuCode;

    /** 规格名称 (如: 颜色, 尺寸) */
    private String specName;

    /** 规格值 (如: 红色, XL) */
    private String specValue;

    /** SKU价格 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;
}
