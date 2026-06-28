package com.indiestation.entity.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品DTO (新增/编辑)
 *
 * @author IndieStation
 */
@Data
public class ProductDTO {

    private Long id;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "商品分类不能为空")
    private Long categoryId;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.00", message = "商品价格不能小于0")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "商品折后价不能小于0")
    private BigDecimal discountPrice;

    private String skuCode;

    private String mainImage;

    private String posterImage;

    private String detailImage;

    private Integer status = 1;

    private Integer sort = 0;

    /** 商品副图URL列表 */
    private List<String> images;

    /** SKU列表 */
    private List<SkuDTO> skus;

    @Data
    public static class SkuDTO {
        private Long id;
        private String skuCode;
        private String specName;
        private String specValue;
        private BigDecimal price;
        private Integer stock;
        private Integer status = 1;
    }
}
