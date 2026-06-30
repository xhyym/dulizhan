package com.indiestation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.User;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.UserMapper;
import com.indiestation.service.EmailService;
import com.indiestation.service.PortalAuthService;
import com.indiestation.entity.vo.PortalUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 门户端认证服务实现
 *
 * @author IndieStation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalAuthServiceImpl implements PortalAuthService {

    private static final String LOGIN_CODE_KEY_PREFIX = "portal:login:code:";
    private static final String LOGIN_CODE_COOLDOWN_KEY_PREFIX = "portal:login:code:cooldown:";
    private static final long LOGIN_CODE_EXPIRE_MINUTES = 10;
    private static final long LOGIN_CODE_COOLDOWN_SECONDS = 60;

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendLoginCode(String email, String whatsapp) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedWhatsapp = normalizeWhatsapp(whatsapp);

        String cooldownKey = LOGIN_CODE_COOLDOWN_KEY_PREFIX + normalizedEmail;
        Boolean canSend = stringRedisTemplate.opsForValue().setIfAbsent(
                cooldownKey,
                "1",
                LOGIN_CODE_COOLDOWN_SECONDS,
                TimeUnit.SECONDS
        );

        if (Boolean.FALSE.equals(canSend)) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        String verificationCode = RandomUtil.randomNumbers(6);
        String codeKey = LOGIN_CODE_KEY_PREFIX + normalizedEmail;
        try {
            stringRedisTemplate.opsForValue().set(
                    codeKey,
                    buildCodeCacheValue(verificationCode, normalizedWhatsapp),
                    LOGIN_CODE_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );

            emailService.sendHtmlEmail(
                    normalizedEmail,
                    "【OSEN FURNITURE】邮箱验证码",
                    buildLoginCodeEmailContent(verificationCode)
            );
        } catch (Exception e) {
            stringRedisTemplate.delete(cooldownKey);
            stringRedisTemplate.delete(codeKey);
            throw e;
        }

        log.info("门户邮箱验证码发送成功，邮箱：{}", normalizedEmail);
    }

    @Override
    public PortalUserVO login(String email, String whatsapp, String code) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedWhatsapp = normalizeWhatsapp(whatsapp);
        String normalizedCode = normalizeCode(code);

        validateLoginCode(normalizedEmail, normalizedWhatsapp, normalizedCode);

        // 按邮箱查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, normalizedEmail));

        if (user == null) {
            // 自动注册
            user = new User();
            user.setEmail(normalizedEmail);
            user.setWhatsapp(normalizedWhatsapp);
            user.setUsername(buildUsernameFromEmail(normalizedEmail));
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 更新登录时间和WhatsApp
            if (user.getStatus() != null && user.getStatus() != 1) {
                throw new BusinessException("当前账号已被禁用");
            }
            user.setLastLoginTime(LocalDateTime.now());
            user.setWhatsapp(normalizedWhatsapp);
            userMapper.updateById(user);
        }

        // Sa-Token 登录（使用 portal: 前缀区分管理端）
        StpUtil.login("portal:" + user.getId());

        stringRedisTemplate.delete(LOGIN_CODE_KEY_PREFIX + normalizedEmail);

        return toUserVO(user);
    }

    @Override
    public PortalUserVO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return null;
        return toUserVO(user);
    }

    private PortalUserVO toUserVO(User user) {
        PortalUserVO vo = new PortalUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setWhatsapp(user.getWhatsapp());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("邮箱不能为空");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeWhatsapp(String whatsapp) {
        if (!StringUtils.hasText(whatsapp)) {
            throw new BusinessException("WhatsApp号码不能为空");
        }
        String normalized = whatsapp.replaceAll("[^\\d+]", "");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("WhatsApp号码格式不正确");
        }
        return normalized;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("验证码不能为空");
        }
        return code.trim();
    }

    private String buildCodeCacheValue(String verificationCode, String whatsapp) {
        return verificationCode + "|" + whatsapp;
    }

    private void validateLoginCode(String email, String whatsapp, String code) {
        String cachedValue = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY_PREFIX + email);
        if (!StringUtils.hasText(cachedValue)) {
            throw new BusinessException("验证码已过期，请重新获取");
        }

        String[] cachedParts = cachedValue.split("\\|", 2);
        if (cachedParts.length != 2) {
            stringRedisTemplate.delete(LOGIN_CODE_KEY_PREFIX + email);
            throw new BusinessException("验证码状态异常，请重新获取");
        }

        String cachedCode = cachedParts[0];
        String cachedWhatsapp = cachedParts[1];

        if (!cachedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        if (!cachedWhatsapp.equals(whatsapp)) {
            throw new BusinessException("当前 WhatsApp 信息与验证码申请时不一致");
        }
    }

    private String buildUsernameFromEmail(String email) {
        return email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
    }

    private String buildLoginCodeEmailContent(String verificationCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 24px; }
                        .header { padding-bottom: 16px; border-bottom: 1px solid #e5e5e5; }
                        .brand { font-size: 24px; font-weight: 600; color: #1a1a1a; }
                        .content { padding: 32px 0; }
                        .code-box {
                            display: inline-block;
                            padding: 12px 20px;
                            font-size: 28px;
                            font-weight: 700;
                            letter-spacing: 6px;
                            color: #1a1a1a;
                            background: #f7f7f7;
                            border: 1px solid #e5e5e5;
                        }
                        .tip { margin-top: 16px; color: #666; font-size: 13px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <div class="brand">OSEN FURNITURE</div>
                        </div>
                        <div class="content">
                            <p>您好，您正在进行门户网站登录验证。</p>
                            <p>本次邮箱验证码为：</p>
                            <div class="code-box">%s</div>
                            <p class="tip">验证码 10 分钟内有效，请勿泄露给他人。如果这不是您的操作，请忽略此邮件。</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationCode);
    }
}
