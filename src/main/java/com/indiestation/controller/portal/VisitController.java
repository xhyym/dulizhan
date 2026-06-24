package com.indiestation.controller.portal;

import com.indiestation.common.Result;
import com.indiestation.service.VisitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 访客采集控制器（门户端公开接口）
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/visit")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    /**
     * 记录访问（门户端页面加载时调用）
     */
    @PostMapping
    public Result<Void> recordVisit(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String pageUrl = body.getOrDefault("pageUrl", "/");
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        visitService.recordVisit(ip, pageUrl, userAgent);
        return Result.success();
    }

    /**
     * 获取客户端真实IP（考虑反向代理）
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}
