package com.hurried1y.remoting.transport.netty.codec;

import com.hurried1y.compress.gzip.GzipCompress;
import com.hurried1y.enums.CompressTypeEnum;
import com.hurried1y.enums.SerializationTypeEnum;
import com.hurried1y.extension.ExtensionLoader;
import com.hurried1y.remoting.constants.RpcConstants;
import com.hurried1y.compress.Compress;
import com.hurried1y.remoting.dto.RpcMessage;
import com.hurried1y.serialize.Serializer;
import com.hurried1y.serialize.hessian.HessianSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * User：Hurried1y
 * Date：2023/4/24
 *
 * custom protocol decoder
 *
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+--------+-----+-----+-------+
 *   |   magic   code        |version | full length         |messageType| codec |compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4Byte magic code（魔法数）   1Byte version（版本）   4Byte full length（消息长度）
 * 1Byte messageType（消息类型）  1Byte compress（压缩类型） 1Byte codec（序列化类型）
 * 4Byte requestId（请求的Id）
 * body（object类型数据）
 *
 * User：Hurried1y
 * Date：2023/4/26
 *
 */
@Slf4j
public class RpcMessageEncoder extends MessageToByteEncoder<RpcMessage> {
    // 原子类，保证线程安全
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage rpcMessage, ByteBuf out) {
        //将RpcMessage对象转换为字节流，写入到ByteBuf中
        try {
            //写入魔数
            out.writeBytes(RpcConstants.MAGIC_NUMBER);
            //写入版本号
            out.writeByte(RpcConstants.VERSION);
            //写入消息长度，先占位，后面再写入
            out.writerIndex(out.writerIndex() + 4);
            //写入消息类型
            byte messageType = rpcMessage.getMessageType();
            out.writeByte(messageType);
            //写入序列化类型
            out.writeByte(rpcMessage.getCodec());
            //写入压缩类型
            out.writeByte(CompressTypeEnum.GZIP.getCode());
            //写入requestId，相当于请求序号，为了全双工通信，提供异步能力
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            //获取消息长度和消息体
            byte[] bodyBytes = null;
            //初始消息长度为消息头长度
            int fullLength = RpcConstants.HEAD_LENGTH;
            //如果消息类型不是心跳包, fullLength = head length + body length
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE
                    && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                //获取序列化类型
                String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
                //TODO 根据序列化类型通过SPI机制获取序列化器
                Serializer serializer = new HessianSerializer();
//                Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
//                        .getExtension(codecName);
                //序列化消息体
                bodyBytes = serializer.serialize(rpcMessage.getData());
                //获取压缩类型
                String compressName = CompressTypeEnum.getName(rpcMessage.getCompress());
                //TODO 根据压缩类型通过SPI机制获取压缩器
                Compress compress = new GzipCompress();
//                Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
//                        .getExtension(compressName);
                //压缩消息体
                bodyBytes = compress.compress(bodyBytes);
                //最终消息长度 = 消息头长度 + 消息体长度
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                //写入消息体
                out.writeBytes(bodyBytes);
            }
            //获取写入索引
            int writeIndex = out.writerIndex();
            //回到消息长度的占位符位置：当前索引 - 消息长度占位符长度 + 魔数长度 + version长度
            out.writerIndex(writeIndex - fullLength + RpcConstants.MAGIC_NUMBER.length + 1);
            //写入消息长度
            out.writeInt(fullLength);
            //回到写入索引位置
            out.writerIndex(writeIndex);
        } catch (Exception e) {
            log.error("Encode request error!", e);
        }

    }
}
