package com.hurried1y.cache;


import com.hurried1y.config.ServerConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * User：Hurried1y
 * Date：2023/4/20
 *
 * 服务端缓存
 */
public class CommonServerCache {
    public static ServerConfig SERVER_CONFIG;
    //服务端发布的服务
    public static Map<String, Object> SERVICE_MAP = new HashMap<>();
}