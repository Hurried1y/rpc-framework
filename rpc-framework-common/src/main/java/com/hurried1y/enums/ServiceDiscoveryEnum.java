package com.hurried1y.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * User：Hurried1y
 * Date：2023/4/27
 */
@AllArgsConstructor
@Getter
public enum ServiceDiscoveryEnum {
    ZK("zk");

    private final String name;
}
