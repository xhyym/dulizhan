package com.indiestation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.Admin;
import com.indiestation.entity.Menu;
import com.indiestation.entity.RoleMenu;
import com.indiestation.entity.vo.MenuVo;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.AdminMapper;
import com.indiestation.mapper.MenuMapper;
import com.indiestation.mapper.RoleMenuMapper;
import com.indiestation.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现 (动态菜单)
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final AdminMapper adminMapper;

    @Override
    public List<MenuVo> getCurrentUserMenus() {
        // 获取当前登录管理员
        long adminId = StpUtil.getLoginIdAsLong();
        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }

        List<Menu> menus;

        if ("SUPER".equals(admin.getRole())) {
            // 超级管理员获取所有菜单
            menus = menuMapper.selectList(
                    new LambdaQueryWrapper<Menu>()
                            .eq(Menu::getStatus, 1)
                            .ne(Menu::getType, "B") // 排除按钮类型
                            .orderByAsc(Menu::getSort)
            );
        } else {
            // 普通管理员根据角色获取菜单
            String roleCode = "R_" + admin.getRole();
            menus = menuMapper.selectMenusByRoleCodes("'" + roleCode + "'");
        }

        // 转换为 MenuVo 并构建树形结构
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuVo> getAllMenus() {
        List<Menu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<Menu>()
                        .orderByAsc(Menu::getSort)
        );
        return buildMenuTree(menus);
    }

    /**
     * 构建菜单树形结构
     */
    private List<MenuVo> buildMenuTree(List<Menu> menus) {
        // 转换为 MenuVo
        List<MenuVo> voList = menus.stream()
                .map(this::convertToMenuVo)
                .collect(Collectors.toList());

        // 构建树形结构
        Map<Long, MenuVo> voMap = voList.stream()
                .collect(Collectors.toMap(MenuVo::getId, v -> v));

        List<MenuVo> tree = new ArrayList<>();
        for (MenuVo vo : voList) {
            // 查找父节点
            Menu parentMenu = menus.stream()
                    .filter(m -> m.getId().equals(vo.getId()))
                    .findFirst()
                    .orElse(null);

            if (parentMenu != null && parentMenu.getParentId() != null && parentMenu.getParentId() > 0) {
                MenuVo parentVo = voMap.get(parentMenu.getParentId());
                if (parentVo != null) {
                    if (parentVo.getChildren() == null) {
                        parentVo.setChildren(new ArrayList<>());
                    }
                    parentVo.getChildren().add(vo);
                }
            } else {
                tree.add(vo);
            }
        }

        return tree;
    }

    /**
     * Menu 实体转 MenuVo
     */
    private MenuVo convertToMenuVo(Menu menu) {
        MenuVo vo = new MenuVo();
        vo.setId(menu.getId());
        vo.setPath(menu.getPath());
        vo.setName(menu.getName());
        vo.setComponent(menu.getComponent());
        vo.setRedirect(menu.getRedirect());

        // 构建 meta
        MenuVo.Meta meta = new MenuVo.Meta();
        meta.setTitle(menu.getTitle());
        meta.setIcon(menu.getIcon());
        meta.setKeepAlive(menu.getKeepAlive() == 1);
        meta.setIsHide(menu.getIsHide() == 1);
        meta.setIsHideTab(menu.getIsHideTab() == 1);

        // 获取角色信息
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>()
                        .eq(RoleMenu::getMenuId, menu.getId())
        );
        if (!roleMenus.isEmpty()) {
            meta.setRoles(roleMenus.stream()
                    .map(RoleMenu::getRoleCode)
                    .collect(Collectors.toList()));
        }

        vo.setMeta(meta);
        return vo;
    }
}
