package com.indiestation.service.impl;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.indiestation.service.InquiryNoGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 基于雪花算法的询盘编号生成器
 *
 * @author IndieStation
 */
@Service
public class InquiryNoGeneratorImpl implements InquiryNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Snowflake snowflake = IdUtil.getSnowflake(1, 1);

    @Override
    public String generateInquiryNo() {
        String datePrefix = LocalDateTime.now().format(DATE_FORMATTER);
        long suffix = Math.abs(snowflake.nextId() % 1_000_000_000L);
        return "INQ" + datePrefix + String.format("%09d", suffix);
    }
}
