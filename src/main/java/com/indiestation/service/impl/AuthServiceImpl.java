package com.indiestation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.Admin;
import com.indiestation.entity.dto.AdminLoginDTO;
import com.indiestation.entity.vo.AdminInfoVo;
import com.indiestation.entity.vo.LoginVo;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.AdminMapper;
import com.indiestation.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 管理员认证服务实现
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminMapper adminMapper;

    @Override
    public LoginVo login(AdminLoginDTO dto) {
        // 查询管理员
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, dto.getUserName())
                        .eq(Admin::getStatus, 1)
        );

        if (admin == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码
        if (!BCrypt.checkpw(dto.getPassword(), admin.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // Sa-Token 登录，将角色信息存入 Session
        StpUtil.login(admin.getId());
        StpUtil.getSession().set("admin", admin);

        // 生成 Token
        String token = StpUtil.getTokenValue();

        LoginVo vo = new LoginVo();
        vo.setToken(token);
        vo.setRefreshToken(token); // Sa-Token 暂用同一token
        return vo;
    }

    @Override
    public AdminInfoVo getAdminInfo() {
        // 获取当前登录管理员ID
        long adminId = StpUtil.getLoginIdAsLong();

        Admin admin = adminMapper.selectById(adminId);
        if (admin == null) {
            throw new BusinessException("管理员不存在");
        }

        AdminInfoVo vo = new AdminInfoVo();
        vo.setUserId(admin.getId());
        vo.setUserName(admin.getUsername());
        vo.setNickname(admin.getNickname());
        vo.setAvatar(admin.getAvatar());

        // 角色列表
        String role = admin.getRole();
        List<String> roles = Collections.singletonList("R_" + role);
        vo.setRoles(roles);

        // 按钮权限 (根据角色分配)
        List<String> buttons = getButtonsByRole(role);
        vo.setButtons(buttons);

        return vo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    /**
     * 根据角色获取按钮权限
     */
    private List<String> getButtonsByRole(String role) {
        if ("SUPER".equals(role)) {
            // 超级管理员拥有所有权限
            return List.of(
                    "user:add", "user:edit", "user:delete",
                    "product:add", "product:edit", "product:delete",
                    "category:add", "category:edit", "category:delete",
                    "inquiry:view", "inquiry:edit", "inquiry:export",
                    "siteConfig:edit",
                    "menu:add", "menu:edit", "menu:delete"
            );
        } else if ("ADMIN".equals(role)) {
            // 普通管理员
            return List.of(
                    "product:add", "product:edit",
                    "category:add", "category:edit",
                    "inquiry:view", "inquiry:edit", "inquiry:export"
            );
        }
        return Collections.emptyList();
    }
}
