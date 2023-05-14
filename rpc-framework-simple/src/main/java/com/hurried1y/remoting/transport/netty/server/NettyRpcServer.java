package com.hurried1y.remoting.transport.netty.server;

import com.hurried1y.config.CustomShutdownHook;
import com.hurried1y.config.PropertiesBootstrap;
import com.hurried1y.config.RpcServiceConfig;
import com.hurried1y.config.ServerConfig;
import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.provider.ServiceProvider;
import com.hurried1y.provider.impl.ZkServiceProviderImpl;
import com.hurried1y.remoting.transport.netty.codec.RpcMessageDecoder;
import com.hurried1y.remoting.transport.netty.codec.RpcMessageEncoder;
import com.hurried1y.uitls.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import static com.hurried1y.cache.CommonServerCache.SERVER_CONFIG;

/**
 * User：Hurried1y
 * Date：2023/4/20
 * 服务器接收客户端消息，根据客户端消息调用相应的方法，然后将结果返回给客户端。
 */
@Component
@Slf4j
@Setter
public class NettyRpcServer {
    private ServerConfig serverConfig;

    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);


    public void registerService(RpcServiceConfig rpcServiceConfig) {
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        //创建bossGroup和workerGroup
        //bossGroup只负责连接请求，workerGroup负责读写请求
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //创建服务端启动对象
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                //cpu核心数*2
                Runtime.getRuntime().availableProcessors() * 2,
                //创建线程工厂
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group", false)
        );
        try {
            //获取本机ip
            String host = InetAddress.getLocalHost().getHostAddress();
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    // 是否开启 TCP 底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 30 秒之内没有收到客户端请求的话就关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
//                            p.addLast(new NettyKryoEncoder(new KyroSerializer(), RpcMessage.class));
//                            p.addLast(new NettyKryoDecoder(new KyroSerializer(), RpcMessage.class));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup, new NettyRpcServerHandler());
                        }
                    });
            //绑定端口，启动服务，sync()同步等待绑定成功，然后获取到ChannelFuture
            bootstrap.bind(host, serverConfig.getServerPort()).sync();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info("启动NettyRpcServer服务时发生错误: ", e);
        } finally {
            //关闭线程组
            log.info("断开NettyRpcServer服务");
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//            serviceHandlerGroup.shutdownGracefully();
        }
    }

    /**
     * 暴露服务 -- 如 127.0.0.1:8080 下的UserService、ProductService...
     * @param rpcServiceConfig
     */
    public void exposeService(RpcServiceConfig rpcServiceConfig) {
        registerService(rpcServiceConfig);
    }

    public void initServerConfig() {
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
        SERVER_CONFIG = serverConfig;
    }
}

