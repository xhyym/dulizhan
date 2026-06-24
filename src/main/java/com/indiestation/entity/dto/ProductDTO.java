package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
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

    private Long categoryId;

    private BigDecimal price;

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
