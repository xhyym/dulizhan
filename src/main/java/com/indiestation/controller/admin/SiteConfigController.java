package com.indiestation.controller.admin;

import com.indiestation.common.Result;
import com.indiestation.service.EmailService;
import com.indiestation.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/site-config")
@RequiredArgsConstructor
public class SiteConfigController {

    private final SiteConfigService siteConfigService;
    private final EmailService emailService;

    /**
     * 获取所有配置
     */
    @GetMapping
    public Result<Map<String, String>> getAll() {
        Map<String, String> config = siteConfigService.getAllConfig();
        return Result.success(config);
    }

    /**
     * 批量更新配置
     */
    @PutMapping
    public Result<Void> update(@RequestBody Map<String, String> configMap) {
        siteConfigService.updateConfig(configMap);
        return Result.success();
    }

    /**
     * 发送测试邮件
     */
    @PostMapping("/test-email")
    public Result<Void> sendTestEmail(@RequestBody Map<String, String> params) {
        String email = params.get("email");
        if (email == null || email.isEmpty()) {
            return Result.error("请输入邮箱地址");
        }
        emailService.sendTestEmail(email);
        return Result.success();
    }
}
