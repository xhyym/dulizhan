package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 预签名 URL 请求 DTO
 *
 * @author IndieStation
 */
@Data
public class PresignedUrlDTO {

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotBlank(message = "文件类型不能为空")
    private String contentType;
}
