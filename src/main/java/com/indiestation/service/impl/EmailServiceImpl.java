package com.indiestation.service.impl;

import com.indiestation.exception.BusinessException;
import com.indiestation.service.EmailService;
import com.indiestation.service.SiteConfigService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

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

    @Override
    public void sendTestEmail(String to) {
        Map<String, String> configMap = siteConfigService.getConfigMap();
        String emailConfigJson = configMap.get("email_config");

        if (emailConfigJson == null || emailConfigJson.isEmpty()) {
            throw new BusinessException("请先配置邮件设置");
        }

        try {
            // 解析邮件配置
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> emailConfig = mapper.readValue(emailConfigJson, Map.class);

            String host = (String) emailConfig.get("smtp_host");
            Integer port = emailConfig.get("smtp_port") != null ? ((Number) emailConfig.get("smtp_port")).intValue() : 587;
            String username = (String) emailConfig.get("smtp_username");
            String password = (String) emailConfig.get("smtp_password");
            String encryption = (String) emailConfig.get("smtp_encryption");
            String fromName = (String) emailConfig.get("from_name");
            String fromEmail = (String) emailConfig.get("from_email");

            if (host == null || username == null || password == null) {
                throw new BusinessException("邮件配置不完整，请检查SMTP设置");
            }

            // 创建邮件发送器
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost(host);
            mailSender.setPort(port);
            mailSender.setUsername(username);
            mailSender.setPassword(password);

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            if ("ssl".equals(encryption)) {
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } else if ("tls".equals(encryption)) {
                props.put("mail.smtp.starttls.enable", "true");
            }
            mailSender.setJavaMailProperties(props);

            // 发送测试邮件
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail != null ? fromEmail : username, fromName != null ? fromName : "Indie Station");
            helper.setTo(to);
            helper.setSubject("【Indie Station】测试邮件");
            helper.setText(getTestEmailContent(), true);

            mailSender.send(message);
            log.info("测试邮件已发送至: {}", to);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送测试邮件失败", e);
            throw new BusinessException("发送失败: " + e.getMessage());
        }
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
                            <h1>Indie Station</h1>
                        </div>
                        <div class="content">
                            <h2>测试邮件</h2>
                            <p>恭喜！您的邮件配置已成功。</p>
                            <p>这是一封测试邮件，用于验证您的SMTP配置是否正确。</p>
                            <p>如果您收到此邮件，说明邮件功能已正常工作。</p>
                        </div>
                        <div class="footer">
                            <p>此邮件由系统自动发送，请勿回复</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
