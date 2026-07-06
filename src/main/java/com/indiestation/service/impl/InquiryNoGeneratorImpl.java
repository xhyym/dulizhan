package com.indiestation.service.impl;

import com.indiestation.exception.BusinessException;
import com.indiestation.service.InquiryNoGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 每日递增序列的询盘编号生成器
 *
 * @author IndieStation
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InquiryNoGeneratorImpl implements InquiryNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String INQUIRY_NO_KEY_PREFIX = "inquiry:no:";
    private static final int INQUIRY_NO_SEQUENCE_WIDTH = 8;
    private static final long INQUIRY_NO_KEY_EXPIRE_DAYS = 3L;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public String generateInquiryNo() {
        LocalDate today = LocalDate.now();
        String datePrefix = today.format(DATE_FORMATTER);
        String sequenceKey = INQUIRY_NO_KEY_PREFIX + datePrefix;

        try {
            Long sequence = stringRedisTemplate.opsForValue().increment(sequenceKey);
            if (sequence == null || sequence <= 0) {
                throw new BusinessException("生成询盘编号失败，请稍后重试");
            }

            if (sequence == 1L) {
                stringRedisTemplate.expire(sequenceKey, INQUIRY_NO_KEY_EXPIRE_DAYS, TimeUnit.DAYS);
            }

            return "INQ" + datePrefix + String.format("%0" + INQUIRY_NO_SEQUENCE_WIDTH + "d", sequence);
        } catch (DataAccessException exception) {
            log.error("通过Redis生成询盘编号失败，日期前缀：{}", datePrefix, exception);
            throw new BusinessException("生成询盘编号失败，请稍后重试");
        }
    }
}
