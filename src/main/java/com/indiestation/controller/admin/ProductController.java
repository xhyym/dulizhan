package com.indiestation.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.indiestation.common.PageResult;
import com.indiestation.common.Result;
import com.indiestation.entity.Product;
import com.indiestation.entity.dto.ProductDTO;
import com.indiestation.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 商品分页列表
     */
    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {

        IPage<Product> page = productService.getProductPage(current, size, name, categoryId, status);
        PageResult<Product> result = new PageResult<>(
                page.getRecords(), page.getCurrent(), page.getSize(), page.getTotal()
        );
        return Result.success(result);
    }

    /**
     * 商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductDTO> detail(@PathVariable Long id) {
        ProductDTO dto = productService.getProductDetail(id);
        return Result.success(dto);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductDTO dto) {
        productService.createProduct(dto);
        return Result.success();
    }

    /**
     * 编辑商品
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto.setId(id);
        productService.updateProduct(dto);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
