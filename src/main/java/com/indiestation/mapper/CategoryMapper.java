package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
