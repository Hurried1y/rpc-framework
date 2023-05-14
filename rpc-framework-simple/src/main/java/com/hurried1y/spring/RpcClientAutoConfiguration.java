package com.hurried1y.spring;

import com.hurried1y.annotation.RpcReference;
import com.hurried1y.config.RpcServiceConfig;
import com.hurried1y.proxy.RpcClientProxy;
import com.hurried1y.remoting.transport.netty.client.ConnectionHandler;
import com.hurried1y.remoting.transport.netty.client.NettyRpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;
import java.net.UnknownHostException;

/**
 * User：Hurried1y
 * Date：2023/5/9
 */
@Slf4j
public class RpcClientAutoConfiguration implements BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {
    private static NettyRpcClient client;
    private volatile boolean needInitClient = false;
    private volatile boolean hasInitClientConfig = false;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        final Class<?> targetClass = bean.getClass();
        final Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field field : declaredFields) {
            if(field.isAnnotationPresent(RpcReference.class)){
                if(!hasInitClientConfig) {
                    //每个Reference对应一个RpcClient
                    client = new NettyRpcClient();
                    client.initClientApplication();
                    hasInitClientConfig = true;
                }
                needInitClient = true;
                final RpcReference annotation = field.getAnnotation(RpcReference.class);
                final RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(annotation.group())
                        .version(annotation.version())
                        .build();
                field.setAccessible(true);
                RpcClientProxy rpcClientProxy = new RpcClientProxy(client, rpcServiceConfig);
                final Object proxy = rpcClientProxy.getProxy(field.getType());
                try {
                    field.set(bean, proxy);
                    client.doSubscribeService(field.getType());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        if(needInitClient && client != null){
            log.info(" ================== [{}] started success ================== ", client.getClass().getName());
            ConnectionHandler.setBootstrap(client.getBootstrap());
            client.doConnectServer();
        }
    }
}
