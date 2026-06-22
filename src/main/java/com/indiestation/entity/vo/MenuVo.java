package com.indiestation.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单视图对象 (适配前端 art-design-pro 动态菜单)
 *
 * 前端期望的数据结构:
 * {
 *   "path": "/product",
 *   "name": "Product",
 *   "component": "/index/index",
 *   "meta": { "title": "menus.product.title", "icon": "ri:shopping-bag-line", "roles": [...] },
 *   "children": [...]
 * }
 *
 * @author IndieStation
 */
@Data
public class MenuVo {

    /** 菜单ID */
    private Long id;

    /** 路由路径 */
    private String path;

    /** 路由名称 */
    private String name;

    /** 组件路径 */
    private String component;

    /** 重定向 */
    private String redirect;

    /** 路由元信息 */
    private Meta meta;

    /** 子菜单 */
    private List<MenuVo> children;

    /**
     * 路由元信息
     */
    @Data
    public static class Meta {
        /** 菜单标题 (国际化key) */
        private String title;
        /** 图标 */
        private String icon;
        /** 可访问角色 */
        private List<String> roles;
        /** 是否缓存 */
        private Boolean keepAlive;
        /** 是否隐藏 */
        private Boolean isHide;
        /** 是否隐藏标签 */
        private Boolean isHideTab;
        /** 固定标签 */
        private Boolean fixedTab;
        /** 激活路径 */
        private String activePath;
        /** 全屏 */
        private Boolean isFullPage;
        /** iframe */
        private Boolean isIframe;
        /** 外链 */
        private String link;
        /** 按钮权限列表 */
        private List<AuthItem> authList;
        /** 角标 */
        private Boolean showBadge;
        /** 文本角标 */
        private String showTextBadge;
    }

    /**
     * 按钮权限项
     */
    @Data
    public static class AuthItem {
        private String title;
        private String authMark;
    }
}
