package com.indiestation.controller.admin;

import com.indiestation.common.Result;
import com.indiestation.entity.dto.AdminLoginDTO;
import com.indiestation.entity.vo.LoginVo;
import com.indiestation.entity.vo.AdminInfoVo;
import com.indiestation.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员认证控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody AdminLoginDTO dto) {
        LoginVo loginVo = authService.login(dto);
        return Result.success(loginVo);
    }

    /**
     * 获取管理员信息
     */
    @GetMapping("/info")
    public Result<AdminInfoVo> getAdminInfo() {
        AdminInfoVo info = authService.getAdminInfo();
        return Result.success(info);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
