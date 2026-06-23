package com.indiestation.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.indiestation.entity.User;

/**
 * 门户用户服务
 *
 * @author IndieStation
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询客户
     */
    IPage<User> getUserPage(int current, int size, String username, String email, Integer status);

    /**
     * 获取客户详情
     */
    User getUserDetail(Long id);
}
