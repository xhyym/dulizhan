package com.indiestation.service;

import com.indiestation.entity.vo.LoginVo;
import com.indiestation.entity.vo.AdminInfoVo;
import com.indiestation.entity.dto.AdminLoginDTO;

/**
 * 管理员认证服务
 *
 * @author IndieStation
 */
public interface AuthService {

    /**
     * 管理员登录
     */
    LoginVo login(AdminLoginDTO dto);

    /**
     * 获取当前登录管理员信息
     */
    AdminInfoVo getAdminInfo();

    /**
     * 退出登录
     */
    void logout();
}
