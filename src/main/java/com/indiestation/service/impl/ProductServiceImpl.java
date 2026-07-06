package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.Category;
import com.indiestation.entity.Product;
import com.indiestation.entity.ProductImage;
import com.indiestation.entity.ProductSku;
import com.indiestation.entity.dto.ProductDTO;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.CategoryMapper;
import com.indiestation.mapper.ProductImageMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.mapper.ProductSkuMapper;
import com.indiestation.service.ProductService;
import com.indiestation.service.R2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 商品服务实现
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final CategoryMapper categoryMapper;
    private final ProductImageMapper productImageMapper;
    private final ProductSkuMapper productSkuMapper;
    private final R2Service r2Service;

    @Override
    public IPage<Product> getProductPage(int current, int size, String name, Long categoryId, Integer status, String skuCode, String startTime, String endTime) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (categoryId != null) {
            List<Long> categoryIds = getAllChildCategoryIds(categoryId);
            categoryIds.add(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        if (StringUtils.hasText(skuCode)) {
            wrapper.like(Product::getSkuCode, skuCode);
        }
        if (StringUtils.hasText(startTime)) {
            LocalDateTime start = LocalDate.parse(startTime, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            wrapper.ge(Product::getCreateTime, start);
        }
        if (StringUtils.hasText(endTime)) {
            LocalDateTime end = LocalDate.parse(endTime, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX);
            wrapper.le(Product::getCreateTime, end);
        }

        wrapper.orderByDesc(Product::getCreateTime);
        return page(new Page<>(current, size), wrapper);
    }

    @Override
    public ProductDTO getProductDetail(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
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

        // 查询副图
        List<ProductImage> images = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort)
        );
        dto.setImages(images.stream()
                .map(ProductImage::getImageUrl)
                .toList());

        // 查询SKU
        List<ProductSku> skus = productSkuMapper.selectList(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
        );
        dto.setSkus(skus.stream().map(sku -> {
            ProductDTO.SkuDTO skuDTO = new ProductDTO.SkuDTO();
            skuDTO.setId(sku.getId());
            skuDTO.setSkuCode(sku.getSkuCode());
            skuDTO.setSpecName(sku.getSpecName());
            skuDTO.setSpecValue(sku.getSpecValue());
            skuDTO.setPrice(product.getPrice());
            skuDTO.setStock(sku.getStock());
            skuDTO.setStatus(sku.getStatus());
            return skuDTO;
        }).toList());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductDTO dto) {
        validateProductData(dto);

        // 保存商品主表
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setSkuCode(dto.getSkuCode());
        product.setMainImage(dto.getMainImage());
        product.setPosterImage(dto.getPosterImage());
        product.setDetailImage(dto.getDetailImage());
        product.setStatus(dto.getStatus());
        product.setSort(dto.getSort());
        save(product);

        // 保存副图
        saveProductImages(product.getId(), dto.getImages());

        // 保存SKU
        saveProductSkus(product.getId(), dto.getPrice(), dto.getSkus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO dto) {
        Product product = getById(dto.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        List<ProductImage> existingImages = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, product.getId())
                        .orderByAsc(ProductImage::getSort)
        );
        Set<String> previousFileUrls = collectProductFileUrls(
                product.getMainImage(),
                product.getPosterImage(),
                product.getDetailImage(),
                existingImages.stream().map(ProductImage::getImageUrl).toList()
        );

        validateProductData(dto);

        // 更新主表
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setSkuCode(dto.getSkuCode());
        product.setMainImage(dto.getMainImage());
        product.setPosterImage(dto.getPosterImage());
        product.setDetailImage(dto.getDetailImage());
        product.setStatus(dto.getStatus());
        product.setSort(dto.getSort());
        updateById(product);

        // 删除旧副图，重新保存
        productImageMapper.delete(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, product.getId())
        );
        saveProductImages(product.getId(), dto.getImages());

        // 删除旧SKU，重新保存
        productSkuMapper.delete(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, product.getId())
        );
        saveProductSkus(product.getId(), dto.getPrice(), dto.getSkus());

        Set<String> currentFileUrls = collectProductFileUrls(
                dto.getMainImage(),
                dto.getPosterImage(),
                dto.getDetailImage(),
                dto.getImages()
        );
        deleteRemovedFiles(previousFileUrls, currentFileUrls);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        Product product = getById(id);
        if (product == null) {
            return;
        }

        List<ProductImage> existingImages = productImageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
        );

        // 删除商品
        removeById(id);
        // 删除副图
        productImageMapper.delete(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, id)
        );
        // 删除SKU
        productSkuMapper.delete(
                new LambdaQueryWrapper<ProductSku>()
                        .eq(ProductSku::getProductId, id)
        );

        deleteRemovedFiles(
                collectProductFileUrls(
                        product.getMainImage(),
                        product.getPosterImage(),
                        product.getDetailImage(),
                        existingImages.stream().map(ProductImage::getImageUrl).toList()
                ),
                Set.of()
        );
    }

    /**
     * 统一校验商品核心图片字段，避免前后台规则不一致。
     */
    private void validateProductData(ProductDTO dto) {
        validateUniqueProductSkuCode(dto);

        if (!StringUtils.hasText(dto.getMainImage())) {
            throw new BusinessException("商品主图不能为空");
        }
        if (dto.getImages() == null || dto.getImages().isEmpty()) {
            throw new BusinessException("商品副图不能为空");
        }
        if (!StringUtils.hasText(dto.getPosterImage())) {
            throw new BusinessException("商品海报图不能为空");
        }
        if (!StringUtils.hasText(dto.getDetailImage())) {
            throw new BusinessException("商品详情图不能为空");
        }
    }

    /**
     * 校验商品主 SKU 编码唯一，避免后台新增或编辑后出现重复编码。
     */
    private void validateUniqueProductSkuCode(ProductDTO dto) {
        String skuCode = dto.getSkuCode() != null ? dto.getSkuCode().trim() : "";
        if (!StringUtils.hasText(skuCode)) {
            throw new BusinessException("SKU编码不能为空");
        }

        dto.setSkuCode(skuCode);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getSkuCode, skuCode);
        if (dto.getId() != null) {
            queryWrapper.ne(Product::getId, dto.getId());
        }

        long duplicateCount = count(queryWrapper);
        if (duplicateCount > 0) {
            throw new BusinessException("SKU编码已存在，请更换后重试");
        }
    }

    /**
     * 保存商品副图
     */
    private void saveProductImages(Long productId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(productId);
            image.setImageUrl(images.get(i));
            image.setSort(i);
            productImageMapper.insert(image);
        }
    }

    /**
     * 保存商品SKU
     */
    private void saveProductSkus(Long productId, java.math.BigDecimal productPrice, List<ProductDTO.SkuDTO> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        for (ProductDTO.SkuDTO skuDTO : skus) {
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSkuCode(skuDTO.getSkuCode());
            sku.setSpecName(skuDTO.getSpecName());
            sku.setSpecValue(skuDTO.getSpecValue());
            sku.setPrice(productPrice);
            sku.setStock(skuDTO.getStock());
            sku.setStatus(skuDTO.getStatus());
            productSkuMapper.insert(sku);
        }
    }

    /**
     * 收集商品当前实际引用的全部图片地址，便于后续统一做 R2 清理。
     */
    private Set<String> collectProductFileUrls(String mainImage, String posterImage, String detailImage, List<String> images) {
        Set<String> fileUrls = new LinkedHashSet<>();
        if (StringUtils.hasText(mainImage)) {
            fileUrls.add(mainImage.trim());
        }
        if (StringUtils.hasText(posterImage)) {
            fileUrls.add(posterImage.trim());
        }
        if (StringUtils.hasText(detailImage)) {
            fileUrls.add(detailImage.trim());
        }
        if (images != null) {
            for (String imageUrl : images) {
                if (StringUtils.hasText(imageUrl)) {
                    fileUrls.add(imageUrl.trim());
                }
            }
        }
        return fileUrls;
    }

    /**
     * 删除已不再被商品引用的 R2 图片文件。
     */
    private void deleteRemovedFiles(Set<String> previousFileUrls, Set<String> currentFileUrls) {
        if (previousFileUrls == null || previousFileUrls.isEmpty()) {
            return;
        }

        Set<String> removedFileUrls = new LinkedHashSet<>(previousFileUrls);
        if (currentFileUrls != null && !currentFileUrls.isEmpty()) {
            removedFileUrls.removeAll(currentFileUrls);
        }

        for (String removedFileUrl : removedFileUrls) {
            r2Service.deleteFileByUrl(removedFileUrl);
        }
    }

    /**
     * 递归收集当前分类下的所有子分类ID
     */
    private List<Long> getAllChildCategoryIds(Long parentId) {
        List<Category> allCategories = categoryMapper.selectList(new LambdaQueryWrapper<>());
        List<Long> categoryIds = new ArrayList<>();
        collectChildIds(allCategories, parentId, categoryIds);
        return categoryIds;
    }

    /**
     * 递归遍历分类树，收集子分类ID
     */
    private void collectChildIds(List<Category> allCategories, Long parentId, List<Long> categoryIds) {
        for (Category category : allCategories) {
            if (parentId.equals(category.getParentId())) {
                categoryIds.add(category.getId());
                collectChildIds(allCategories, category.getId(), categoryIds);
            }
        }
    }
}
