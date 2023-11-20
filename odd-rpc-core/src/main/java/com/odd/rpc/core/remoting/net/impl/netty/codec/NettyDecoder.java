package com.odd.rpc.core.remoting.net.impl.netty.codec;

import com.odd.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.odd.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author oddity
 * @create 2023-11-24 18:39
 */
public class NettyDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass; // RpcRequest for server/RpcResponse for client 两种场景都需Decoder
    private Serializer serializer;

    public NettyDecoder(Class<?> genericClass, final Serializer serializer){
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4){
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt(); //int类型的byte数组的长度
        if (dataLength < 0){
            ctx.close();
        }
        if (in.readableBytes() < dataLength){ //接收到的数据不完整，重置读取索引，等待更多数据到达
            in.resetReaderIndex();
            return; // fix 1024k buffer splic limit
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = serializer.deserialize(data, genericClass);
        out.add(obj);
    }
}
