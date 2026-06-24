package com.indiestation.entity.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车操作参数
 *
 * @author IndieStation
 */
@Data
public class CartItemDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private Long skuId;

    @Min(value = 1, message = "数量不能小于1")
    private Integer quantity = 1;
}
