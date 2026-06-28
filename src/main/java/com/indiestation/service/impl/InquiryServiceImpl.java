package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.InquiryItemMapper;
import com.indiestation.mapper.InquiryMapper;
import com.indiestation.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
        return inquiryItemMapper.selectList(
                new LambdaQueryWrapper<InquiryItem>()
                        .eq(InquiryItem::getInquiryId, inquiryId)
        );
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
}
