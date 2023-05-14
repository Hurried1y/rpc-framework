package com.hurried1y.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User：Hurried1y
 * Date：2023/5/11
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClientConfig {
    private String applicationName;
    private String registerAddr;
    private String registerType;

    /**
     * 代理类型 example: jdk,javassist
     */
    private String proxyType;

    /**
     * 负载均衡策略 example:random,rotate
     */
    private String routerStrategy;

    /**
     * 客户端序列化方式 example: hession2,kryo,jdk,fastjson
     */
    private String clientSerialize;
}
