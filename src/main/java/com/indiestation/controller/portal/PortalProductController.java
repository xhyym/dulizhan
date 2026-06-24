package com.indiestation.controller.portal;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.indiestation.common.PageResult;
import com.indiestation.common.Result;
import com.indiestation.entity.*;
import com.indiestation.entity.dto.ProductDTO;
import com.indiestation.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 门户端商品控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalProductController {

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSkuMapper productSkuMapper;

    /**
     * 获取分类树
     */
    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort));
        return Result.success(buildTree(all, 0L));
    }

    /**
     * 商品列表（分页 + 分类筛选 + 关键词搜索）
     */
    @GetMapping("/products")
    public Result<PageResult<Product>> products(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0);

        if (categoryId != null) {
            List<Long> categoryIds = getAllChildCategoryIds(categoryId);
            categoryIds.add(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getCreateTime);

        IPage<Product> page = productMapper.selectPage(new Page<>(current, size), wrapper);
        return Result.success(new PageResult<>(page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal()));
    }

    /**
     * 商品详情（含图片、SKU）
     */
    @GetMapping("/products/{id}")
    public Result<ProductDTO> productDetail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() != 1) {
            return Result.error("商品不存在");
        }

        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategoryId());
        dto.setPrice(product.getPrice());
        dto.setDiscountPrice(product.getDiscountPrice());
        dto.setSkuCode(product.getSkuCode());
        dto.setMainImage(product.getMainImage());
        dto.setPosterImage(product.getPosterImage());
        dto.setDetailImage(product.getDetailImage());
        dto.setStatus(product.getStatus());
        dto.setSort(product.getSort());

        // 副图
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort));
        dto.setImages(images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList()));

        // SKU
        List<ProductSku> skus = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
                        .eq(ProductSku::getStatus, 1));
        dto.setSkus(skus.stream().map(sku -> {
            ProductDTO.SkuDTO skuDTO = new ProductDTO.SkuDTO();
            skuDTO.setId(sku.getId());
            skuDTO.setSkuCode(sku.getSkuCode());
            skuDTO.setSpecName(sku.getSpecName());
            skuDTO.setSpecValue(sku.getSpecValue());
            skuDTO.setPrice(sku.getPrice());
            skuDTO.setStock(sku.getStock());
            skuDTO.setStatus(sku.getStatus());
            return skuDTO;
        }).collect(Collectors.toList()));

        return Result.success(dto);
    }

    /**
     * 最新上架商品（首页用）
     */
    @GetMapping("/products/new")
    public Result<List<Product>> newProducts(@RequestParam(defaultValue = "8") int limit) {
        List<Product> products = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getStatus, 1)
                        .eq(Product::getDeleted, 0)
                        .orderByDesc(Product::getCreateTime)
                        .last("LIMIT " + limit));
        return Result.success(products);
    }

    private List<Long> getAllChildCategoryIds(Long parentId) {
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1));
        List<Long> result = new ArrayList<>();
        collectChildIds(all, parentId, result);
        return result;
    }

    private void collectChildIds(List<Category> all, Long parentId, List<Long> result) {
        for (Category c : all) {
            if (parentId.equals(c.getParentId())) {
                result.add(c.getId());
                collectChildIds(all, c.getId(), result);
            }
        }
    }

    private List<Category> buildTree(List<Category> all, Long parentId) {
        List<Category> children = new ArrayList<>();
        for (Category c : all) {
            if (parentId.equals(c.getParentId())) {
                c.setChildren(buildTree(all, c.getId()));
                children.add(c);
            }
        }
        return children;
    }
}
