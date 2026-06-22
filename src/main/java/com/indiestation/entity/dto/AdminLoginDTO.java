package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录DTO
 *
 * @author IndieStation
 */
@Data
public class AdminLoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String userName;

    @NotBlank(message = "密码不能为空")
    private String password;
}
