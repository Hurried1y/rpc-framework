package com.hurried1y.stub;

import com.hurried1y.entity.User;
import com.hurried1y.service.UserService;

/**
 * User：Hurried1y
 * Date：2023/4/30
 */
public class UserServiceStub implements UserService {
    //远程接口的本地存根
    private final UserService userService;

    public UserServiceStub(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User getInfo(Integer userId) {
        System.out.println("UserService本地存根执行...");
        return userService.getInfo(userId);
    }
}
