package com.hurried1y.remoting.transport.nio;

import com.alibaba.fastjson.JSON;
import com.hurried1y.uitls.ThreadPoolFactoryUtil;
import com.hurried1y.registry.ServiceDiscovery;
import com.hurried1y.registry.impl.ZkServiceDiscoveryImpl;
import com.hurried1y.remoting.transport.netty.server.NettyRpcServer;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;

/**
 * User：Hurried1y
 * Date：2023/4/24
 */
@Slf4j
public class NioRpcServer {
    //服务端通道
    private ServerSocketChannel ssChannel;
    //选择器
    private Selector selector;
    //服务端地址
    private final static String HOST = "127.0.0.1";
    //服务端端口
    private final static int PORT = 8090;
    //线程池
    private ExecutorService threadPool;
    //服务发现
    private ServiceDiscovery serviceDiscovery;

    public NioRpcServer(){
        try {
            //创建客户端通道
            this.ssChannel = ServerSocketChannel.open();
            //创建选择器
            this.selector = Selector.open();
            //绑定客户端连接的端口
            ssChannel.bind(new InetSocketAddress(HOST, PORT));
            //设置非阻塞
            ssChannel.configureBlocking(false);
            //把通道注册到选择器上，监听客户端的连接请求
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);
            threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("nio-rpc-server");
            serviceDiscovery = new ZkServiceDiscoveryImpl();
        } catch (Exception e){
            log.error("NioRpcServer初始化失败: ", e);
        }
    }

    /**
     * 服务端监听客户端的所有请求
     */
    public void listen() {
        //轮巡事件
        try {
            while (selector.select() > 0) {
                //获取选择器中
                final Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey sk = it.next();
                    //如果是客户端连接请求
                    if (sk.isAcceptable()) {
                        //获取客户端连接请求
                        final SocketChannel channel = ssChannel.accept();
                        channel.configureBlocking(false);
                        //将当前通道注册到选择器上
                        channel.register(selector, SelectionKey.OP_READ);
                        log.info("NioRpcServer监听到客户端连接: {}", channel.getRemoteAddress());
                    } else if (sk.isReadable()) {
                        //异步处理客户端请求 -- 负载均衡到目标服务
                        try {
                            sendToClient(sk);
                        } catch (IOException e) {
                            try {
                                SocketChannel channel = (SocketChannel) sk.channel();
                                log.error(channel.getRemoteAddress()+"已断开连接");
                                //取消注册
                                sk.channel();
                                //关闭通道
                                channel.close();
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                    it.remove();
                }
            }
        } catch (Exception e) {
            log.error("NioRpcServer监听失败: ", e);
        }
    }

    private void sendToClient(SelectionKey sk) throws IOException {
        SocketChannel channel = (SocketChannel) sk.channel();
        //创建缓冲区
        final ByteBuffer buffer = ByteBuffer.allocate(1024);
        //读取数据
        int len = channel.read(buffer);
        if (len > 0) {
            //切换读取模式
            buffer.flip();
            //获取客户端请求
            String json = new String(buffer.array(), 0, buffer.remaining());
            log.info("NioRpcServer接收到客户端请求: {}", json);
            //反序列化
            final RpcRequest rpcRequest = JSON.parseObject(json, RpcRequest.class);

            //负载均衡，获取目标服务地址
            final InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequest);
            String addr = inetSocketAddress.toString();
            log.info("NioRpcServer接收到客户端请求，负载到目标服务地址为: {}", inetSocketAddress);
            //向目标服务发送请求
            for (SelectionKey selectionKey : selector.selectedKeys()) {
                if (selectionKey.channel() instanceof SocketChannel) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    if (selectionKey.attachment() == null || selectionKey.attachment().equals(addr)) {
//                      socketChannel.write(ByteBuffer.wrap(rpcRequest.toString().getBytes()));
                        socketChannel.write(ByteBuffer.wrap(json.getBytes()));
                        break;
                    }
                }
            }
            //TODO 异步获取目标服务响应
            Object result = readInfo(channel);
            json = JSON.toJSONString(result);
            //把结果返回给客户端
            channel.write(ByteBuffer.wrap(json.getBytes()));
            buffer.clear();
        }
    }

    public Object readInfo(SocketChannel channel) throws IOException {
        while (true) {
            while (selector.select() > 0) {
                final Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    if (it.next().isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.read(buffer);
                        buffer.flip();
                        String json = new String(buffer.array(), 0, buffer.remaining());
                        it.remove();
                        return JSON.parseObject(json, RpcResponse.class);
                    }
                    it.remove();
                }
            }
        }
    }

    public void start(){
        listen();
    }
}
