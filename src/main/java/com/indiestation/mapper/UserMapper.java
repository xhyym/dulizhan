package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门户用户 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
