package com.hurried1y.registry;

import java.net.InetSocketAddress;

/**
 * User：Hurried1y
 * Date：2023/4/19
 * 服务注册接口
 */
public interface ServiceRegistry {
    /**
     * 注册服务到注册中心
     * @param rpcServiceName 完整的服务名称(class name + group + version)
     * @param inetSocketAddress 远程服务地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

    /**
     * 订阅服务
     * @param url 标注了@RpcReference属性 -- 客户端信息
     */
    void subscribe(URL url);

}
