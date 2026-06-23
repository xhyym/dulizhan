package com.indiestation.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.indiestation.common.PageResult;
import com.indiestation.common.Result;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.User;
import com.indiestation.service.InquiryService;
import com.indiestation.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final InquiryService inquiryService;

    /**
     * 客户分页列表
     */
    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status) {

        IPage<User> page = userService.getUserPage(current, size, username, email, status);
        PageResult<User> result = new PageResult<>(
                page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal()
        );
        return Result.success(result);
    }

    /**
     * 客户详情
     */
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        User user = userService.getUserDetail(id);
        return Result.success(user);
    }

    /**
     * 获取客户的询盘列表
     */
    @GetMapping("/{id}/inquiries")
    public Result<List<Inquiry>> getUserInquiries(@PathVariable Long id) {
        List<Inquiry> inquiries = inquiryService.getInquiriesByUserId(id);
        return Result.success(inquiries);
    }
}
