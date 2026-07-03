package com.indiestation.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.User;
import com.indiestation.entity.vo.PortalUserVO;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.UserMapper;
import com.indiestation.service.EmailService;
import com.indiestation.service.PortalAuthService;
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
    public void sendLoginCode(String email) {
        String normalizedEmail = normalizeEmail(email);

        String cooldownKey = LOGIN_CODE_COOLDOWN_KEY_PREFIX + normalizedEmail;
        Boolean canSend = stringRedisTemplate.opsForValue().setIfAbsent(
                cooldownKey,
                "1",
                LOGIN_CODE_COOLDOWN_SECONDS,
                TimeUnit.SECONDS
        );

        if (Boolean.FALSE.equals(canSend)) {
            throw new BusinessException("Verification code requests are too frequent. Please try again in a minute.");
        }

        String verificationCode = RandomUtil.randomNumbers(6);
        String codeKey = LOGIN_CODE_KEY_PREFIX + normalizedEmail;
        try {
            stringRedisTemplate.opsForValue().set(
                    codeKey,
                    verificationCode,
                    LOGIN_CODE_EXPIRE_MINUTES,
                    TimeUnit.MINUTES
            );

            emailService.sendHtmlEmail(
                    normalizedEmail,
                    "OSEN FURNITURE | Your Verification Code",
                    buildLoginCodeEmailContent(verificationCode)
            );
        } catch (BusinessException e) {
            stringRedisTemplate.delete(cooldownKey);
            stringRedisTemplate.delete(codeKey);
            throw new BusinessException("Failed to send the verification email. Please try again later.");
        } catch (Exception e) {
            stringRedisTemplate.delete(cooldownKey);
            stringRedisTemplate.delete(codeKey);
            throw new BusinessException("Failed to send the verification email. Please try again later.");
        }

        log.info("门户邮箱验证码发送成功，邮箱：{}", normalizedEmail);
    }

    @Override
    public PortalUserVO login(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        String normalizedCode = normalizeCode(code);

        validateLoginCode(normalizedEmail, normalizedCode);

        // 按邮箱查找用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, normalizedEmail));

        if (user == null) {
            // 自动注册
            user = new User();
            user.setEmail(normalizedEmail);
            user.setUsername(buildUsernameFromEmail(normalizedEmail));
            user.setStatus(1);
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            // 更新登录时间
            if (user.getStatus() != null && user.getStatus() != 1) {
                throw new BusinessException("This account has been disabled.");
            }
            user.setLastLoginTime(LocalDateTime.now());
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

    @Override
    public PortalUserVO updateWhatsapp(Long userId, String whatsapp) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found.");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException("This account has been disabled.");
        }

        user.setWhatsapp(normalizeWhatsapp(whatsapp));
        userMapper.updateById(user);
        return toUserVO(user);
    }

    @Override
    public PortalUserVO updateUsername(Long userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("User not found.");
        }
        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException("This account has been disabled.");
        }

        String normalizedUsername = normalizeUsername(username);
        User duplicatedUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, normalizedUsername)
                        .ne(User::getId, userId)
                        .last("LIMIT 1")
        );
        if (duplicatedUser != null) {
            throw new BusinessException("This username is already in use.");
        }

        user.setUsername(normalizedUsername);
        userMapper.updateById(user);
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
            throw new BusinessException("Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeWhatsapp(String whatsapp) {
        if (!StringUtils.hasText(whatsapp)) {
            throw new BusinessException("WhatsApp number is required");
        }
        String normalized = whatsapp.replaceAll("[^\\d+]", "");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("Please enter a valid WhatsApp number");
        }
        return normalized;
    }

    private String normalizeCode(String code) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException("Verification code is required");
        }
        return code.trim();
    }

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("Username is required");
        }

        String normalized = username.trim();
        if (normalized.length() > 30) {
            throw new BusinessException("Username must be 30 characters or fewer");
        }
        return normalized;
    }

    private void validateLoginCode(String email, String code) {
        String cachedValue = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY_PREFIX + email);
        if (!StringUtils.hasText(cachedValue)) {
            throw new BusinessException("The verification code has expired. Please request a new one.");
        }

        String normalizedCachedCode = cachedValue.contains("|")
                ? cachedValue.substring(0, cachedValue.indexOf("|"))
                : cachedValue;

        if (!normalizedCachedCode.equals(code)) {
            throw new BusinessException("The verification code is incorrect.");
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
                            <p>Hello,</p>
                            <p>You requested a verification code to sign in to OSEN FURNITURE.</p>
                            <p>Your verification code is:</p>
                            <div class="code-box">%s</div>
                            <p class="tip">This code will expire in 10 minutes. Please do not share it with anyone. If you did not request this code, you can safely ignore this email.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationCode);
    }
}
