package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_cart")
public class Cart {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** SKU ID */
    private Long skuId;

    /** 数量 */
    private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
