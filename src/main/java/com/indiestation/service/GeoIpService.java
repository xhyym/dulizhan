package com.indiestation.service;

import com.indiestation.entity.vo.GeoLocation;
import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 地理位置解析服务
 *
 * @author IndieStation
 */
public interface GeoIpService {

    /**
     * 根据 IP 解析地理位置
     *
     * @param ip IP 地址
     * @return 地理位置信息
     */
    GeoLocation resolve(String ip);

    /**
     * 优先从 Cloudflare Header 中解析地理位置（零延迟）
     *
     * @param request HTTP 请求
     * @return 地理位置信息，Header 缺失时返回 null
     */
    GeoLocation resolveFromHeaders(HttpServletRequest request);
}
