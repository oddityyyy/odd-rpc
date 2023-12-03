package com.odd.rpc.core.remoting.net.impl.netty_http.client;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.net.common.ConnectClient;
import com.odd.rpc.core.remoting.net.params.BaseCallback;
import com.odd.rpc.core.remoting.net.params.Beat;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.serialize.Serializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * @author oddity
 * @create 2023-12-03 21:12
 */
public class NettyHttpConnectClient extends ConnectClient {

    private static NioEventLoopGroup nioEventLoopGroup;

    private Channel channel;

    private Serializer serializer;
    private String address;
    private String host;

    @Override
    public void init(String address, final Serializer serializer, final OddRpcInvokerFactory oddRpcInvokerFactory) throws Exception {

        // address
        if (!address.toLowerCase().startsWith("http")) {
            address = "http://" + address;	// IP:PORT, need parse to url
        }

        this.address = address;
        URL url = new URL(address);
        this.host = url.getHost();
        int port = url.getPort()>-1?url.getPort():80;

        // group
        if (nioEventLoopGroup == null) {
            synchronized (NettyHttpConnectClient.class) {
                if (nioEventLoopGroup == null) {
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
        final NettyHttpConnectClient thisClient = this;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(nioEventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline()
                                .addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS))   // beat N, close if fail
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(5*1024*1024))
                                .addLast(new NettyHttpClientHandler(oddRpcInvokerFactory, serializer, thisClient));
                    }
                })
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
        this.channel = bootstrap.connect(host, port).sync().channel();

        this.serializer = serializer;

        // valid
        if (!isValidate()) {
            close();
            return;
        }

        logger.debug(">>>>>>>>>>> odd-rpc netty client proxy, connect to server success at host:{}, port:{}", host, port);
    }

    @Override
    public boolean isValidate() {
        if (this.channel != null) {
            return this.channel.isActive();
        }
        return false;
    }


    @Override
    public void close() {
        if (this.channel!=null && this.channel.isActive()) {
            this.channel.close();		// if this.channel.isOpen()
        }
        logger.debug(">>>>>>>>>>> odd-rpc netty client close.");
    }


    @Override
    public void send(OddRpcRequest oddRpcRequest) throws Exception {
        byte[] requestBytes = serializer.serialize(oddRpcRequest);

        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, new URI(address).getRawPath(), Unpooled.wrappedBuffer(requestBytes));
        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

        this.channel.writeAndFlush(request).sync();
    }
}
