package com.hurried1y.remoting.constants;

import io.netty.util.AttributeKey;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * User：Hurried1y
 * Date：2023/4/20
 */
public class RpcConstants {
    //魔数 -- 用来第一时间判断是否是无效数据包
    public static final byte[] MAGIC_NUMBER = {(byte) 'g', (byte) 'r', (byte) 'p', (byte) 'c'};
    //默认编码
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //版本信息 -- 可以支持协议的升级
    public static final byte VERSION = 1;
    //数据长度
    public static final byte TOTAL_LENGTH = 16;
    //请求类型
    public static final byte REQUEST_TYPE = 1;
    //响应类型
    public static final byte RESPONSE_TYPE = 2;
    //ping -- 心跳请求类型
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong -- 心跳响应类型
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    //头部长度
    public static final int HEAD_LENGTH = 16;
    public static final String PING = "ping";
    public static final String PONG = "pong";
    //最大帧长度
    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
}
