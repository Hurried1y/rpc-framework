package com.hurried1y.remoting.transport.netty.server;

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
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;


/**
 * User：Hurried1y
 * Date：2023/4/27
 * Netty服务端处理器
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter  {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler() {
        this.rpcRequestHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }

    /**
     * 读取客户端发送的消息，判断消息类型，根据消息类型进行不同的处理
     * @param ctx 上下文
     * @param msg 客户端发送的消息
     * @throws Exception 异常
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            if(msg instanceof RpcMessage){
                RpcMessage rpcMessage = (RpcMessage) msg;
                final byte messageType = ((RpcMessage) msg).getMessageType();
                rpcMessage.setCodec(SerializationTypeEnum.KYRO.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    //心跳检测请求
                    log.info("服务端接收到客户端心跳包……");
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    //设置心跳检测的数据 -- PONG 用于心跳续约
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    //处理正常的请求
                    RpcRequest rpcRequest = (RpcRequest) ((RpcMessage) msg).getData();
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    } else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                }
                //addListener -- 添加一个 ChannelFutureListener，以便在操作完成时获得通知 -- 异步
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            //确保 ByteBuf 被释放，否则可能会有内存泄露问题
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 心跳检测 -- 超时事件
     * @param ctx 上下文
     * @param evt 事件
     * @throws Exception 异常
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) { //超时事件
            //IdleStateEvent -- 心跳检测
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                //读空闲超时（客户端没有发送数据）
                log.info("idle读空闲超时，因此关闭连接");
                ctx.close();
            }
        } else {
            //调用父类的方法，继续传播事件
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 异常处理
     * @param ctx 上下文
     * @param cause 异常
     * @throws Exception 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("处理过程中有错误发生");
        cause.printStackTrace();
        ctx.close();
    }
}
