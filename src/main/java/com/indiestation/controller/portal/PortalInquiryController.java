package com.indiestation.controller.portal;

import com.indiestation.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门户端询盘控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/inquiries")
@RequiredArgsConstructor
public class PortalInquiryController {

    /**
     * 提交询盘
     * TODO: 实现
     */
    @PostMapping
    public Result<Void> submit() {
        return Result.error("功能开发中");
    }
}
