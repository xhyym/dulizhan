package com.indiestation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiestation.entity.dto.EmailConfigDTO;
import com.indiestation.exception.BusinessException;
import com.indiestation.service.EmailService;
import com.indiestation.service.SiteConfigService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;

/**
 * 邮件服务实现
 *
 * @author IndieStation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final SiteConfigService siteConfigService;
    private final ObjectMapper objectMapper;

    @Override
    public void sendTextEmail(String to, String subject, String content) {
        validateRecipient(to);
        EmailConfigDTO emailConfig = loadEmailConfig();
        JavaMailSenderImpl mailSender = createMailSender(emailConfig);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(resolveFromAddress(emailConfig));
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            log.info("纯文本邮件发送成功，收件邮箱：{}，邮件主题：{}", to, subject);
        } catch (Exception e) {
            log.error("发送纯文本邮件失败，收件邮箱：{}，邮件主题：{}", to, subject, e);
            throw new BusinessException("发送邮件失败: " + e.getMessage());
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        validateRecipient(to);
        EmailConfigDTO emailConfig = loadEmailConfig();
        JavaMailSenderImpl mailSender = createMailSender(emailConfig);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(resolveFromAddress(emailConfig), resolveFromName(emailConfig));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("HTML邮件发送成功，收件邮箱：{}，邮件主题：{}", to, subject);
        } catch (Exception e) {
            log.error("发送HTML邮件失败，收件邮箱：{}，邮件主题：{}", to, subject, e);
            throw new BusinessException("发送邮件失败: " + e.getMessage());
        }
    }

    @Override
    public void sendTestEmail(String to) {
        sendHtmlEmail(to, "【OSEN FURNITURE】测试邮件", getTestEmailContent());
    }

    @Override
    public void validateEmailConfig(EmailConfigDTO emailConfig) {
        if (emailConfig == null) {
            throw new BusinessException("邮件配置不能为空");
        }
        if (!StringUtils.hasText(emailConfig.getSmtpHost())) {
            throw new BusinessException("SMTP服务器不能为空");
        }
        if (emailConfig.getSmtpPort() == null || emailConfig.getSmtpPort() <= 0 || emailConfig.getSmtpPort() > 65535) {
            throw new BusinessException("SMTP端口配置不正确");
        }
        if (!StringUtils.hasText(emailConfig.getSmtpUsername())) {
            throw new BusinessException("SMTP用户名不能为空");
        }
        if (!StringUtils.hasText(emailConfig.getSmtpPassword())) {
            throw new BusinessException("SMTP密码或授权码不能为空");
        }
        String encryption = normalizeEncryption(emailConfig.getSmtpEncryption());
        if (!"none".equals(encryption) && !"ssl".equals(encryption) && !"tls".equals(encryption)) {
            throw new BusinessException("SMTP加密方式仅支持 none / ssl / tls");
        }
        if (StringUtils.hasText(emailConfig.getFromEmail()) && !isValidEmail(emailConfig.getFromEmail())) {
            throw new BusinessException("发件人邮箱格式不正确");
        }
    }

    /**
     * 加载邮件配置并做统一校验，确保后续所有邮件发送逻辑复用同一套配置入口。
     */
    private EmailConfigDTO loadEmailConfig() {
        Map<String, String> configMap = siteConfigService.getConfigMap();
        String emailConfigJson = configMap.get("email_config");

        if (!StringUtils.hasText(emailConfigJson)) {
            throw new BusinessException("请先在系统设置中配置邮件服务");
        }

        try {
            EmailConfigDTO emailConfig = objectMapper.readValue(emailConfigJson, EmailConfigDTO.class);
            if (emailConfig.getSmtpPort() == null) {
                emailConfig.setSmtpPort(587);
            }
            if (!StringUtils.hasText(emailConfig.getSmtpEncryption())) {
                emailConfig.setSmtpEncryption("tls");
            }
            validateEmailConfig(emailConfig);
            return emailConfig;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("解析邮件配置失败，原始配置内容：{}", emailConfigJson, e);
            throw new BusinessException("邮件配置格式不正确，请重新保存邮件配置");
        }
    }

    /**
     * 按配置创建SMTP发信客户端，供测试邮件、验证码邮件、通知邮件等统一复用。
     */
    private JavaMailSenderImpl createMailSender(EmailConfigDTO emailConfig) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getSmtpHost().trim());
        mailSender.setPort(emailConfig.getSmtpPort());
        mailSender.setUsername(emailConfig.getSmtpUsername().trim());
        mailSender.setPassword(emailConfig.getSmtpPassword());
        mailSender.setDefaultEncoding("UTF-8");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        String encryption = normalizeEncryption(emailConfig.getSmtpEncryption());
        if ("ssl".equals(encryption)) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.port", String.valueOf(emailConfig.getSmtpPort()));
        } else if ("tls".equals(encryption)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        mailSender.setJavaMailProperties(props);
        return mailSender;
    }

    private String resolveFromAddress(EmailConfigDTO emailConfig) {
        return StringUtils.hasText(emailConfig.getFromEmail())
                ? emailConfig.getFromEmail().trim()
                : emailConfig.getSmtpUsername().trim();
    }

    private String resolveFromName(EmailConfigDTO emailConfig) {
        return StringUtils.hasText(emailConfig.getFromName())
                ? emailConfig.getFromName().trim()
                : "OSEN FURNITURE";
    }

    private void validateRecipient(String to) {
        if (!StringUtils.hasText(to) || !isValidEmail(to)) {
            throw new BusinessException("收件邮箱格式不正确");
        }
    }

    private String normalizeEncryption(String encryption) {
        return StringUtils.hasText(encryption) ? encryption.trim().toLowerCase() : "none";
    }

    private boolean isValidEmail(String email) {
        return StringUtils.hasText(email)
                && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private String getTestEmailContent() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #409eff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border: 1px solid #eee; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>OSEN FURNITURE</h1>
                        </div>
                        <div class="content">
                            <h2>测试邮件</h2>
                            <p>恭喜，您的SMTP邮件配置已经生效。</p>
                            <p>这是一封测试邮件，用于验证当前邮箱服务配置是否可以正常发信。</p>
                            <p>如果您已经收到此邮件，说明后续验证码邮件、通知邮件等能力具备接入基础。</p>
                        </div>
                        <div class="footer">
                            <p>此邮件由系统自动发送，请勿直接回复</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
