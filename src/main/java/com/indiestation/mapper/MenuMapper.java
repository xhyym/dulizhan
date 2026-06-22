package com.indiestation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.indiestation.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper
 *
 * @author IndieStation
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据角色编码查询菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM t_menu m " +
            "INNER JOIN t_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_code IN (${roleCodes}) " +
            "AND m.status = 1 " +
            "ORDER BY m.sort")
    List<Menu> selectMenusByRoleCodes(@Param("roleCodes") String roleCodes);
}
