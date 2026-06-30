package com.indiestation.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 *
 * @author IndieStation
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 管理端 - 登录接口
                        "/api/admin/auth/login",
                        // 门户端 - 公开接口（无需登录）
                        "/api/portal/auth/send-code",
                        "/api/portal/auth/login",
                        "/api/portal/categories",
                        "/api/portal/products/**",
                        "/api/portal/site-config",
                        "/api/portal/visit",
                        // 其他
                        "/api/v3/system/menus/simple"
                );
    }
}
