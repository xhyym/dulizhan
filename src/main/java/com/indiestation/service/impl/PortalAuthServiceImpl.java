package com.indiestation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.User;
import com.indiestation.mapper.UserMapper;
import com.indiestation.service.PortalAuthService;
import com.indiestation.entity.vo.PortalUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 门户端认证服务实现
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class PortalAuthServiceImpl implements PortalAuthService {

    private final UserMapper userMapper;

    @Override
    public PortalUserVO login(String email, String whatsapp) {
        // 按邮箱查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, email));

        if (user == null) {
            // 自动注册
            user = new User();
            user.setEmail(email);
            user.setWhatsapp(whatsapp);
            user.setUsername(email.split("@")[0]);
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 更新登录时间和WhatsApp
            user.setLastLoginTime(LocalDateTime.now());
            if (whatsapp != null && !whatsapp.isEmpty()) {
                user.setWhatsapp(whatsapp);
            }
            userMapper.updateById(user);
        }

        // Sa-Token 登录（使用 portal: 前缀区分管理端）
        StpUtil.login("portal:" + user.getId());

        return toUserVO(user);
    }

    @Override
    public PortalUserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        return toUserVO(user);
    }

    private PortalUserVO toUserVO(User user) {
        PortalUserVO vo = new PortalUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setWhatsapp(user.getWhatsapp());
        vo.setAvatar(user.getAvatar());
        return vo;
    }
}
