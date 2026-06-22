package com.indiestation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.indiestation.entity.Inquiry;

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
     * 更新询盘状态
     */
    void updateStatus(Long id, Integer status, String adminRemark);
}
