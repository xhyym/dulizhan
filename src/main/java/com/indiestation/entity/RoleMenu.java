package com.indiestation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 角色-菜单关联实体
 *
 * @author IndieStation
 */
@Data
@TableName("t_role_menu")
public class RoleMenu {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码 */
    private String roleCode;

    /** 菜单ID */
    private Long menuId;
}
