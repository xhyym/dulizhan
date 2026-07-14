package com.indiestation.support.upload;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * 后台图片上传用途定义。
 * 这里统一约束不同图片位允许的最大文件大小、图片比例，并为 R2 对象目录做分类。
 */
public enum AdminUploadPurpose {

    CATEGORY_IMAGE("category_image", "分类图片", "category", 5L * 1024 * 1024, 0.72D, 0.78D),
    PRODUCT_GALLERY("product_gallery", "商品主图/副图", "product-gallery", 8L * 1024 * 1024, 0.72D, 0.78D),
    PRODUCT_POSTER("product_poster", "商品海报图", "product-poster", 10L * 1024 * 1024, 2.28D, 2.39D),
    PRODUCT_DETAIL("product_detail", "商品详情图", "product-detail", 10L * 1024 * 1024, 0.72D, 0.78D),
    SITE_LOGO("site_logo", "网站 Logo", "site-logo", 3L * 1024 * 1024, 2.7D, 3.3D),
    SITE_FAVICON("site_favicon", "Title Logo", "site-favicon", 2L * 1024 * 1024, 0.95D, 1.05D),
    SITE_PAGE_BANNER("site_page_banner", "页面背景图", "site-banner", 8L * 1024 * 1024, 2.28D, 2.39D),
    HOME_BANNER("home_banner", "首页轮播图", "home-banner", 10L * 1024 * 1024, 1.72D, 1.83D),
    ABOUT_STORY("about_story", "品牌故事图片", "about-story", 6L * 1024 * 1024, 0.95D, 1.05D),
    ABOUT_CRAFT("about_craft", "展示图片", "about-craft", 8L * 1024 * 1024, 1.45D, 1.55D);

    private final String code;
    private final String label;
    private final String directory;
    private final long maxFileSize;
    private final double minRatio;
    private final double maxRatio;

    AdminUploadPurpose(String code, String label, String directory, long maxFileSize, double minRatio, double maxRatio) {
        this.code = code;
        this.label = label;
        this.directory = directory;
        this.maxFileSize = maxFileSize;
        this.minRatio = minRatio;
        this.maxRatio = maxRatio;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public String getDirectory() {
        return directory;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public double getMinRatio() {
        return minRatio;
    }

    public double getMaxRatio() {
        return maxRatio;
    }

    public static Optional<AdminUploadPurpose> fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }
}
