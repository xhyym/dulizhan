package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.SiteConfig;
import com.indiestation.mapper.SiteConfigMapper;
import com.indiestation.service.SiteConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现
 *
 * @author IndieStation
 */
@Service
public class SiteConfigServiceImpl extends ServiceImpl<SiteConfigMapper, SiteConfig> implements SiteConfigService {

    @Override
    public Map<String, String> getAllConfig() {
        List<SiteConfig> configs = list();
        Map<String, String> map = new HashMap<>();
        for (SiteConfig config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
    }

    @Override
    public Map<String, String> getConfigMap() {
        return getAllConfig();
    }

    @Override
    public void updateConfig(Map<String, String> configMap) {
        for (Map.Entry<String, String> entry : configMap.entrySet()) {
            SiteConfig existing = getOne(
                    new LambdaQueryWrapper<SiteConfig>()
                            .eq(SiteConfig::getConfigKey, entry.getKey())
            );

            if (existing != null) {
                existing.setConfigValue(entry.getValue());
                updateById(existing);
            } else {
                SiteConfig config = new SiteConfig();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                save(config);
            }
        }
    }
}
