package com.hurried1y.impl;

import com.hurried1y.entity.User;
import com.hurried1y.service.UserService;
import com.hurried1y.annotation.RpcService;
import com.hurried1y.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User：Hurried1y
 * Date：2023/3/20
 */
@RpcService
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User getInfo(Integer userId) {
        return userMapper.getInfo(userId);
//        return new User(1, "小明", 1, 19);
    }
}
