package com.hurried1y.provider;

import com.hurried1y.config.RpcServiceConfig;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
public interface ServiceProvider {
    /**
     * 增加服务
     * @param rpcServiceConfig 服务相关属性配置
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务
     * @param rpcServiceName 服务名称
     * @return 远程服务
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
