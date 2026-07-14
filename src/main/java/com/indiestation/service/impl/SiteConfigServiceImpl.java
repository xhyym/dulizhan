package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiestation.entity.SiteConfig;
import com.indiestation.mapper.SiteConfigMapper;
import com.indiestation.service.R2Service;
import com.indiestation.service.SiteConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
@Slf4j
@RequiredArgsConstructor
public class SiteConfigServiceImpl extends ServiceImpl<SiteConfigMapper, SiteConfig> implements SiteConfigService {

    private static final Set<String> DIRECT_IMAGE_CONFIG_KEYS = Set.of(
            "site_logo",
            "site_favicon",
            "products_banner_image",
            "about_banner_image",
            "contact_banner_image"
    );

    private static final Set<String> ABOUT_US_IMAGE_FIELDS = Set.of(
            "bannerImage",
            "storyImage",
            "craftImage"
    );

    private final R2Service r2Service;
    private final ObjectMapper objectMapper;

    /**
     * 门户允许公开读取的配置白名单。
     */
    private static final Set<String> PUBLIC_CONFIG_KEYS = new LinkedHashSet<>() {{
        add("site_title");
        add("site_logo");
        add("site_favicon");
        add("contact_email");
        add("contact_whatsapp");
        add("hero_tagline");
        add("hero_title");
        add("hero_subtitle");
        add("banner_images");
        add("featured_category_ids");
        add("contact_banner_image");
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
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(Map<String, String> configMap) {
        Map<String, String> previousConfigMap = getAllConfig();
        Set<String> previousImageUrls = collectManagedImageUrls(previousConfigMap);
        Map<String, String> mergedConfigMap = new HashMap<>(previousConfigMap);
        mergedConfigMap.putAll(configMap);

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

        Set<String> currentImageUrls = collectManagedImageUrls(mergedConfigMap);
        deleteRemovedImages(previousImageUrls, currentImageUrls);
    }

    /**
     * 收集系统配置中受管图片地址，便于在保存后统一清理旧图。
     */
    private Set<String> collectManagedImageUrls(Map<String, String> configMap) {
        Set<String> imageUrls = new LinkedHashSet<>();
        if (configMap == null || configMap.isEmpty()) {
            return imageUrls;
        }

        for (String configKey : DIRECT_IMAGE_CONFIG_KEYS) {
            addImageUrl(imageUrls, configMap.get(configKey));
        }

        collectBannerImages(imageUrls, configMap.get("banner_images"));
        collectAboutUsImages(imageUrls, configMap.get("about_us"));
        return imageUrls;
    }

    private void collectBannerImages(Set<String> imageUrls, String bannerImagesJson) {
        if (!StringUtils.hasText(bannerImagesJson)) {
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(bannerImagesJson);
            if (!rootNode.isArray()) {
                return;
            }

            for (JsonNode itemNode : rootNode) {
                if (itemNode.isTextual()) {
                    addImageUrl(imageUrls, itemNode.asText());
                    continue;
                }

                if (itemNode.isObject()) {
                    addImageUrl(imageUrls, itemNode.path("image").asText(null));
                }
            }
        } catch (Exception exception) {
            log.warn("解析 banner_images 配置中的图片地址失败", exception);
        }
    }

    private void collectAboutUsImages(Set<String> imageUrls, String aboutUsJson) {
        if (!StringUtils.hasText(aboutUsJson)) {
            return;
        }

        try {
            JsonNode rootNode = objectMapper.readTree(aboutUsJson);
            if (!rootNode.isObject()) {
                return;
            }

            for (String fieldName : ABOUT_US_IMAGE_FIELDS) {
                addImageUrl(imageUrls, rootNode.path(fieldName).asText(null));
            }
        } catch (Exception exception) {
            log.warn("解析 about_us 配置中的图片地址失败", exception);
        }
    }

    private void addImageUrl(Set<String> imageUrls, String rawImageUrl) {
        if (!StringUtils.hasText(rawImageUrl)) {
            return;
        }

        imageUrls.add(rawImageUrl.trim());
    }

    private void deleteRemovedImages(Set<String> previousImageUrls, Set<String> currentImageUrls) {
        if (previousImageUrls == null || previousImageUrls.isEmpty()) {
            return;
        }

        Set<String> removedImageUrls = new LinkedHashSet<>(previousImageUrls);
        if (currentImageUrls != null && !currentImageUrls.isEmpty()) {
            removedImageUrls.removeAll(currentImageUrls);
        }

        for (String removedImageUrl : removedImageUrls) {
            r2Service.deleteFileByUrl(removedImageUrl);
        }
    }
}
