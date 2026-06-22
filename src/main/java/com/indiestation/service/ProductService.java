package com.indiestation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.indiestation.entity.Product;
import com.indiestation.entity.dto.ProductDTO;

/**
 * 商品服务
 *
 * @author IndieStation
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品
     */
    IPage<Product> getProductPage(int current, int size, String name, Long categoryId, Integer status);

    /**
     * 获取商品详情 (含副图、SKU)
     */
    ProductDTO getProductDetail(Long id);

    /**
     * 新增商品 (含副图、SKU)
     */
    void createProduct(ProductDTO dto);

    /**
     * 编辑商品 (含副图、SKU)
     */
    void updateProduct(ProductDTO dto);

    /**
     * 删除商品 (含副图、SKU)
     */
    void deleteProduct(Long id);
}
