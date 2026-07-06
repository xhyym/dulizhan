package com.indiestation.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.common.Result;
import com.indiestation.entity.Cart;
import com.indiestation.entity.Product;
import com.indiestation.entity.ProductSku;
import com.indiestation.entity.dto.CartItemDTO;
import com.indiestation.entity.vo.CartItemVO;
import com.indiestation.mapper.CartMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.mapper.ProductSkuMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 门户端购物车控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/cart")
@RequiredArgsConstructor
public class PortalCartController {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;

    /**
     * 获取购物车列表
     */
    @GetMapping
    public Result<List<CartItemVO>> getCart() {
        Long userId = getCurrentUserId();
        List<Cart> cartItems = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getCreateTime));

        List<CartItemVO> result = new ArrayList<>();
        for (Cart cart : cartItems) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStatus() != 1 || product.getDeleted() != 0) {
                continue;
            }

            CartItemVO vo = new CartItemVO();
            vo.setId(cart.getId());
            vo.setProductId(cart.getProductId());
            vo.setSkuId(cart.getSkuId());
            vo.setQuantity(cart.getQuantity());
            vo.setProductName(product.getName());
            vo.setMainImage(product.getMainImage());
            vo.setPrice(product.getPrice());
            vo.setDiscountPrice(product.getDiscountPrice());

            if (cart.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(cart.getSkuId());
                if (sku != null && sku.getStatus() == 1) {
                    vo.setSkuCode(sku.getSkuCode());
                    vo.setSpecName(sku.getSpecName());
                    vo.setSpecValue(sku.getSpecValue());
                }
            }
            result.add(vo);
        }
        return Result.success(result);
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public Result<Void> addItem(@Valid @RequestBody CartItemDTO dto) {
        Long userId = getCurrentUserId();

        // 校验商品是否存在
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() != 1 || product.getDeleted() != 0) {
            return Result.error("商品不存在或已下架");
        }

        // 校验 SKU
        if (dto.getSkuId() != null) {
            ProductSku sku = productSkuMapper.selectById(dto.getSkuId());
            if (sku == null || sku.getStatus() != 1 || !sku.getProductId().equals(dto.getProductId())) {
                return Result.error("SKU 无效");
            }
        }

        // 检查购物车是否已有同一商品+SKU
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, dto.getProductId());
        if (dto.getSkuId() != null) {
            wrapper.eq(Cart::getSkuId, dto.getSkuId());
        } else {
            wrapper.isNull(Cart::getSkuId);
        }
        Cart existing = cartMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setSkuId(dto.getSkuId());
            cart.setQuantity(dto.getQuantity());
            cartMapper.insert(cart);
        }

        return Result.success();
    }

    /**
     * 更新购物车商品数量
     */
    @PutMapping("/items/{id}")
    public Result<Void> updateItem(@PathVariable Long id, @RequestParam Integer quantity) {
        if (quantity < 1) {
            return Result.error("数量不能小于1");
        }
        Long userId = getCurrentUserId();
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.error("购物车项不存在");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
        return Result.success();
    }

    /**
     * 移除购物车商品
     */
    @DeleteMapping("/items/{id}")
    public Result<Void> removeItem(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.error("购物车项不存在");
        }
        cartMapper.deleteById(id);
        return Result.success();
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public Result<Void> clearCart() {
        Long userId = getCurrentUserId();
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        return Result.success();
    }

    private Long getCurrentUserId() {
        String loginId = (String) StpUtil.getLoginId();
        return Long.parseLong(loginId.replace("portal:", ""));
    }
}
