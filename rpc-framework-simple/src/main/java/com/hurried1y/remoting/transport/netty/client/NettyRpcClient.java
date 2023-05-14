package com.hurried1y.remoting.transport.netty.client;

import com.hurried1y.registry.URL;
import com.hurried1y.config.ClientConfig;
import com.hurried1y.config.PropertiesBootstrap;
import com.hurried1y.enums.CompressTypeEnum;
import com.hurried1y.enums.SerializationTypeEnum;
import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.registry.ServiceDiscovery;
import com.hurried1y.registry.ServiceRegistry;
import com.hurried1y.registry.impl.ZkServiceDiscoveryImpl;
import com.hurried1y.registry.impl.ZkServiceRegistryImpl;
import com.hurried1y.remoting.constants.RpcConstants;
import com.hurried1y.remoting.transport.RpcRequestTransport;
import com.hurried1y.remoting.dto.RpcMessage;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import com.hurried1y.remoting.transport.netty.codec.RpcMessageDecoder;
import com.hurried1y.remoting.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.hurried1y.cache.CommonClientCache.*;

/**
 * User：Hurried1y
 * Date：2023/4/26
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private ServiceDiscovery serviceDiscovery;
    private UnprocessedRequests unprocessedRequests;
    private ChannelProvider channelProvider;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private ClientConfig clientConfig;
    private ServiceRegistry serviceRegistry;

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void initClientApplication(){
        bootstrap = new Bootstrap();
        eventLoopGroup = new NioEventLoopGroup();

        EventLoopGroup worker = new NioEventLoopGroup();
        bootstrap.group(worker);
        //添加 ChannelHandler 以处理每个 Channel 的日志消息
        bootstrap.handler(new LoggingHandler(LogLevel.INFO));
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new RpcMessageEncoder());
                ch.pipeline().addLast(new RpcMessageDecoder());
                //添加自定义的ChannelHandler
                ch.pipeline().addLast(new NettyRpcClientHandler());
            }
        });
        //TODO 从配置文件中读取
        this.serviceDiscovery = new ZkServiceDiscoveryImpl();
//        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();
        serviceRegistry = new ZkServiceRegistryImpl();
        CLIENT_CONFIG = clientConfig;
    }

    /**
     * 服务订阅
     * @param serviceBean 标注了@RpcReference的属性
     */
    public void doSubscribeService(Class serviceBean) throws UnknownHostException {
        URL url = new URL();
        url.setServiceName(serviceBean.getName());
        url.setApplicationName(clientConfig.getApplicationName());
        url.addParameter("host", InetAddress.getLocalHost().getHostAddress());
        //subscribe：将URL存入SUBSCRIBE_SERVICE_LIST
        serviceRegistry.subscribe(url);
    }

    /**
     * 开始与各个provider建立连接，TODO 同时监听各个providerNode节点的变化（child变化和nodeData的变化）
     */
    public void doConnectServer(){
        //SUBSCRIBE_SERVICE_LIST 为所有标注了@RpcReference的属性的信息集合
        for (URL providerURL : SUBSCRIBE_SERVICE_LIST) {
            //根据标注了@RpcReference的serviceName去Zookeeper上获取其对应的地址
            List<String> providerIps = serviceDiscovery.lookupService(providerURL.getServiceName());
            /*
                相当于@RpcReference客户端与对应的每一个服务提供者建立连接
                @RpcReference
                OrderService orderService;
                -> OrderApplication:8081
                -> OrderApplication:8082
             */
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerURL.getServiceName(), providerIp);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resultFuture = new CompletableFuture<>();
        //根据serviceName获取Channel
        Channel channel = getChannel(rpcRequest.getInterfaceName());

        if(channel.isActive()){
            //将未处理的请求放入map中
            unprocessedRequests.put(rpcRequest.getRequestId(), resultFuture);
            //构建RpcMessage
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.KYRO.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE).build();
            //将请求发送给服务器
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()){
                    log.info("客户端发送消息: [{}]", rpcMessage);
                }else {
                    future.channel().close();
                    resultFuture.completeExceptionally(future.cause());
                    log.error("发送消息时发生错误: ", future.cause());
                }
            });
        } else {
            throw new IllegalStateException();
        }

        return resultFuture;
    }

    public Channel getChannel(String interfaceName){
        Channel channel = channelProvider.get(interfaceName);
        return channel;
    }

    public void close(){
        eventLoopGroup.shutdownGracefully();
    }
}
