package com.indiestation.service;

import com.indiestation.entity.vo.PortalUserVO;

/**
 * 门户端认证服务
 *
 * @author IndieStation
 */
public interface PortalAuthService {

    /**
     * 发送门户邮箱登录验证码
     */
    void sendLoginCode(String email, String whatsapp);

    /**
     * 邮箱验证码登录（不存在则自动注册）
     *
     * @param email     邮箱
     * @param whatsapp  WhatsApp号码
     * @param code      邮箱验证码
     * @return 登录后的门户用户信息
     */
    PortalUserVO login(String email, String whatsapp, String code);

    /**
     * 获取当前登录用户信息
     */
    PortalUserVO getCurrentUser(Long userId);
}
