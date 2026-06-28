package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.SiteConfig;
import com.indiestation.mapper.SiteConfigMapper;
import com.indiestation.service.SiteConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 系统配置服务实现
 *
 * @author IndieStation
 */
@Service
public class SiteConfigServiceImpl extends ServiceImpl<SiteConfigMapper, SiteConfig> implements SiteConfigService {

    /**
     * 门户允许公开读取的配置白名单。
     */
    private static final Set<String> PUBLIC_CONFIG_KEYS = new LinkedHashSet<>() {{
        add("site_title");
        add("site_logo");
        add("contact_email");
        add("contact_whatsapp");
        add("hero_tagline");
        add("hero_title");
        add("hero_subtitle");
        add("banner_images");
        add("footer_info");
        add("social_links");
        add("seo_config");
        add("analytics_config");
        add("about_us");
    }};

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
    public Map<String, String> getPublicConfig() {
        List<SiteConfig> configs = list(
                new LambdaQueryWrapper<SiteConfig>()
                        .in(SiteConfig::getConfigKey, PUBLIC_CONFIG_KEYS)
        );
        Map<String, String> map = new HashMap<>();
        for (SiteConfig config : configs) {
            map.put(config.getConfigKey(), config.getConfigValue());
        }
        return map;
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
