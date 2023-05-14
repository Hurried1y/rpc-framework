package com.hurried1y.config;

import com.hurried1y.registry.impl.util.CuratorUtils;
import com.hurried1y.uitls.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static com.hurried1y.cache.CommonServerCache.SERVER_CONFIG;

/**
 * User：Hurried1y
 * Date：2023/4/20
 * 自定义关闭钩子
 */
@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll(){
        log.info("addShutdownHook clearAll");
        //虚拟机在正常关闭前，会调用所有已注册的钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), SERVER_CONFIG.getServerPort());
                //将当前服务从zookeeper注册中心删除
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), socketAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            //关闭线程池
            ThreadPoolFactoryUtil.shutdownThreadPool();
        }));
    }
}
