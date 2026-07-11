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
import com.indiestation.service.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    private static final Long ROOT_PARENT_ID = 0L;
    private static final int MAX_CATEGORY_LEVEL = 2;

    private final ProductMapper productMapper;
    private final R2Service r2Service;

    @Override
    public List<Category> getCategoryTree() {
        List<Category> allCategories = list(
                new LambdaQueryWrapper<Category>()
                        .orderByAsc(Category::getSort)
                        .orderByAsc(Category::getId)
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
        validateCategoryData(dto, null);
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
        String previousImage = category.getImage();

        validateCategoryData(dto, category);
        validateDuplicateCategoryName(dto);

        category.setName(dto.getName());
        category.setImage(dto.getImage());
        category.setParentId(dto.getParentId());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        updateById(category);

        deleteCategoryImageIfRemoved(previousImage, dto.getImage());
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

        String previousImage = category.getImage();
        removeById(id);
        deleteCategoryImageIfRemoved(previousImage, null);
    }

    /**
     * 校验分类基础数据
     */
    private void validateCategoryData(CategoryDTO dto, Category currentCategory) {
        Long parentId = dto.getParentId() == null ? ROOT_PARENT_ID : dto.getParentId();
        dto.setParentId(parentId);

        if (dto.getId() != null && Objects.equals(dto.getId(), parentId)) {
            throw new BusinessException("父级分类不能选择自己");
        }

        Category parentCategory = null;
        if (!Objects.equals(parentId, ROOT_PARENT_ID)) {
            parentCategory = getById(parentId);
            if (parentCategory == null) {
                throw new BusinessException("父级分类不存在");
            }

            if (dto.getId() != null && isDescendantCategory(parentId, dto.getId())) {
                throw new BusinessException("父级分类不能选择当前分类或其子分类");
            }

            int parentLevel = calculateCategoryLevel(parentCategory);
            if (parentLevel >= MAX_CATEGORY_LEVEL) {
                throw new BusinessException("商品分类最多只支持二级分类");
            }
        }

        int targetLevel = parentCategory == null ? 1 : calculateCategoryLevel(parentCategory) + 1;
        if (targetLevel > MAX_CATEGORY_LEVEL) {
            throw new BusinessException("商品分类最多只支持二级分类");
        }

        if (targetLevel == 1 && !StringUtils.hasText(dto.getImage())) {
            throw new BusinessException("顶级分类必须上传分类图片");
        }

        if (targetLevel > 1 && StringUtils.hasText(dto.getImage())) {
            throw new BusinessException("只有一级分类才能上传分类图片");
        }

        if (currentCategory != null
                && hasChildren(currentCategory.getId())
                && !Objects.equals(parentId, ROOT_PARENT_ID)) {
            throw new BusinessException("当前分类下存在子分类，不能调整为二级分类");
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

    /**
     * 计算分类层级，顶级分类为 1 级。
     */
    private int calculateCategoryLevel(Category category) {
        int level = 1;
        Category current = category;
        while (current != null && !Objects.equals(current.getParentId(), ROOT_PARENT_ID)) {
            current = getById(current.getParentId());
            level++;
            if (level > MAX_CATEGORY_LEVEL + 1) {
                break;
            }
        }
        return level;
    }

    /**
     * 判断目标父级是否为当前分类的子孙分类，避免形成循环树。
     */
    private boolean isDescendantCategory(Long targetParentId, Long currentCategoryId) {
        Category current = getById(targetParentId);
        while (current != null && !Objects.equals(current.getParentId(), ROOT_PARENT_ID)) {
            if (Objects.equals(current.getId(), currentCategoryId)) {
                return true;
            }
            current = getById(current.getParentId());
        }
        return current != null && Objects.equals(current.getId(), currentCategoryId);
    }

    /**
     * 判断当前分类下是否仍存在子分类。
     */
    private boolean hasChildren(Long categoryId) {
        return count(new LambdaQueryWrapper<Category>().eq(Category::getParentId, categoryId)) > 0;
    }

    /**
     * 分类保存或删除成功后，清理已不再被引用的旧图片。
     */
    private void deleteCategoryImageIfRemoved(String previousImage, String currentImage) {
        String normalizedPreviousImage = previousImage != null ? previousImage.trim() : "";
        String normalizedCurrentImage = currentImage != null ? currentImage.trim() : "";

        if (!StringUtils.hasText(normalizedPreviousImage)) {
            return;
        }

        if (normalizedPreviousImage.equals(normalizedCurrentImage)) {
            return;
        }

        r2Service.deleteFileByUrl(normalizedPreviousImage);
    }
}
