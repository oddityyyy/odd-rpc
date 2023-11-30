package com.odd.rpc.core.remoting.net.impl.netty.client;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.net.common.ConnectClient;
import com.odd.rpc.core.remoting.net.impl.netty.codec.NettyDecoder;
import com.odd.rpc.core.remoting.net.impl.netty.codec.NettyEncoder;
import com.odd.rpc.core.remoting.net.params.BaseCallback;
import com.odd.rpc.core.remoting.net.params.Beat;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.serialize.Serializer;
import com.odd.rpc.core.util.IpUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * netty pooled client
 *
 * @author oddity
 * @create 2023-11-29 21:03
 */
public class NettyConnectClient extends ConnectClient {

    private static NioEventLoopGroup nioEventLoopGroup;

    private Channel channel;

    //初始化TCP连接
    @Override
    public void init(String address, final Serializer serializer, final OddRpcInvokerFactory oddRpcInvokerFactory) throws Exception {

        //address
        Object[] array = IpUtil.parseIpPort(address);
        String host = (String) array[0];
        int port = (int) array[1];

        //group
        if (nioEventLoopGroup == null){
            //TODO pr等待通过
            synchronized (NettyConnectClient.class){
                if (nioEventLoopGroup == null){
                    nioEventLoopGroup = new NioEventLoopGroup();
                    oddRpcInvokerFactory.addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            nioEventLoopGroup.shutdownGracefully();
                        }
                    });
                }
            }
        }

        // init
        final NettyConnectClient thisClient = this;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS)) // beat N, close if fail
                                .addLast(new NettyEncoder(OddRpcRequest.class, serializer))
                                .addLast(new NettyDecoder(OddRpcResponse.class, serializer))
                                .addLast(new NettyClientHandler(oddRpcInvokerFactory, thisClient));
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)  //启用心跳保活
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000); //10s
        this.channel = bootstrap.connect(host, port).sync().channel();

        // valid
        if (!isValidate()){
            close();
            return;
        }

        logger.debug(">>>>>>>>>>> odd-rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);
    }

    @Override
    public boolean isValidate() {
        if (this.channel != null){
            return this.channel.isActive();
        }
        return false;
    }

    @Override
    public void close() {
        if (this.channel != null && this.channel.isActive()) {
            this.channel.close();   // if this.channel.isOpen()
        }
        logger.debug(">>>>>>>>>>> odd-rpc netty client close.");
    }

    @Override
    public void send(OddRpcRequest oddRpcRequest) throws Exception {
        this.channel.writeAndFlush(oddRpcRequest).sync();
    }
}
