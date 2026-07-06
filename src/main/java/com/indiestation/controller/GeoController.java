package com.indiestation.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 地理位置调试控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api")
public class GeoController {

    /**
     * 返回当前请求透传到后端的地理位置 Header，便于线上排查代理配置问题。
     */
    @GetMapping("/geo")
    public Map<String, String> getGeoInfo(HttpServletRequest request) {
        return Map.of(
                "country", getHeaderOrDefault(request, "X-Geo-Country"),
                "region", getHeaderOrDefault(request, "X-Geo-Region"),
                "city", getHeaderOrDefault(request, "X-Geo-City"),
                "latitude", getHeaderOrDefault(request, "X-Geo-Latitude"),
                "longitude", getHeaderOrDefault(request, "X-Geo-Longitude"),
                "postalCode", getHeaderOrDefault(request, "X-Geo-Postal-Code")
        );
    }

    private String getHeaderOrDefault(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return (value != null && !value.isEmpty()) ? value : "unknown";
    }
}
