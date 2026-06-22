package com.indiestation.controller.admin;

import com.indiestation.common.Result;
import com.indiestation.entity.vo.MenuVo;
import com.indiestation.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器 (动态菜单)
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/v3/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取当前用户的菜单列表 (前端动态路由调用)
     * GET /api/v3/system/menus/simple
     */
    @GetMapping("/simple")
    public Result<List<MenuVo>> getMenus() {
        List<MenuVo> menus = menuService.getCurrentUserMenus();
        return Result.success(menus);
    }

    /**
     * 获取所有菜单 (树形，用于菜单管理页面)
     * GET /api/admin/menus
     */
    @GetMapping("/api/admin/menus")
    public Result<List<MenuVo>> getAllMenus() {
        List<MenuVo> menus = menuService.getAllMenus();
        return Result.success(menus);
    }
}
