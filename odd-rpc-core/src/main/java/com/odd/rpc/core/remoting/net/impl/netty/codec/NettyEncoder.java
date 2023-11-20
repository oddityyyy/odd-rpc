package com.odd.rpc.core.remoting.net.impl.netty.codec;

import com.odd.rpc.core.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author oddity
 * @create 2023-11-24 21:01
 */
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass; // RpcRequest for client/RpcResponse for server 两种场景都需Encoder
    private Serializer serializer;

    public NettyEncoder(Class<?> genericClass, final Serializer serializer){
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)){
            byte[] data = serializer.serialize(in);
            out.writeInt(data.length); //将数据的长度写入 `out` 中
            out.writeBytes(data);      //将序列化后的字节数组写入到 `out` 中
        }
    }
}
