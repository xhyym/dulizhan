package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 询盘商品明细实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_inquiry_item")
public class InquiryItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 询盘ID */
    private Long inquiryId;

    /** 商品ID */
    private Long productId;

    /** 商品名称 (冗余) */
    private String productName;

    /** 商品图片 (冗余) */
    private String productImage;

    /** SKU ID */
    private Long skuId;

    /** SKU规格 (冗余) */
    private String skuSpec;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer quantity;
}
