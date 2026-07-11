package com.indiestation.support.upload;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * 后台图片上传用途定义。
 * 这里统一约束不同图片位允许的最大文件大小，并为 R2 对象目录做分类。
 */
public enum AdminUploadPurpose {

    CATEGORY_IMAGE("category_image", "分类图片", "category", 5L * 1024 * 1024),
    PRODUCT_GALLERY("product_gallery", "商品主图/副图", "product-gallery", 8L * 1024 * 1024),
    PRODUCT_POSTER("product_poster", "商品海报图", "product-poster", 10L * 1024 * 1024),
    PRODUCT_DETAIL("product_detail", "商品详情图", "product-detail", 10L * 1024 * 1024),
    SITE_LOGO("site_logo", "网站 Logo", "site-logo", 3L * 1024 * 1024),
    SITE_FAVICON("site_favicon", "Title Logo", "site-favicon", 2L * 1024 * 1024),
    SITE_PAGE_BANNER("site_page_banner", "页面背景图", "site-banner", 8L * 1024 * 1024),
    HOME_BANNER("home_banner", "首页轮播图", "home-banner", 10L * 1024 * 1024),
    ABOUT_STORY("about_story", "品牌故事图片", "about-story", 6L * 1024 * 1024),
    ABOUT_CRAFT("about_craft", "工艺展示图片", "about-craft", 8L * 1024 * 1024);

    private final String code;
    private final String label;
    private final String directory;
    private final long maxFileSize;

    AdminUploadPurpose(String code, String label, String directory, long maxFileSize) {
        this.code = code;
        this.label = label;
        this.directory = directory;
        this.maxFileSize = maxFileSize;
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

    public static Optional<AdminUploadPurpose> fromCode(String code) {
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }

        return Arrays.stream(values())
                .filter(item -> item.code.equalsIgnoreCase(code.trim()))
                .findFirst();
    }
}
