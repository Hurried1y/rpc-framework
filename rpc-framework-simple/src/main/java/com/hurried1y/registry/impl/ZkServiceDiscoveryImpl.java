package com.hurried1y.registry.impl;

import com.hurried1y.enums.RpcErrorMessageEnum;
import com.hurried1y.exception.RpcException;
import com.hurried1y.loadbalance.LoadBalance;
import com.hurried1y.loadbalance.loadbalancer.RandomLoadBalance;
import com.hurried1y.registry.ServiceDiscovery;
import com.hurried1y.registry.impl.util.CuratorUtils;
import com.hurried1y.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.util.CollectionUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * User：Hurried1y
 * Date：2023/4/19
 * 基于Zookeeper的服务发现
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        //TODO 通过配置文件选择负载均衡策略
        loadBalance = new RandomLoadBalance();
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //获取当前节点下的所有子节点 -- 一个服务可以存在多个地址
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(CollectionUtils.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        //根据负载均衡策略选择一个地址
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("服务调用 - 成功发现服务地址: [{}]", targetServiceUrl);
        String[] socketAddress = targetServiceUrl.split(":");
        String host = socketAddress[0];
        int port = Integer.parseInt(socketAddress[1]);
        return new InetSocketAddress(host, port);
    }

    @Override
    public List<String> lookupService(String serviceName) {
        return CuratorUtils.getChildrenNodes(CuratorUtils.getZkClient(), serviceName);
    }
}
