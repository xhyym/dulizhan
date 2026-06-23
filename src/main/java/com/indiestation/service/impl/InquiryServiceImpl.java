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

import java.util.List;

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
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public Inquiry getInquiryDetail(Long id) {
        return getById(id);
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

        inquiry.setStatus(status);
        if (StringUtils.hasText(adminRemark)) {
            inquiry.setAdminRemark(adminRemark);
        }
        updateById(inquiry);
    }
}
