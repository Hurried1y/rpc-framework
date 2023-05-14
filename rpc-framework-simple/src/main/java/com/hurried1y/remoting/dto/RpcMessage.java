package com.hurried1y.remoting.dto;

import lombok.*;

/**
 * User：Hurried1y
 * Date：2023/4/19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class RpcMessage {
    //消息类型
    private byte messageType;
    //序列化类型
    private byte codec;
    //压缩类型
    private byte compress;
    //请求id
    private int requestId;
    //数据
    private Object data;
}
