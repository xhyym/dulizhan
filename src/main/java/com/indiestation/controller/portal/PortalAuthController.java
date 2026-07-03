package com.indiestation.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.indiestation.common.Result;
import com.indiestation.entity.dto.PortalLoginDTO;
import com.indiestation.entity.dto.PortalSendCodeDTO;
import com.indiestation.entity.dto.PortalUpdateWhatsappDTO;
import com.indiestation.entity.dto.PortalUpdateUsernameDTO;
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
     * 发送邮箱验证码
     */
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody PortalSendCodeDTO dto) {
        portalAuthService.sendLoginCode(dto.getEmail());
        return Result.success("Verification code sent successfully. Please check your email.", null);
    }

    /**
     * 邮箱验证码登录（不存在则自动注册）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody PortalLoginDTO dto) {
        PortalUserVO user = portalAuthService.login(dto.getEmail(), dto.getCode());

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
        Long userId = getCurrentPortalUserId();
        PortalUserVO user = portalAuthService.getCurrentUser(userId);
        return Result.success(user);
    }

    /**
     * 更新当前登录用户的 WhatsApp 信息
     */
    @PutMapping("/me/whatsapp")
    public Result<PortalUserVO> updateWhatsapp(@Valid @RequestBody PortalUpdateWhatsappDTO dto) {
        PortalUserVO user = portalAuthService.updateWhatsapp(getCurrentPortalUserId(), dto.getWhatsapp());
        return Result.success("WhatsApp updated successfully.", user);
    }

    /**
     * 更新当前登录用户的用户名
     */
    @PutMapping("/me/username")
    public Result<PortalUserVO> updateUsername(@Valid @RequestBody PortalUpdateUsernameDTO dto) {
        PortalUserVO user = portalAuthService.updateUsername(getCurrentPortalUserId(), dto.getUsername());
        return Result.success("Username updated successfully.", user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    private Long getCurrentPortalUserId() {
        String loginId = (String) StpUtil.getLoginId();
        return Long.parseLong(loginId.replace("portal:", ""));
    }
}
