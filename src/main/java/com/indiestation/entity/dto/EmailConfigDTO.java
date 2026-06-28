package com.indiestation.entity.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 邮件配置DTO
 *
 * 用于统一承载SMTP配置，便于校验和复用。
 *
 * @author IndieStation
 */
@Data
public class EmailConfigDTO {

    /** SMTP服务器地址 */
    @JsonAlias("smtp_host")
    private String smtpHost;

    /** SMTP端口 */
    @JsonAlias("smtp_port")
    private Integer smtpPort;

    /** 加密方式: none / ssl / tls */
    @JsonAlias("smtp_encryption")
    private String smtpEncryption;

    /** SMTP登录用户名 */
    @JsonAlias("smtp_username")
    private String smtpUsername;

    /** SMTP登录密码或授权码 */
    @JsonAlias("smtp_password")
    private String smtpPassword;

    /** 发件人名称 */
    @JsonAlias("from_name")
    private String fromName;

    /** 发件人邮箱 */
    @JsonAlias("from_email")
    private String fromEmail;
}
