package com.indiestation.controller.admin;

import com.indiestation.common.Result;
import com.indiestation.entity.vo.DashboardVO;
import com.indiestation.entity.vo.VisitorStatsVO;
import com.indiestation.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 仪表盘控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final VisitService visitService;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping
    public Result<DashboardVO> getDashboard() {
        return Result.success(visitService.getDashboardStats());
    }

    /**
     * 获取访客统计详情
     */
    @GetMapping("/visitor-stats")
    public Result<VisitorStatsVO> getVisitorStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(visitService.getVisitorStats(startDate, endDate));
    }
}
