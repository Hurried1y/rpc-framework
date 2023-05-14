package com.hurried1y.remoting.transport.netty.client;

import com.hurried1y.enums.CompressTypeEnum;
import com.hurried1y.enums.RpcResponseCodeEnum;
import com.hurried1y.enums.SerializationTypeEnum;
import com.hurried1y.factory.SingletonFactory;
import com.hurried1y.remoting.constants.RpcConstants;
import com.hurried1y.remoting.dto.RpcMessage;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import com.hurried1y.remoting.handler.RpcRequestHandler;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 *
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 *
 * User：Hurried1y
 * Date：2023/4/26
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(NettyRpcClient.class);
    }

    /**
     * 读取服务端发送的消息并处理
     * @param ctx 上下文
     * @param msg 服务端发送的消息
     * @throws Exception 异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if(msg instanceof RpcMessage){
                RpcMessage rpcMessage = (RpcMessage) msg;
                byte messageType = rpcMessage.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("客户端接收到服务端的心跳响应");
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    RpcResponse<Object> rpcResponse = (RpcResponse<Object>) rpcMessage.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        } finally {
            // 释放 ByteBuf
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 当客户端连接服务端成功之后，会触发 channelActive 方法，我们在这个方法里面发送数据给服务端
     * @param ctx 上下文
     * @throws Exception 异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if(evt instanceof IdleStateEvent){
//            final IdleState state = ((IdleStateEvent) evt).state();
//            if(state == IdleState.WRITER_IDLE){
//                //写空闲，发送心跳包
//                log.info("出现写空闲，向服务端发送心跳包...");
//                Channel channel = nettyRpcClient.getChannel()
//                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
//                RpcMessage rpcMessage = new RpcMessage();
//                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
//                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
//                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
//                rpcMessage.setData(RpcConstants.PING);
//                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
//            }
//        } else {
//            super.userEventTriggered(ctx, evt);
//        }
    }

    /**
     * 异常处理
     * @param ctx 上下文
     * @param cause 异常
     * @throws Exception 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端处理过程中有错误发生: ", cause);
        cause.printStackTrace();
        ctx.close();
    }
}