package com.indiestation.controller.portal;

import com.indiestation.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门户端商品控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/products")
@RequiredArgsConstructor
public class PortalProductController {

    /**
     * 获取商品列表
     * TODO: 实现
     */
    @GetMapping
    public Result<Void> list() {
        return Result.error("功能开发中");
    }

    /**
     * 获取商品详情
     * TODO: 实现
     */
    @GetMapping("/{id}")
    public Result<Void> detail(@PathVariable Long id) {
        return Result.error("功能开发中");
    }
}
