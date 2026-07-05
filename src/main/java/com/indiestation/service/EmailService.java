package com.indiestation.service;

import com.indiestation.entity.dto.EmailConfigDTO;

/**
 * 邮件服务
 *
 * @author IndieStation
 */
public interface EmailService {

    /**
     * 发送纯文本邮件
     */
    void sendTextEmail(String to, String subject, String content);

    /**
     * 发送HTML邮件
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);

    /**
     * 发送测试邮件
     */
    void sendTestEmail(String to);

    /**
     * 校验邮件配置有效性
     */
    void validateEmailConfig(EmailConfigDTO emailConfig);

    /**
     * 发送每日询盘汇总通知邮件
     *
     * @param date  统计日期
     * @param count 新增询盘数量
     */
    void sendDailyInquiryReport(String date, int count);
}
