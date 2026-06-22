package com.indiestation.service;

import com.indiestation.entity.vo.MenuVo;

import java.util.List;

/**
 * 菜单服务 (动态菜单)
 *
 * @author IndieStation
 */
public interface MenuService {

    /**
     * 获取当前用户的菜单列表 (适配前端动态路由)
     */
    List<MenuVo> getCurrentUserMenus();

    /**
     * 获取所有菜单列表 (树形)
     */
    List<MenuVo> getAllMenus();
}
