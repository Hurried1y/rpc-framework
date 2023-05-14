package com.hurried1y.service;
import com.hurried1y.entity.Order;
import com.hurried1y.entity.User;

/**
 * User：Hurried1y
 * Date：2023/3/20
 */
public interface OrderService {
    Order getInfo(String orderSn);
    void addOrder(Order order);
    User getUserInfoByOrderSn(String orderSn);
}
