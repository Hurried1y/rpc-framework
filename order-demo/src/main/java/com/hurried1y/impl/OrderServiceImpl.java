package com.hurried1y.impl;

import com.hurried1y.entity.Order;
import com.hurried1y.entity.User;
import com.hurried1y.service.OrderService;
import com.hurried1y.service.UserService;
import com.hurried1y.mapper.OrderMapper;
import com.hurried1y.annotation.RpcReference;
import com.hurried1y.annotation.RpcService;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * User：Hurried1y
 * Date：2023/3/20
 *
 */
@RpcService
public class OrderServiceImpl implements OrderService {
    @RpcReference
    private UserService userService;
//    @Autowired
//    private OrderMapper orderMapper;

    @Override
    public Order getInfo(String orderSn) {
//        return orderMapper.getOrderByOrderSn(orderSn);
        return new Order("123", 1, "小米");
    }

    @Override
    public void addOrder(Order order) {
//        orderMapper.save(order);
    }

    @Override
    public User getUserInfoByOrderSn(String orderSn) {
//        Order order = orderMapper.getOrderByOrderSn(orderSn);
//        Integer userId = order.getUserId();
        return userService.getInfo(1);
    }
}
