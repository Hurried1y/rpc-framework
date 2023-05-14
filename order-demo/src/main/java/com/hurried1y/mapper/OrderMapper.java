package com.hurried1y.mapper;

import com.hurried1y.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * User：Hurried1y
 * Date：2023/5/4
 */
@Mapper
public interface OrderMapper  {
    Order getOrderByOrderSn(String orderSn);

    void save(Order order);
}
