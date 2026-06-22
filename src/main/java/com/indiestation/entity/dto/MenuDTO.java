package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 菜单DTO (新增/编辑)
 *
 * @author IndieStation
 */
@Data
public class MenuDTO {

    private Long id;

    private Long parentId;

    @NotBlank(message = "路由路径不能为空")
    private String path;

    @NotBlank(message = "路由名称不能为空")
    private String name;

    private String component;

    @NotBlank(message = "菜单标题不能为空")
    private String title;

    private String icon;

    private String redirect;

    private Integer isHide = 0;

    private Integer isHideTab = 0;

    private Integer keepAlive = 1;

    private Integer sort = 0;

    private Integer status = 1;

    /** 菜单类型: M-目录, C-菜单, B-按钮 */
    private String type;

    private String permission;

    /** 可访问角色编码列表 */
    private List<String> roles;
}
