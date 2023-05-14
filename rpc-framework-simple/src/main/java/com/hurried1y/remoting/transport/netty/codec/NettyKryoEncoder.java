package com.hurried1y.remoting.transport.netty.codec;

import com.hurried1y.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.AllArgsConstructor;

/**
 * User：Hurried1y
 * Date：2023/4/27
 */
@AllArgsConstructor
public class NettyKryoEncoder extends MessageToByteEncoder<Object> {
    private final Serializer serializer;
    private final Class<?> genericClass;

    /**
     * 编码器 -- 将对象转换为字节码然后写入到 ByteBuf 中
     * @param channelHandlerContext 上下文
     * @param o 对象
     * @param byteBuf ByteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(genericClass.isInstance(o)) {
            //将对象转换为字节码
            byte[] bytes = serializer.serialize(o);
            //写入 ByteBuf
            byteBuf.writeInt(bytes.length);
            //写入字节码
            byteBuf.writeBytes(bytes);
        }
    }
}
