package com.indiestation.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门户端登录参数
 *
 * @author IndieStation
 */
@Data
public class PortalLoginDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "WhatsApp号码不能为空")
    private String whatsapp;
}
