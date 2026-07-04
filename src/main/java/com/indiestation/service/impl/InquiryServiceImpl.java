package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;
import com.indiestation.entity.Product;
import com.indiestation.entity.ProductSku;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.InquiryItemMapper;
import com.indiestation.mapper.InquiryMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.mapper.ProductSkuMapper;
import com.indiestation.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 询盘服务实现
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl extends ServiceImpl<InquiryMapper, Inquiry> implements InquiryService {

    private final InquiryItemMapper inquiryItemMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductMapper productMapper;

    @Override
    public IPage<Inquiry> getInquiryPage(int current, int size, String inquiryNo,
                                          String userName, Integer status) {
        LambdaQueryWrapper<Inquiry> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(inquiryNo)) {
            wrapper.like(Inquiry::getInquiryNo, inquiryNo);
        }
        if (StringUtils.hasText(userName)) {
            wrapper.like(Inquiry::getUserName, userName);
        }
        if (status != null) {
            wrapper.eq(Inquiry::getStatus, status);
        }

        wrapper.orderByDesc(Inquiry::getCreateTime);
        IPage<Inquiry> inquiryPage = page(new Page<>(current, size), wrapper);
        fillInquiryTotalQuantity(inquiryPage.getRecords());
        return inquiryPage;
    }

    @Override
    public Inquiry getInquiryDetail(Long id) {
        Inquiry inquiry = getById(id);
        if (inquiry == null) {
            throw new BusinessException("询盘不存在");
        }
        inquiry.setItems(getInquiryItems(id));
        inquiry.setTotalQuantity(calculateTotalQuantity(inquiry.getItems()));
        return inquiry;
    }

    @Override
    public List<InquiryItem> getInquiryItems(Long inquiryId) {
        List<InquiryItem> inquiryItems = inquiryItemMapper.selectList(
                new LambdaQueryWrapper<InquiryItem>()
                        .eq(InquiryItem::getInquiryId, inquiryId)
        );
        fillInquiryItemSkuCode(inquiryItems);
        return inquiryItems;
    }

    @Override
    public List<Inquiry> getInquiriesByUserId(Long userId) {
        return list(
                new LambdaQueryWrapper<Inquiry>()
                        .eq(Inquiry::getUserId, userId)
                        .orderByDesc(Inquiry::getCreateTime)
        );
    }

    @Override
    public IPage<Inquiry> getPortalInquiryPage(Long userId, int current, int size, String inquiryNo) {
        LambdaQueryWrapper<Inquiry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inquiry::getUserId, userId);

        if (StringUtils.hasText(inquiryNo)) {
            wrapper.like(Inquiry::getInquiryNo, inquiryNo.trim());
        }

        wrapper.orderByDesc(Inquiry::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status, String adminRemark) {
        Inquiry inquiry = getById(id);
        if (inquiry == null) {
            throw new BusinessException("询盘不存在");
        }

        validateStatusTransition(inquiry.getStatus(), status);
        inquiry.setStatus(status);
        inquiry.setAdminRemark(StringUtils.hasText(adminRemark) ? adminRemark.trim() : null);
        updateById(inquiry);
    }

    @Override
    public void validateStatusTransition(Integer currentStatus, Integer targetStatus) {
        if (targetStatus == null) {
            throw new BusinessException("询盘状态不能为空");
        }
        if (currentStatus == null) {
            throw new BusinessException("当前询盘状态异常");
        }
        if (currentStatus.equals(targetStatus)) {
            return;
        }

        boolean isValid = switch (currentStatus) {
            case 0 -> targetStatus == 1 || targetStatus == 3;
            case 1 -> targetStatus == 2 || targetStatus == 3;
            case 2, 3 -> false;
            default -> false;
        };

        if (!isValid) {
            throw new BusinessException("当前询盘状态不允许执行该操作");
        }
    }

    @Override
    public void cancelInquiryByUser(Long inquiryId, Long userId) {
        Inquiry inquiry = getById(inquiryId);
        if (inquiry == null || userId == null || !userId.equals(inquiry.getUserId())) {
            throw new BusinessException("Inquiry not found.");
        }
        if (inquiry.getStatus() == null) {
            throw new BusinessException("Inquiry status is invalid.");
        }
        if (!Integer.valueOf(0).equals(inquiry.getStatus())) {
            throw new BusinessException("Only pending inquiries can be cancelled.");
        }

        inquiry.setStatus(3);
        updateById(inquiry);
    }

    /**
     * 回填询盘商品总数量
     */
    private void fillInquiryTotalQuantity(List<Inquiry> inquiries) {
        if (inquiries == null || inquiries.isEmpty()) {
            return;
        }

        List<Long> inquiryIds = inquiries.stream()
                .map(Inquiry::getId)
                .toList();

        Map<Long, Integer> inquiryQuantityMap = inquiryItemMapper.selectList(
                        new LambdaQueryWrapper<InquiryItem>().in(InquiryItem::getInquiryId, inquiryIds)
                ).stream()
                .collect(Collectors.groupingBy(
                        InquiryItem::getInquiryId,
                        Collectors.summingInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                ));

        for (Inquiry inquiry : inquiries) {
            inquiry.setTotalQuantity(inquiryQuantityMap.getOrDefault(inquiry.getId(), 0));
        }
    }

    /**
     * 统计单条询盘商品总数量
     */
    private Integer calculateTotalQuantity(List<InquiryItem> items) {
        List<InquiryItem> inquiryItems = items == null ? Collections.emptyList() : items;
        return inquiryItems.stream()
                .map(InquiryItem::getQuantity)
                .filter(quantity -> quantity != null && quantity > 0)
                .reduce(0, Integer::sum);
    }

    /**
     * 回填询盘商品的 SKU 编码，便于后台详情页直接展示。
     */
    private void fillInquiryItemSkuCode(List<InquiryItem> inquiryItems) {
        if (inquiryItems == null || inquiryItems.isEmpty()) {
            return;
        }

        List<Long> skuIds = inquiryItems.stream()
                .map(InquiryItem::getSkuId)
                .filter(skuId -> skuId != null && skuId > 0)
                .distinct()
                .toList();

        Map<Long, String> skuCodeMap = skuIds.isEmpty()
                ? Collections.emptyMap()
                : productSkuMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(ProductSku::getId, ProductSku::getSkuCode, (left, right) -> left));

        List<Long> productIds = inquiryItems.stream()
                .map(InquiryItem::getProductId)
                .filter(productId -> productId != null && productId > 0)
                .distinct()
                .toList();

        Map<Long, String> productSkuCodeMap = productIds.isEmpty()
                ? Collections.emptyMap()
                : productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getSkuCode, (left, right) -> left));

        for (InquiryItem inquiryItem : inquiryItems) {
            String skuCode = inquiryItem.getSkuId() != null
                    ? skuCodeMap.get(inquiryItem.getSkuId())
                    : null;
            if (!StringUtils.hasText(skuCode)) {
                skuCode = productSkuCodeMap.get(inquiryItem.getProductId());
            }
            inquiryItem.setSkuCode(StringUtils.hasText(skuCode) ? skuCode : null);
        }
    }
}
