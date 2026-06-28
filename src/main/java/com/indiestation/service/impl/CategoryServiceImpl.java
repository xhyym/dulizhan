package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.Category;
import com.indiestation.entity.Product;
import com.indiestation.entity.dto.CategoryDTO;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.CategoryMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final ProductMapper productMapper;

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = list(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
        );

        // 构建树形结构
        Map<Long, List<Category>> groupByParent = allCategories.stream()
                .collect(Collectors.groupingBy(Category::getParentId));

        return buildTree(groupByParent, 0L);
    }

    private List<Category> buildTree(Map<Long, List<Category>> groupByParent, Long parentId) {
        List<Category> children = groupByParent.getOrDefault(parentId, new ArrayList<>());
        for (Category child : children) {
            child.setChildren(buildTree(groupByParent, child.getId()));
        }
        return children;
    }

    @Override
    public void createCategory(CategoryDTO dto) {
        validateCategoryData(dto);
        validateDuplicateCategoryName(dto);

        Category category = new Category();
        category.setName(dto.getName());
        category.setImage(dto.getImage());
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

        validateCategoryData(dto);
        validateDuplicateCategoryName(dto);

        category.setName(dto.getName());
        category.setImage(dto.getImage());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否有子分类
        long childCount = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("请先删除子分类");
        }

        // 检查分类下是否还有商品
        Long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getCategoryId, id));
        if (productCount != null && productCount > 0) {
            throw new BusinessException("该分类下存在商品，无法删除");
        }

        removeById(id);
    }

    /**
     * 校验分类基础数据
     */
    private void validateCategoryData(CategoryDTO dto) {
        if (dto.getId() != null && Objects.equals(dto.getId(), dto.getParentId())) {
            throw new BusinessException("父级分类不能选择自己");
        }
    }

    /**
     * 校验同级分类名称是否重复
     */
    private void validateDuplicateCategoryName(CategoryDTO dto) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, dto.getParentId())
                .eq(Category::getName, dto.getName());

        if (dto.getId() != null) {
            queryWrapper.ne(Category::getId, dto.getId());
        }

        long sameLevelNameCount = count(queryWrapper);
        if (sameLevelNameCount > 0) {
            throw new BusinessException("同级分类名称已存在");
        }
    }
}
