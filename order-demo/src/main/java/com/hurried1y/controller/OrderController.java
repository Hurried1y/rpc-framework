package com.hurried1y.controller;

import com.hurried1y.entity.Order;
import com.hurried1y.entity.User;
import com.hurried1y.service.OrderService;
import com.hurried1y.annotation.RpcReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User：Hurried1y
 * Date：2023/3/20
 */
@RestController
@RequestMapping("/order")
public class OrderController {
    @RpcReference
    private OrderService orderService;

    @GetMapping("/info/{orderSn}")
    public Order getInfo(@PathVariable("orderSn") String orderSn){
        return orderService.getInfo(orderSn);
    }

    @GetMapping("/userInfo/{orderSn}")
    public User getUserInfoByOrderSn(@PathVariable("orderSn") String orderSn){
        return orderService.getUserInfoByOrderSn(orderSn);
    }

}
