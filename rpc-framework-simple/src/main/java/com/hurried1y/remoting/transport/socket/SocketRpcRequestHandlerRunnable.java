package com.hurried1y.remoting.transport.socket;

import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import com.hurried1y.remoting.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable {
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket) {
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    @Override
    public void run() {
        log.info("消息处理 - 服务端正在通过线程: [{}] 处理客户端的消息...", Thread.currentThread().getName());
        //从socket中获取input、output流
        try (
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());){
            //从input流中读取RpcRequest对象
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result, rpcRequest.getRequestId()));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e){
            log.error("消息处理 - 服务端处理客户端的消息时发生错误: ", e);
        }
    }
}
