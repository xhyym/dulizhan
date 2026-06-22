package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
}
