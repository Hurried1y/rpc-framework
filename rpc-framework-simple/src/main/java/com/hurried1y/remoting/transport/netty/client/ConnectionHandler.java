package com.hurried1y.remoting.transport.netty.client;

import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.remoting.dto.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.protostuff.Rpc;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.hurried1y.cache.CommonClientCache.*;

/**
 * User：Hurried1y
 * Date：2023/5/12
 *
 * 当注册中心的节点新增或者移除的时候，这个类主要负责对内存中的url做变更
 */
public class ConnectionHandler {
    /**
     * 核心的连接处理器
     * 专门用于与服务端建立连接
     */
    private static Bootstrap bootstrap;

    public static void setBootstrap(Bootstrap bootstrap) {
        ConnectionHandler.bootstrap = bootstrap;
    }

    /**
     * 构建单个连接通道 元操作，既要处理连接，还要统一将连接进行内存存储管理
     * @param providerServiceName 服务名称
     * @param providerIp 服务端ip
     */
    public static void connect(String providerServiceName, String providerIp) throws InterruptedException, ExecutionException {
        if(bootstrap == null){
            bootstrap = new Bootstrap();
        }
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];
        int port = Integer.parseInt(providerAddress[1]);

        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(new InetSocketAddress(ip, port)).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()){
                completableFuture.complete(future.channel());
            }else {
                throw new IllegalStateException();
            }
        });
        Channel channel = completableFuture.get();
        ChannelProvider channelProvider = new ChannelProvider();
        channelProvider.set(providerServiceName, channel);
    }
}
