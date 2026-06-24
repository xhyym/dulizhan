package com.indiestation.service;

import com.indiestation.entity.vo.PortalUserVO;

/**
 * 门户端认证服务
 *
 * @author IndieStation
 */
public interface PortalAuthService {

    /**
     * WhatsApp + 邮箱登录（不存在则自动注册）
     *
     * @param email     邮箱
     * @param whatsapp  WhatsApp号码
     * @return 用户信息 + Token
     */
    PortalUserVO login(String email, String whatsapp);

    /**
     * 获取当前登录用户信息
     */
    PortalUserVO getCurrentUser(Long userId);
}
