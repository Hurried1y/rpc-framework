package com.hurried1y.loadbalance;

import com.hurried1y.remoting.dto.RpcRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if(CollectionUtils.isEmpty(serviceUrlList)){
            return null;
        }
        if(serviceUrlList.size() == 1){
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest);
}
