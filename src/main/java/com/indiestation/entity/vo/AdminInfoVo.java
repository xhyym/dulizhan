package com.indiestation.entity.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理员用户信息VO
 *
 * 对应前端 Api.Auth.UserInfo 接口
 *
 * @author IndieStation
 */
@Data
public class AdminInfoVo {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String userName;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 头像 */
    private String avatar;

    /** 角色列表 */
    private List<String> roles;

    /** 按钮权限列表 */
    private List<String> buttons;
}
