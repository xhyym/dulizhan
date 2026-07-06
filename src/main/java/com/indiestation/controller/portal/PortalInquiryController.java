package com.indiestation.controller.portal;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.indiestation.common.PageResult;
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
import com.indiestation.service.InquiryService;
import com.indiestation.service.InquiryNoGenerator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
@Slf4j
public class PortalInquiryController {
    private static final int MAX_INQUIRY_NO_RETRY_TIMES = 5;

    private final InquiryMapper inquiryMapper;
    private final InquiryItemMapper inquiryItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final UserMapper userMapper;
    private final InquiryNoGenerator inquiryNoGenerator;
    private final InquiryService inquiryService;

    /**
     * 提交询盘（从购物车生成订单）
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public Result<Long> submit(@Valid @RequestBody InquirySubmitDTO dto) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("User not found.");
        }

        if (!StringUtils.hasText(user.getWhatsapp())) {
            return Result.error("Please update your WhatsApp number in My Account before submitting an inquiry.");
        }

        // 查询用户购物车
        List<Cart> cartItems = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        if (cartItems.isEmpty()) {
            return Result.error("Your cart is empty.");
        }

        List<InquiryItem> items = new ArrayList<>();
        List<String> invalidProductMessages = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Cart cart : cartItems) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStatus() != 1 || product.getDeleted() != 0) {
                invalidProductMessages.add("One or more products are no longer available and cannot be submitted.");
                continue;
            }

            InquiryItem item = new InquiryItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImage(product.getMainImage());
            item.setQuantity(cart.getQuantity());

            BigDecimal unitPrice = product.getPrice();

            if (cart.getSkuId() != null) {
                ProductSku sku = productSkuMapper.selectById(cart.getSkuId());
                if (sku == null || sku.getStatus() != 1 || !sku.getProductId().equals(product.getId())) {
                    invalidProductMessages.add("The selected specification for product \"" + product.getName() + "\" is no longer available. Please choose again.");
                    continue;
                }
                item.setSkuId(sku.getId());
                item.setSkuSpec(buildSkuSpec(sku));
            }

            item.setPrice(unitPrice);
            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(cart.getQuantity())));
            items.add(item);
        }

        if (!invalidProductMessages.isEmpty()) {
            return Result.error(String.join("；", invalidProductMessages));
        }

        if (items.isEmpty()) {
            return Result.error("There are no valid products in your cart to submit.");
        }

        // 创建询盘订单
        LocalDate deliveryDate;
        try {
            deliveryDate = LocalDate.parse(dto.getDeliveryDate().trim());
        } catch (DateTimeParseException exception) {
            log.warn("门户询盘提交配送时间格式错误，用户ID：{}，原始值：{}", userId, dto.getDeliveryDate());
            return Result.error("Please select a valid delivery date.");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setUserId(userId);
        inquiry.setUserName(user.getUsername());
        inquiry.setUserEmail(user.getEmail());
        inquiry.setUserWhatsapp(user.getWhatsapp());
        inquiry.setTotalAmount(totalAmount);
        inquiry.setRemark(StringUtils.hasText(dto.getRemark()) ? dto.getRemark().trim() : null);
        inquiry.setDeliveryDate(deliveryDate);
        inquiry.setStatus(0);
        insertInquiryWithRetry(inquiry);

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
     * 取消我的询盘
     */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        inquiryService.cancelInquiryByUser(id, getCurrentUserId());
        return Result.success("Inquiry cancelled successfully.", null);
    }

    /**
     * 查询我的询盘列表
     */
    @GetMapping
    public Result<PageResult<Inquiry>> myInquiries(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String inquiryNo) {
        Long userId = getCurrentUserId();
        IPage<Inquiry> page = inquiryService.getPortalInquiryPage(userId, current, size, inquiryNo);
        PageResult<Inquiry> result = new PageResult<>(
                page.getRecords(),
                page.getCurrent(),
                page.getSize(),
                page.getTotal()
        );
        return Result.success(result);
    }

    /**
     * 查询询盘详情
     */
    @GetMapping("/{id}")
    public Result<Inquiry> detail(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Inquiry inquiry = inquiryMapper.selectById(id);
        if (inquiry == null || !inquiry.getUserId().equals(userId)) {
            return Result.error("Inquiry not found.");
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

    /**
     * 通过唯一索引做最终防重，极端情况下若编号冲突则自动重试。
     */
    private void insertInquiryWithRetry(Inquiry inquiry) {
        for (int attempt = 1; attempt <= MAX_INQUIRY_NO_RETRY_TIMES; attempt++) {
            inquiry.setInquiryNo(inquiryNoGenerator.generateInquiryNo());
            try {
                inquiryMapper.insert(inquiry);
                return;
            } catch (DuplicateKeyException exception) {
                log.warn("询盘编号冲突，准备重新生成后重试，尝试次数：{}，询盘编号：{}", attempt, inquiry.getInquiryNo());
            }
        }

        throw new IllegalStateException("询盘编号生成失败，重试次数已达上限");
    }

    private Long getCurrentUserId() {
        String loginId = (String) StpUtil.getLoginId();
        return Long.parseLong(loginId.replace("portal:", ""));
    }
}
