package com.indiestation.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.indiestation.common.Result;
import com.indiestation.entity.Cart;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;
import com.indiestation.entity.Product;
import com.indiestation.entity.ProductSku;
import com.indiestation.entity.User;
import com.indiestation.entity.dto.InquirySubmitDTO;
import com.indiestation.mapper.CartMapper;
import com.indiestation.mapper.InquiryItemMapper;
import com.indiestation.mapper.InquiryMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.mapper.ProductSkuMapper;
import com.indiestation.mapper.UserMapper;
import com.indiestation.service.InquiryNoGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 门户端询盘控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal/inquiries")
@RequiredArgsConstructor
public class PortalInquiryController {

    private final InquiryMapper inquiryMapper;
    private final InquiryItemMapper inquiryItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserMapper userMapper;
    private final InquiryNoGenerator inquiryNoGenerator;

    /**
     * 提交询盘（从购物车生成订单）
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> submit(@Valid @RequestBody InquirySubmitDTO dto) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 查询用户购物车
        List<Cart> cartItems = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        if (cartItems.isEmpty()) {
            return Result.error("购物车为空");
        }

        List<InquiryItem> items = new ArrayList<>();
        List<String> invalidProductMessages = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : cartItems) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStatus() != 1 || product.getDeleted() != 0) {
                invalidProductMessages.add("商品已失效，无法提交询盘");
                continue;
            }

            InquiryItem item = new InquiryItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setQuantity(cart.getQuantity());

            BigDecimal unitPrice = product.getDiscountPrice() != null
                    ? product.getDiscountPrice() : product.getPrice();

            if (cart.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(cart.getSkuId());
                if (sku == null || sku.getStatus() != 1 || !sku.getProductId().equals(product.getId())) {
                    invalidProductMessages.add("商品「" + product.getName() + "」的规格已失效，请重新选择");
                    continue;
                }
                item.setSkuId(sku.getId());
                item.setSkuSpec(buildSkuSpec(sku));
                unitPrice = sku.getPrice() != null ? sku.getPrice() : unitPrice;
            }

            item.setPrice(unitPrice);
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(cart.getQuantity())));
            items.add(item);
        }

        if (!invalidProductMessages.isEmpty()) {
            return Result.error(String.join("；", invalidProductMessages));
        }

        if (items.isEmpty()) {
            return Result.error("购物车中没有可提交的商品");
        }

        // 创建询盘订单
        Inquiry inquiry = new Inquiry();
        inquiry.setInquiryNo(inquiryNoGenerator.generateInquiryNo());
        inquiry.setUserId(userId);
        inquiry.setUserName(user.getUsername());
        inquiry.setUserEmail(user.getEmail());
        inquiry.setUserWhatsapp(user.getWhatsapp());
        inquiry.setTotalAmount(totalAmount);
        inquiry.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        inquiry.setStatus(0);
        inquiryMapper.insert(inquiry);

        // 创建询盘明细
        for (InquiryItem item : items) {
            item.setInquiryId(inquiry.getId());
            inquiryItemMapper.insert(item);
        }

        // 清空购物车
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));

        return Result.success(inquiry.getId());
    }

    /**
     * 查询我的询盘列表
     */
    @GetMapping
    public Result<List<Inquiry>> myInquiries() {
        Long userId = getCurrentUserId();
        List<Inquiry> list = inquiryMapper.selectList(
                new LambdaQueryWrapper<Inquiry>()
                        .eq(Inquiry::getUserId, userId)
                        .orderByDesc(Inquiry::getCreateTime));
        return Result.success(list);
    }

    /**
     * 查询询盘详情
     */
    @GetMapping("/{id}")
    public Result<Inquiry> detail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Inquiry inquiry = inquiryMapper.selectById(id);
        if (inquiry == null || !inquiry.getUserId().equals(userId)) {
            return Result.error("询盘不存在");
        }
        List<InquiryItem> items = inquiryItemMapper.selectList(
                new LambdaQueryWrapper<InquiryItem>()
                        .eq(InquiryItem::getInquiryId, id));
        inquiry.setItems(items);
        return Result.success(inquiry);
    }

    /**
     * 生成SKU展示文本
     */
    private String buildSkuSpec(ProductSku sku) {
        if (StringUtils.hasText(sku.getSpecName()) && StringUtils.hasText(sku.getSpecValue())) {
            return sku.getSpecName() + ":" + sku.getSpecValue();
        }
        if (StringUtils.hasText(sku.getSpecValue())) {
            return sku.getSpecValue();
        }
        if (StringUtils.hasText(sku.getSpecName())) {
            return sku.getSpecName();
        }
        return "默认规格";
    }

    private Long getCurrentUserId() {
        String loginId = (String) StpUtil.getLoginId();
        return Long.parseLong(loginId.replace("portal:", ""));
    }
}
