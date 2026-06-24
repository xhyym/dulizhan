package com.indiestation.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.indiestation.common.Result;
import com.indiestation.entity.dto.PortalLoginDTO;
import com.indiestation.entity.vo.PortalUserVO;
import com.indiestation.service.PortalAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 门户端认证控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/auth")
@RequiredArgsConstructor
public class PortalAuthController {

    private final PortalAuthService portalAuthService;

    /**
     * WhatsApp + 邮箱登录（不存在则自动注册）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody PortalLoginDTO dto) {
        PortalUserVO user = portalAuthService.login(dto.getEmail(), dto.getWhatsapp());

        Map<String, Object> data = new HashMap<>();
        data.put("token", StpUtil.getTokenValue());
        data.put("user", user);
        return Result.success(data);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<PortalUserVO> me() {
        String loginId = (String) StpUtil.getLoginId();
        Long userId = Long.parseLong(loginId.replace("portal:", ""));
        PortalUserVO user = portalAuthService.getCurrentUser(userId);
        return Result.success(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
