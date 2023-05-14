package com.hurried1y.registry.impl;

import com.hurried1y.registry.URL;
import com.hurried1y.registry.ServiceRegistry;

import com.hurried1y.registry.impl.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

import static com.hurried1y.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;

/**
 * User：Hurried1y
 * Date：2023/4/19
 * 基于Zookeeper的服务注册
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        /*
        * 注册服务节点到Zookeeper
        * /my-rpc/com.example.service.UserService/192.168.43.178:9998
        * */
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createTemporaryNode(zkClient, servicePath);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url);
    }

}
