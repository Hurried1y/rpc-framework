package com.hurried1y.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * User：Hurried1y
 * Date：2023/3/20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order implements Serializable {
    private String OrderSn;
    private Integer userId;
    private String productName;
}
