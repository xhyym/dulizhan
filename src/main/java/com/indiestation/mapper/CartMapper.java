package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.Cart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 购物车 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
