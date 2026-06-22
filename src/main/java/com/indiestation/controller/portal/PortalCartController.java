package com.indiestation.controller.portal;

import com.indiestation.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 门户端购物车控制器 (Redis)
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/cart")
@RequiredArgsConstructor
public class PortalCartController {

    /**
     * 获取购物车
     * TODO: 实现 Redis 购物车
     */
    @GetMapping
    public Result<Void> getCart() {
        return Result.error("功能开发中");
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public Result<Void> addItem() {
        return Result.error("功能开发中");
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/items/{productId}")
    public Result<Void> updateItem(@PathVariable Long productId) {
        return Result.error("功能开发中");
    }

    /**
     * 移除购物车商品
     */
    @DeleteMapping("/items/{productId}")
    public Result<Void> removeItem(@PathVariable Long productId) {
        return Result.error("功能开发中");
    }
}
