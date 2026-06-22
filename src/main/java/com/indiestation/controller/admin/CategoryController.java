package com.indiestation.controller.admin;

import com.indiestation.common.Result;
import com.indiestation.entity.Category;
import com.indiestation.entity.dto.CategoryDTO;
import com.indiestation.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author IndieStation
 */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类列表 (树形)
     */
    @GetMapping
    public Result<List<Category>> list() {
        List<Category> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> create(@Valid @RequestBody CategoryDTO dto) {
        categoryService.createCategory(dto);
        return Result.success();
    }

    /**
     * 编辑分类
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        dto.setId(id);
        categoryService.updateCategory(dto);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
