package com.indiestation.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.indiestation.common.PageResult;
import com.indiestation.common.Result;
import com.indiestation.entity.Inquiry;
import com.indiestation.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 询盘管理控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 询盘分页列表
     */
    @GetMapping
    public Result<PageResult<Inquiry>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String inquiryNo,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) Integer status) {

        IPage<Inquiry> page = inquiryService.getInquiryPage(current, size, inquiryNo, userName, status);
        PageResult<Inquiry> result = new PageResult<>(
                page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal()
        );
        return Result.success(result);
    }

    /**
     * 询盘详情
     */
    @GetMapping("/{id}")
    public Result<Inquiry> detail(@PathVariable Long id) {
        Inquiry inquiry = inquiryService.getInquiryDetail(id);
        return Result.success(inquiry);
    }

    /**
     * 更新询盘状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String adminRemark = (String) params.get("adminRemark");
        inquiryService.updateStatus(id, status, adminRemark);
        return Result.success();
    }

    /**
     * 导出 Excel (TODO)
     */
    @GetMapping("/export/excel")
    public Result<Void> exportExcel() {
        // TODO: 实现 Excel 导出
        return Result.error("功能开发中");
    }

    /**
     * 导出 PDF (TODO)
     */
    @GetMapping("/export/pdf")
    public Result<Void> exportPdf() {
        // TODO: 实现 PDF 导出
        return Result.error("功能开发中");
    }
}
