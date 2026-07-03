package com.indiestation.entity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门户端更新 WhatsApp 参数
 *
 * @author IndieStation
 */
@Data
public class PortalUpdateWhatsappDTO {

    @NotBlank(message = "WhatsApp number is required")
    private String whatsapp;
}
