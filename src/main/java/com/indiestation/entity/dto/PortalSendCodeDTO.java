package com.indiestation.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门户端发送邮箱验证码参数
 *
 * @author IndieStation
 */
@Data
public class PortalSendCodeDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;
}
