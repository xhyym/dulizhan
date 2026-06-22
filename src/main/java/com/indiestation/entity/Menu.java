package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 菜单实体 (支持动态菜单)
 *
 * @author IndieStation
 */
@Data
@TableName("t_menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单ID (0为顶级) */
    private Long parentId;

    /** 路由路径 */
    private String path;

    /** 路由名称 (唯一标识) */
    private String name;

    /** 组件路径 (相对views, 如 /product/category) */
    private String component;

    /** 菜单标题 (国际化key, 如 menus.product.title) */
    private String title;

    /** 图标 (如 ri:shopping-bag-line) */
    private String icon;

    /** 重定向路径 */
    private String redirect;

    /** 是否隐藏: 0-显示, 1-隐藏 */
    private Integer isHide;

    /** 是否隐藏标签: 0-显示, 1-隐藏 */
    private Integer isHideTab;

    /** 是否缓存: 0-不缓存, 1-缓存 */
    private Integer keepAlive;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    /** 菜单类型: M-目录, C-菜单, B-按钮 */
    private String type;

    /** 权限标识 (如 user:add, product:edit) */
    private String permission;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
