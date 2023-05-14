package com.hurried1y.remoting.transport.netty.codec;

import com.hurried1y.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * User：Hurried1y
 * Date：2023/4/26
 * NettyKryoDecoder 是我们自定义的解码器，它负责处理“入站”消息，他会从 ByteBuf 中
 * 读取数据到业务对象对应的字节序列，然后将其反序列化为业务对象。
 *
 */
@AllArgsConstructor
@Slf4j
public class NettyKryoDecoder extends ByteToMessageDecoder {
    //序列化器
    private final Serializer serializer;
    //泛型类
    private final Class<?> genericClass;
    //Netty传输的消息长度，也就是对象序列化后的字节数组的长度，存储在 ByteBuf
    private static final int BODY_LENGTH = 4;

    /**
     * 解码 ByteBuf 对象
     * @param channelHandlerContext 解码器关联的通道处理上下文
     * @param byteBuf 字节缓冲区 "入站数据"
     * @param list 解码后的对象列表 "出站数据" -- 解码之后的对象需要添加到这个列表中
     * @throws Exception 异常
     */
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //byteBuf 中写入的消息长度=4，所以 byteBuf 的可读字节数必须大于等于4
        if(byteBuf.readableBytes() >= BODY_LENGTH) {
            //标记当前读指针的位置，以便后面重置读指针
            byteBuf.markReaderIndex();
            //读取消息的长度 (消息长度是encode时我们自己写入的)
            int dataLength = byteBuf.readInt();
            //读取的消息体长度如果小于0，不合理，关闭连接
            if (dataLength < 0) {
                log.info("ByteBuf中的可读数据长度不合理！");
                return;
            }
            //读取的消息体长度如果小于我们传送过来的消息长度，说明消息是不完整的，则resetReaderIndex.
            //这个配合markReaderIndex使用的。把读指针reset到mark的地方
            if (byteBuf.readableBytes() < dataLength) {
                byteBuf.resetReaderIndex();
                return;
            }
            //读取消息的内容
            byte[] data = new byte[dataLength];
            byteBuf.readBytes(data);
            //将 byte 数据转化为我们需要的对象 -- 反序列化
            Object obj = serializer.deserialize(data, genericClass);
            list.add(obj);
        }
    }
}
