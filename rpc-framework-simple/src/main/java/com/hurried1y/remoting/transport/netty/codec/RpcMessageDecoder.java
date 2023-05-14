package com.hurried1y.remoting.transport.netty.codec;

import com.hurried1y.compress.gzip.GzipCompress;
import com.hurried1y.enums.CompressTypeEnum;
import com.hurried1y.enums.SerializationTypeEnum;
import com.hurried1y.extension.ExtensionLoader;
import com.hurried1y.remoting.constants.RpcConstants;
import com.hurried1y.compress.Compress;
import com.hurried1y.remoting.dto.RpcMessage;
import com.hurried1y.remoting.dto.RpcRequest;
import com.hurried1y.remoting.dto.RpcResponse;
import com.hurried1y.serialize.Serializer;
import com.hurried1y.serialize.hessian.HessianSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
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
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder {
    public RpcMessageDecoder() {
        // lengthFieldOffset = 魔数(4B) + 版本(1B) = 5B
        // lengthFieldLength = 消息长度 int(4B)
        // lengthAdjustment = -9，因为我们的长度域是从魔数开始的，所以我们需要调整长度域的偏移量
        // initialBytesToStrip = 0，我们会手动检查魔数和版本，所以不需要跳过任何字节
        this(RpcConstants.MAX_FRAME_LENGTH, 5, 4, -9, 0);
    }

    /**
     * @param maxFrameLength      最大帧长度
     * @param lengthFieldOffset   长度域(消息长度)的偏移量，简单而言就是偏移几个字节后才是长度域
     * @param lengthFieldLength   长度域的所占的字节数
     * @param lengthAdjustment    长度适配适配值。该值表示协议中长度字段与消息体字段直接的距离值，Netty在解码时会根据该值计算消息体的开始位置，默认为0
     * @param initialBytesToStrip 最后解析结果中需要剥离的字节数
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
                             int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if (decoded instanceof ByteBuf) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.TOTAL_LENGTH) {
                try {
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!", e);
                    throw e;
                } finally {
                    frame.release();
                }
            }

        }
        return decoded;
    }


    private Object decodeFrame(ByteBuf in) {
        //按顺序读取魔数、版本、消息长度
        //读取魔数并比较
        checkMagicNumber(in);
        //读取版本并比较
        checkVersion(in);
        //读取4字节消息长度
        int fullLength = in.readInt();
        //读取1字节消息类型
        byte messageType = in.readByte();
        //读取1字节序列化类型
        byte codecType = in.readByte();
        //读取1字节压缩类型
        byte compressType = in.readByte();
        //读取4字节请求ID
        int requestId = in.readInt();
        //构建RpcMessage对象
        RpcMessage rpcMessage = RpcMessage.builder()
                .codec(codecType)
                .requestId(requestId)
                .messageType(messageType).build();
        //根据消息类型解析消息
        //心跳消息
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        //普通消息
        //读取消息体长度
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bs = new byte[bodyLength];
            //读取消息体
            in.readBytes(bs);
            //解压缩
            String compressName = CompressTypeEnum.getName(compressType);
            //TODO 根据压缩类型通过SPI机制获取压缩器
            Compress compress = new GzipCompress();
//            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class)
//                    .getExtension(compressName);
            bs = compress.decompress(bs);
            //反序列化
            String codecName = SerializationTypeEnum.getName(rpcMessage.getCodec());
            //TODO 根据序列化类型通过SPI机制获取序列化器
            Serializer serializer = new HessianSerializer();
//            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class)
//                    .getExtension(codecName);
            //根据消息类型反序列化
            if (messageType == RpcConstants.REQUEST_TYPE) {
                RpcRequest tmpValue = serializer.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            } else {
                RpcResponse tmpValue = serializer.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;

    }

    private void checkVersion(ByteBuf in) {
        //读取第5个字节，比较版本
        byte version = in.readByte();
        if (version != RpcConstants.VERSION) {
            throw new RuntimeException("version isn't compatible" + version);
        }
    }

    private void checkMagicNumber(ByteBuf in) {
        //读取前4个字节，比较魔数
        int len = RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp = new byte[len];
        in.readBytes(tmp);
        for (int i = 0; i < len; i++) {
            if (tmp[i] != RpcConstants.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException("Unknown magic code: " + Arrays.toString(tmp));
            }
        }
    }

}
