package com.hurried1y;

import com.hurried1y.annotation.RpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * User：Hurried1y
 * Date：2023/4/25
 */
@SpringBootApplication
@RpcScan(basePackage = {"com.hurried1y"})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
}
