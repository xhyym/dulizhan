package com.indiestation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.indiestation.entity.Inquiry;
import com.indiestation.entity.InquiryItem;

import java.util.List;

/**
 * 询盘服务
 *
 * @author IndieStation
 */
public interface InquiryService extends IService<Inquiry> {

    /**
     * 分页查询询盘
     */
    IPage<Inquiry> getInquiryPage(int current, int size, String inquiryNo,
                                   String userName, Integer status);

    /**
     * 获取询盘详情 (含商品明细)
     */
    Inquiry getInquiryDetail(Long id);

    /**
     * 获取询盘商品明细
     */
    List<InquiryItem> getInquiryItems(Long inquiryId);

    /**
     * 根据用户ID获取询盘列表
     */
    List<Inquiry> getInquiriesByUserId(Long userId);

    /**
     * 更新询盘状态
     */
    void updateStatus(Long id, Integer status, String adminRemark);

    /**
     * 校验询盘状态流转是否合法
     */
    void validateStatusTransition(Integer currentStatus, Integer targetStatus);
}
