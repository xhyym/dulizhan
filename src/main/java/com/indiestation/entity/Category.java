package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 分类图片URL */
    private String image;

    /** 父分类ID (0为顶级) */
    private Long parentId;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 子分类列表（非数据库字段） */
    @TableField(exist = false)
    private List<Category> children;
}
