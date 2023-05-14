package com.hurried1y.loadbalance;

import com.hurried1y.remoting.dto.RpcRequest;

import java.util.List;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
public interface LoadBalance {
    /**
     * 从服务地址列表中选择一个地址
     * @param serviceUrlList 服务地址列表
     * @param rpcRequest rpc请求
     * @return 服务地址
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
