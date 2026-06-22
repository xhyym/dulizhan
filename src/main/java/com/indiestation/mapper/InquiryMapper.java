package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.Inquiry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 询盘订单 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface InquiryMapper extends BaseMapper<Inquiry> {
}
