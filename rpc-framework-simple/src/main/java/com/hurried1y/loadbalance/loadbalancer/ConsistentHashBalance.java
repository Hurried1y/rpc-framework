package com.hurried1y.loadbalance.loadbalancer;

import com.hurried1y.loadbalance.AbstractLoadBalance;
import com.hurried1y.remoting.dto.RpcRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User：Hurried1y
 * Date：2023/4/19
 * refer to dubbo consistent hash load balance
 */
public class ConsistentHashBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceUrlList);
        //通过 rpcRequest 创建 rpc service name
        String rpcServiceName = rpcRequest.getRpcServiceName();
        //获取一致性hash选择器
        ConsistentHashSelector selector = selectors.get(rpcServiceName);

        if(selector == null || selector.identityHashCode != identityHashCode){
            //如果不存在，或者已经存在，但是服务提供者列表发生了变化，那么需要重新创建
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceUrlList, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;
        private final int identityHashCode;

        /**
         * @param invokers 服务提供者列表
         * @param replicaNumber 虚拟节点数
         * @param identityHashCode 用于区分相同服务名的不同服务提供者
         */
        public ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            //创建虚拟节点
            for(String invoker : invokers){
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey){
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        private String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();
            if(entry == null){
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }
    }
}
