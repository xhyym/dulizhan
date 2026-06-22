package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品SKU Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {
}
