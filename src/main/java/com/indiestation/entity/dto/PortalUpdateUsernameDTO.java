package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 门户端更新用户名参数
 *
 * @author IndieStation
 */
@Data
public class PortalUpdateUsernameDTO {

    @NotBlank(message = "Username is required")
    @Size(max = 30, message = "Username must be 30 characters or fewer")
    private String username;
}
