package com.indiestation.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.entity.Inquiry;
import com.indiestation.mapper.InquiryMapper;
import com.indiestation.service.EmailService;
import com.indiestation.support.BusinessTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 询盘每日汇总定时任务
 *
 * @author IndieStation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryReportTask {

    private final InquiryMapper inquiryMapper;
    private final EmailService emailService;
    private final BusinessTimeProvider businessTimeProvider;

    /**
     * 每天上午 8:00 统计昨日新增询盘并发送通知邮件
     */
    @Scheduled(cron = "0 0 8 * * ?", zone = "${app.business-time-zone:Australia/Sydney}")
    public void sendDailyReport() {
        log.info("开始执行每日询盘汇总任务...");
        try {
            LocalDate yesterday = businessTimeProvider.today().minusDays(1);
            LocalDateTime startOfYesterday = yesterday.atStartOfDay();
            LocalDateTime endOfYesterday = yesterday.plusDays(1).atStartOfDay();

            Long count = inquiryMapper.selectCount(
                    new LambdaQueryWrapper<Inquiry>()
                            .ge(Inquiry::getCreateTime, startOfYesterday)
                            .lt(Inquiry::getCreateTime, endOfYesterday)
            );

            String dateStr = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE);
            emailService.sendDailyInquiryReport(dateStr, count.intValue());

            log.info("每日询盘汇总任务完成，日期：{}，新增询盘：{} 条", dateStr, count);
        } catch (Exception e) {
            log.error("每日询盘汇总任务失败", e);
        }
    }
}
