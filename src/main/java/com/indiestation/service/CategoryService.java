package com.indiestation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.indiestation.entity.Category;
import com.indiestation.entity.dto.CategoryDTO;

import java.util.List;

/**
 * 商品分类服务
 *
 * @author IndieStation
 */
public interface CategoryService extends IService<Category> {

    /**
     * 获取分类树形列表
     */
    List<Category> getCategoryTree();

    /**
     * 新增分类
     */
    void createCategory(CategoryDTO dto);

    /**
     * 编辑分类
     */
    void updateCategory(CategoryDTO dto);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
