package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 商品副图实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_product_image")
public class ProductImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 图片URL */
    private String imageUrl;

    /** 排序 */
    private Integer sort;
}
