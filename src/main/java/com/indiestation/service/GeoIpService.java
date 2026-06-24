package com.indiestation.service;

import com.indiestation.entity.vo.GeoLocation;

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
}
