package com.indiestation.controller.portal;

import com.indiestation.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门户端认证控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/auth")
@RequiredArgsConstructor
public class PortalAuthController {

    /**
     * 用户注册 (自动注册，通过邮箱+WhatsApp+用户名)
     * TODO: 实现用户注册逻辑
     */
    @PostMapping("/register")
    public Result<Void> register() {
        // TODO: 实现
        return Result.error("功能开发中");
    }

    /**
     * 用户登录 (邮箱验证码登录)
     * TODO: 实现邮箱验证码登录
     */
    @PostMapping("/login")
    public Result<Void> login() {
        // TODO: 实现
        return Result.error("功能开发中");
    }
}
