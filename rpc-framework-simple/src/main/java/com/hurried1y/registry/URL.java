package com.hurried1y.registry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * User：Hurried1y
 * Date：2023/5/11
 *
 * 消费者中引用的远程服务的基本信息
 * @ Reference 对应的远程服务的信息
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class URL {
    private String applicationName;
    private String serviceName;
    /**
     * 这里面可以自定义不限进行扩展
     * 分组
     * 权重
     * 服务提供者的地址
     * 服务提供者的端口
     */
    private Map<String, String> parameters = new HashMap<>();

    public void addParameter(String key, String value) {
        this.parameters.putIfAbsent(key, value);
    }
}
