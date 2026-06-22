package com.indiestation.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理员登录响应VO
 *
 * @author IndieStation
 */
@Data
public class LoginVo {

    /** Token */
    private String token;

    /** 刷新Token */
    private String refreshToken;
}
