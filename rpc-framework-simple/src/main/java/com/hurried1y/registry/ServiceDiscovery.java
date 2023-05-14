package com.hurried1y.registry;


import com.hurried1y.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * User：Hurried1y
 * Date：2023/4/19
 * 服务发现接口
 */
public interface ServiceDiscovery {
    /**
     * 根据 rpcServiceName 获取远程服务地址
     * 涉及到负载均衡
     * @param rpcRequest rpc请求
     * @return 远程服务地址
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);

    /**
     * 根据serviceName去获取所有的远程服务地址
     * 还不涉及负载均衡
     * @param serviceName
     * @return
     */
    List<String> lookupService(String serviceName);
}
