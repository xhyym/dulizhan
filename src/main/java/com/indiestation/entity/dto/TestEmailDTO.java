package com.indiestation.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 测试邮件发送参数DTO
 *
 * @author IndieStation
 */
@Data
public class TestEmailDTO {

    /** 收件邮箱 */
    @NotBlank(message = "测试邮箱不能为空")
    @Email(message = "测试邮箱格式不正确")
    private String email;
}
