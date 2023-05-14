package com.hurried1y.provider.impl;

import com.hurried1y.enums.RpcErrorMessageEnum;
import com.hurried1y.exception.RpcException;
import com.hurried1y.config.RpcServiceConfig;
import com.hurried1y.provider.ServiceProvider;
import com.hurried1y.registry.ServiceRegistry;
import com.hurried1y.registry.impl.ZkServiceRegistryImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.hurried1y.cache.CommonServerCache.SERVICE_MAP;


/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {

    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl() {
        serviceRegistry = new ZkServiceRegistryImpl();
    }

    /**
     * 增加服务
     * @param rpcServiceConfig 服务相关属性配置
     */
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        Object service = rpcServiceConfig.getService();
        //将服务名称和服务对象放入map中
        if(SERVICE_MAP.containsKey(rpcServiceName)){
            return;
        }
        SERVICE_MAP.put(rpcServiceName, service);
        log.info("服务注册 - 成功注册服务: {} 和接口: {}", rpcServiceName, service.getClass().getInterfaces());
    }

    /**
     * 获取服务
     * @param rpcServiceName 服务名称
     * @return 远程服务
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = SERVICE_MAP.get(rpcServiceName);
        if(service == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    /**
     * 发布服务
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        String host = rpcServiceConfig.getHost();
        Integer port = rpcServiceConfig.getPort();
        addService(rpcServiceConfig);
        serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, port));
    }
}
