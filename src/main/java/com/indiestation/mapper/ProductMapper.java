package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
