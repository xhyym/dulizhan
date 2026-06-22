package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码 (如 R_SUPER, R_ADMIN) */
    private String roleCode;

    /** 角色名称 */
    private String roleName;

    /** 排序 */
    private Integer sort;

    /** 状态: 0-禁用, 1-正常 */
    private Integer status;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
