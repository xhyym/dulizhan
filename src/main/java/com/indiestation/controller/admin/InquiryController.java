package com.indiestation.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.indiestation.common.PageResult;
import com.indiestation.common.Result;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;
import com.indiestation.entity.dto.InquiryStatusUpdateDTO;
import com.indiestation.service.InquiryService;
import com.indiestation.service.PdfService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

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
    private final PdfService pdfService;

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
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody InquiryStatusUpdateDTO dto) {
        inquiryService.updateStatus(id, dto.getStatus(), dto.getAdminRemark());
        return Result.success();
    }

    /**
     * 生成转换单 PDF
     */
    @GetMapping("/{id}/pdf")
    public void generatePdf(@PathVariable Long id, HttpServletResponse response) throws IOException {
        Inquiry inquiry = inquiryService.getInquiryDetail(id);
        List<InquiryItem> items = inquiry.getItems();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=inquiry_" + inquiry.getInquiryNo() + ".pdf");

        pdfService.generateInquiryPdf(inquiry, items, response.getOutputStream());
    }
}
