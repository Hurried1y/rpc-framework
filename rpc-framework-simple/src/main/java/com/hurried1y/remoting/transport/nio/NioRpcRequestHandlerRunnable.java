package com.hurried1y.remoting.transport.nio;

import com.alibaba.fastjson.JSON;
import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import com.hurried1y.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * User：Hurried1y
 * Date：2023/4/24
 */
@Slf4j
public class NioRpcRequestHandlerRunnable implements Runnable{
    //请求处理器
    private final RpcRequestHandler rpcRequestHandler;
    private RpcRequest rpcRequest;
    private SocketChannel channel;

    public NioRpcRequestHandlerRunnable(RpcRequest rpcRequest, SocketChannel channel) {
        rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
        this.rpcRequest = rpcRequest;
        this.channel = channel;
    }


    @Override
    public void run() {
        Object result = rpcRequestHandler.handle(rpcRequest);
        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
//        rpcResponse.setAddr(rpcRequest.getAddr());
        result = rpcResponse;
        log.info("服务端处理请求: [{}] 结果: [{}]", rpcRequest, result);
        try {
            String json = JSON.toJSONString(result);
            channel.write(ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
