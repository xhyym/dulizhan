package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商品分类DTO
 *
 * @author IndieStation
 */
@Data
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 分类图片URL */
    private String image;

    private Long parentId = 0L;

    private Integer sort = 0;

    private Integer status = 1;
}
