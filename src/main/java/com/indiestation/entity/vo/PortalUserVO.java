package com.indiestation.entity.vo;

import lombok.Data;

/**
 * 门户端用户信息
 *
 * @author IndieStation
 */
@Data
public class PortalUserVO {

    private Long id;
    private String username;
    private String email;
    private String whatsapp;
    private String avatar;
}
