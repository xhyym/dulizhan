package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.Category;
import com.indiestation.entity.dto.CategoryDTO;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.CategoryMapper;
import com.indiestation.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 *
 * @author IndieStation
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = list(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
        );

        // 构建树形结构
        Map<Long, List<Category>> groupByParent = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> tree = new ArrayList<>();
        buildTree(tree, groupByParent, 0L);
        return tree;
    }

    private void buildTree(List<Category> result, Map<Long, List<Category>> groupByParent, Long parentId) {
        List<Category> children = groupByParent.getOrDefault(parentId, new ArrayList<>());
        for (Category child : children) {
            result.add(child);
            buildTree(result, groupByParent, child.getId());
        }
    }

    @Override
    public void createCategory(CategoryDTO dto) {
        // 检查同级分类名称是否重复
        long count = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, dto.getParentId())
                .eq(Category::getName, dto.getName()));
        if (count > 0) {
            throw new BusinessException("同级分类名称已存在");
        }

        Category category = new Category();
        category.setName(dto.getName());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        save(category);
    }

    @Override
    public void updateCategory(CategoryDTO dto) {
        Category category = getById(dto.getId());
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        category.setName(dto.getName());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        long childCount = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除子分类");
        }

        removeById(id);
    }
}
