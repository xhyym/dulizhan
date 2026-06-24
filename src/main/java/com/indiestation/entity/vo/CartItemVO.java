package com.indiestation.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项详情
 *
 * @author IndieStation
 */
@Data
public class CartItemVO {

    private Long id;
    private Long productId;
    private Long skuId;
    private Integer quantity;

    /** 商品信息 */
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal discountPrice;

    /** SKU信息 */
    private String skuCode;
    private String specName;
    private String specValue;
    private BigDecimal skuPrice;
}
