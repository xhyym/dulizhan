package com.indiestation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.indiestation.entity.Product;
import com.indiestation.entity.ProductImage;
import com.indiestation.entity.ProductSku;
import com.indiestation.entity.dto.ProductDTO;
import com.indiestation.exception.BusinessException;
import com.indiestation.mapper.ProductImageMapper;
import com.indiestation.mapper.ProductMapper;
import com.indiestation.mapper.ProductSkuMapper;
import com.indiestation.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现
 *
 * @author IndieStation
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductImageMapper productImageMapper;
    private final ProductSkuMapper productSkuMapper;

    @Override
    public IPage<Product> getProductPage(int current, int size, String name, Long categoryId, Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(name)) {
            wrapper.like(Product::getName, name);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
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
            skuDTO.setPrice(sku.getPrice());
            skuDTO.setStock(sku.getStock());
            skuDTO.setStatus(sku.getStatus());
            return skuDTO;
        }).toList());

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createProduct(ProductDTO dto) {
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
        product.setStatus(dto.getStatus());
        product.setSort(dto.getSort());
        save(product);

        // 保存副图
        saveProductImages(product.getId(), dto.getImages());

        // 保存SKU
        saveProductSkus(product.getId(), dto.getSkus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO dto) {
        Product product = getById(dto.getId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 更新主表
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setDiscountPrice(dto.getDiscountPrice());
        product.setSkuCode(dto.getSkuCode());
        product.setMainImage(dto.getMainImage());
        product.setPosterImage(dto.getPosterImage());
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
        saveProductSkus(product.getId(), dto.getSkus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
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
    private void saveProductSkus(Long productId, List<ProductDTO.SkuDTO> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        for (ProductDTO.SkuDTO skuDTO : skus) {
            ProductSku sku = new ProductSku();
            sku.setProductId(productId);
            sku.setSkuCode(skuDTO.getSkuCode());
            sku.setSpecName(skuDTO.getSpecName());
            sku.setSpecValue(skuDTO.getSpecValue());
            sku.setPrice(skuDTO.getPrice());
            sku.setStock(skuDTO.getStock());
            sku.setStatus(skuDTO.getStatus());
            productSkuMapper.insert(sku);
        }
    }
}
