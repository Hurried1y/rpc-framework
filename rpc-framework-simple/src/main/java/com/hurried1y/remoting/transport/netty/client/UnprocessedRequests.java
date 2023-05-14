package com.hurried1y.remoting.transport.netty.client;

import com.hurried1y.remoting.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User：Hurried1y
 * Date：2023/4/27
 * 未处理的请求
 */
public class UnprocessedRequests {
    //未处理的请求
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>> future) {
        UNPROCESSED_RESPONSE_FUTURES.put(requestId, future);
    }

    /**
     * 处理响应
     * @param rpcResponse 响应
     */
    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (future != null){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException();
        }
    }
}
