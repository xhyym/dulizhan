package com.indiestation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.indiestation.entity.SiteConfig;

import java.util.Map;

/**
 * 系统配置服务
 *
 * @author IndieStation
 */
public interface SiteConfigService extends IService<SiteConfig> {

    /**
     * 获取所有配置 (Map格式)
     */
    Map<String, String> getAllConfig();

    /**
     * 获取所有配置 (Map格式，别名)
     */
    Map<String, String> getConfigMap();

    /**
     * 批量更新配置
     */
    void updateConfig(Map<String, String> configMap);
}
