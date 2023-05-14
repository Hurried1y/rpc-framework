package com.hurried1y.remoting.transport.socket;

import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.uitls.ThreadPoolFactoryUtil;
import com.hurried1y.config.RpcServiceConfig;
import com.hurried1y.provider.ServiceProvider;
import com.hurried1y.provider.impl.ZkServiceProviderImpl;
import com.hurried1y.remoting.transport.netty.server.NettyRpcServer;
import com.hurried1y.config.CustomShutdownHook;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer() {
        threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-rpc-server");
        serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        try (ServerSocket server = new ServerSocket()){
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, 8090));
            //注册关闭钩子
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while ((socket = server.accept()) != null){
                log.info("client connect: {}", socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        } catch (IOException e){
            log.error("在启动SocketServer服务端时发生错误: ", e);
        }
    }
}

