package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
