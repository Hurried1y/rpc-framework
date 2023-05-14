package com.hurried1y.spring;

import com.hurried1y.annotation.RpcService;
import com.hurried1y.config.RpcServiceConfig;

import com.hurried1y.remoting.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static com.hurried1y.cache.CommonServerCache.SERVER_CONFIG;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
@Slf4j
@Component
public class RpcServerAutoConfiguration implements InitializingBean, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if(beanMap.size() == 0){
            //说明当前应用内部不需要对外暴露服务
            return;
        }
        NettyRpcServer nettyRpcServer = new NettyRpcServer();
        nettyRpcServer.initServerConfig();
        for (String beanName : beanMap.keySet()) {
            log.info("[{}] is annotated with [{}]", beanName, RpcService.class.getCanonicalName());
            //build RpcServiceProperties
            Object bean = beanMap.get(beanName);
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceConfig rpcServiceConfig = null;
            try {
                rpcServiceConfig = RpcServiceConfig.builder()
                        .host(InetAddress.getLocalHost().getHostAddress())
                        .group(rpcService.group())
                        .version(rpcService.version())
                        .service(bean)
                        .port(SERVER_CONFIG.getServerPort()).build();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            nettyRpcServer.exposeService(rpcServiceConfig);
        }
        nettyRpcServer.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

