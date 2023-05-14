package com.hurried1y.config;

import lombok.*;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcServiceConfig<T> {
    //服务应用名称
    private String applicationName;
    private String host;
    private int port;
    private String group = "";
    private String version = "";
    private String serviceName;
    //target service
    private Object service;

    public String getRpcServiceName() {
        return getServiceName() + getGroup() + getVersion();
    }

    /**
     * 获取服务名称
     * @return 服务名称
     */
    public String getServiceName() {
        return service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
