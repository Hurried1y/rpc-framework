package com.hurried1y.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.hurried1y.cache.CommonClientCache.CONNECT_MAP;

/**
 * User：Hurried1y
 * Date：2023/4/27
 */
@Slf4j
public class ChannelProvider {
    /**
     * 根据服务名称获取channel
     * @param interfaceName 服务名称
     * @return channel
     */
    public Channel get(String interfaceName){
        if(CONNECT_MAP.containsKey(interfaceName)){
            final Channel channel = CONNECT_MAP.get(interfaceName).get(0);
            //如果channel可用，直接返回
            if(channel != null && channel.isActive()){
                return channel;
            } else {
                return null;
            }
        }
        return null;
    }

    public void set(String interfaceName, Channel channel){
        if(CONNECT_MAP.containsKey(interfaceName)){
            CONNECT_MAP.get(interfaceName).add(channel);
        } else {
            List<Channel> channelList = new ArrayList<>();
            channelList.add(channel);
            CONNECT_MAP.put(interfaceName, channelList);
        }
    }
}
