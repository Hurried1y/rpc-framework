package com.hurried1y.remoting.transport.netty.server;

import com.hurried1y.remoting.dto.RpcMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User：Hurried1y
 * Date：2023/5/12
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServerChannelReadData {
    private RpcMessage rpcMessage;
    private ChannelHandlerContext ctx;
}
