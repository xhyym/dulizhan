package com.indiestation.controller.portal;

import com.indiestation.common.Result;
import com.indiestation.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 门户端系统配置控制器 (公开接口，无需登录)
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/site-config")
@RequiredArgsConstructor
public class PortalSiteConfigController {

    private final SiteConfigService siteConfigService;

    /**
     * 获取公开配置信息
     */
    @GetMapping
    public Result<Map<String, String>> getPublicConfig() {
        Map<String, String> config = siteConfigService.getAllConfig();
        return Result.success(config);
    }
}
