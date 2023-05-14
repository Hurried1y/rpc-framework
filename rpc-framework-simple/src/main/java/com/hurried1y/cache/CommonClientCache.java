package com.hurried1y.cache;


import com.hurried1y.registry.URL;
import com.hurried1y.config.ClientConfig;
import io.netty.channel.Channel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 公用缓存 存储请求队列等公共信息
 *
 * User：Hurried1y
 * Date：2023/4/20
 */
public class CommonClientCache {


    public static ClientConfig CLIENT_CONFIG;
    //@Reference -> provider名称 --> 该服务有哪些集群URL
    public static List<URL> SUBSCRIBE_SERVICE_LIST = new ArrayList<>();
    //服务提供者ip
    public static Set<String> SERVER_ADDRESS = new HashSet<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者
    public static Map<String, List<Channel>> CONNECT_MAP = new ConcurrentHashMap<>();

}