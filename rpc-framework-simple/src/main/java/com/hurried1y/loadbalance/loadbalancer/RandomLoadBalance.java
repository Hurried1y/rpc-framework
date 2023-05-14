package com.hurried1y.loadbalance.loadbalancer;

import com.hurried1y.loadbalance.AbstractLoadBalance;
import com.hurried1y.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

/**
 * User：Hurried1y
 * Date：2023/4/19
 * Implementation of random load balancing strategy
 */
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
