package com.indiestation.service;

/**
 * 邮件服务
 *
 * @author IndieStation
 */
public interface EmailService {

    /**
     * 发送测试邮件
     *
     * @param to 收件人邮箱
     */
    void sendTestEmail(String to);
}
