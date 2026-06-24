package com.indiestation.config;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 *
 * @author IndieStation
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回指定账号 id 所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>();
    }

    /**
     * 返回指定账号 id 所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>();
    }
}
