package com.hurried1y.remoting.transport.socket;

import com.hurried1y.exception.RpcException;
import com.hurried1y.registry.ServiceDiscovery;
import com.hurried1y.registry.impl.ZkServiceDiscoveryImpl;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.transport.RpcRequestTransport;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient() {
        //TODO 通过配置文件选择服务发现方式
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
        try (Socket socket = new Socket()) {
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //通过output stream向服务器发送请求
            objectOutputStream.writeObject(rpcRequest);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            //通过input stream获取服务器返回的结果
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RpcException("服务调用 - 服务调用失败: ", e);
        }
    }
}
