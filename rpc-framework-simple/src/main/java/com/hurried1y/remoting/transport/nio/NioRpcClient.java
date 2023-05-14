package com.hurried1y.remoting.transport.nio;

import com.alibaba.fastjson.JSON;
import com.hurried1y.uitls.ThreadPoolFactoryUtil;
import com.hurried1y.remoting.transport.RpcRequestTransport;
import com.hurried1y.remoting.transport.netty.server.NettyRpcServer;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

/**
 * User：Hurried1y
 * Date：2023/4/24
 */
@Slf4j
public class NioRpcClient implements RpcRequestTransport {
    //选择器
    private Selector selector;
    //服务端地址
    private final static String HOST = "127.0.0.1";
    //服务端端口
    private final static int PORT = 8090;
    //通道
    private SocketChannel channel;
    //客户端key
    private ExecutorService threadPool;

    public NioRpcClient() {
        try {
            //初始化选择器
            selector = Selector.open();
            //初始化通道
            channel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            //设置非阻塞
            channel.configureBlocking(false);
            //将通道注册到选择器上，监听服务端的响应
            channel.register(selector, SelectionKey.OP_READ);
            threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("nio-rpc-server");
        } catch (Exception e){
            log.error("NioRpcClient初始化失败: ", e);
        }
        //启动客户端监听
        threadPool.execute(this::start);
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        try {
            //发送请求
            String json = JSON.toJSONString(rpcRequest);

            channel.write(ByteBuffer.wrap(json.getBytes(StandardCharsets.UTF_8)));
//            channel.write(ByteBuffer.wrap(rpcRequest.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            log.error("NioRpcClient发送请求失败: ", e);
        }
        //轮巡接收响应结果
        return readInfo();
    }

    public Object readInfo(){
        try {
            while (true) {
                while (selector.select() > 0) {
                    final Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        final SelectionKey sk = it.next();
                        if (sk.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            channel.read(buffer);
                            buffer.flip();
                            String json = new String(buffer.array(), 0, buffer.remaining());
                            it.remove();
                            return JSON.parseObject(json, RpcResponse.class);
//                            return new ObjectInputStream(channel.socket().getInputStream());
                        }
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.error("NioRpcClient接收响应时发生错误: ", e);
        }
        return null;
    }

    /**
     * 客户端监听服务端的调用请求
     */
    public void listen() {
        try {
            while (true) {
                while (selector.select() > 0) {
                    final Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        final SelectionKey sk = it.next();
                        if (sk.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            channel.read(buffer);
                            buffer.flip();
//                            ObjectInputStream ois = new ObjectInputStream(channel.socket().getInputStream());
                            String json = new String(buffer.array(), 0, buffer.remaining());
                            final RpcRequest rpcRequest = JSON.parseObject(json, RpcRequest.class);
                            log.info("NioRpcClient接收到请求: " + rpcRequest);
                            //将请求交给线程池处理
                            threadPool.execute(new NioRpcRequestHandlerRunnable(rpcRequest, channel));
                        }
                        it.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.info("NioRpcClient监听服务端调用请求时发生错误: ", e);
        }
    }

    public void start(){
        listen();
    }
}
