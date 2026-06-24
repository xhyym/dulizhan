package com.indiestation.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.indiestation.entity.vo.GeoLocation;
import com.indiestation.service.GeoIpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * IP 地理位置解析服务实现
 * 使用 ip-api.com 免费接口，后续可替换为 MaxMind 本地库
 *
 * @author IndieStation
 */
@Slf4j
@Service
public class GeoIpServiceImpl implements GeoIpService {

    private static final String IP_API_URL = "http://ip-api.com/json/%s?fields=status,country,regionName,city";

    /** 内网 IP 前缀 */
    private static final String[] PRIVATE_IP_PREFIXES = {
            "10.", "127.", "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
            "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
            "192.168.", "0.", "169.254."
    };

    @Override
    public GeoLocation resolve(String ip) {
        if (ip == null || ip.isEmpty() || isPrivateIp(ip)) {
            return GeoLocation.unknown();
        }

        try {
            String url = String.format(IP_API_URL, ip);
            String response = HttpUtil.get(url, (int) TimeUnit.SECONDS.toMillis(3));

            JSONObject json = JSONUtil.parseObj(response);
            if ("success".equals(json.getStr("status"))) {
                GeoLocation location = new GeoLocation();
                location.setCountry(json.getStr("country", ""));
                location.setProvince(json.getStr("regionName", ""));
                location.setCity(json.getStr("city", ""));
                return location;
            }
        } catch (Exception e) {
            log.warn("IP 地理位置解析失败: {}", ip, e);
        }

        return GeoLocation.unknown();
    }

    private boolean isPrivateIp(String ip) {
        for (String prefix : PRIVATE_IP_PREFIXES) {
            if (ip.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
