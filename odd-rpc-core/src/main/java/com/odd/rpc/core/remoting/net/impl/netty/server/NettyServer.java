package com.odd.rpc.core.remoting.net.impl.netty.server;

import com.odd.rpc.core.remoting.net.Server;
import com.odd.rpc.core.remoting.net.impl.netty.codec.NettyDecoder;
import com.odd.rpc.core.remoting.net.impl.netty.codec.NettyEncoder;
import com.odd.rpc.core.remoting.net.params.Beat;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.util.ThreadPoolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * netty rpc server
 *
 * @author oddity
 * @create 2023-11-23 16:12
 */
public class NettyServer extends Server {

    private Thread thread;

    //此处在方法的形参前加final关键字，是为了避免形参的值在方法体中被修改
    //当final修饰的变量是引用类型，表示引用地址不可变，但该引用所指向的对象内容仍然可以被修改
    @Override
    public void start(final OddRpcProviderFactory oddRpcProviderFactory) throws Exception {

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // param
                final ThreadPoolExecutor serverHandlerPool = ThreadPoolUtil.makeServerThreadPool(
                        NettyServer.class.getSimpleName(),
                        oddRpcProviderFactory.getCorePoolSize(),
                        oddRpcProviderFactory.getMaxPoolSize());
                NioEventLoopGroup bossGroup = new NioEventLoopGroup();
                NioEventLoopGroup workerGroup = new NioEventLoopGroup();

                try {
                    //start server
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL * 3, TimeUnit.SECONDS))  //// beat 3N, close if idle
                                            .addLast(new NettyDecoder(OddRpcRequest.class, oddRpcProviderFactory.getSerializerInstance()))
                                            .addLast(new NettyEncoder(OddRpcResponse.class, oddRpcProviderFactory.getSerializerInstance()))
                                            .addLast(new NettyServerHandler(oddRpcProviderFactory, serverHandlerPool));
                                }
                            })
                            .childOption(ChannelOption.TCP_NODELAY, true)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    //bind
                    ChannelFuture future = bootstrap.bind(oddRpcProviderFactory.getPort()).sync();

                    logger.info(">>>>>>>>>>> odd-rpc remoting server start success, nettype = {}, port = {}", NettyServer.class.getName(), oddRpcProviderFactory.getPort());
                    onStarted();

                    //wait util stop
                    future.channel().closeFuture().sync();

                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.info(">>>>>>>>>>> odd-rpc remoting server stop.");
                    } else {
                        logger.error(">>>>>>>>>>> odd-rpc remoting server error.", e);
                    }
                } finally {
                    //stop
                    try {
                        serverHandlerPool.shutdown(); // shutdownNow
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        //destory server thread
        if (thread != null && thread.isAlive()){
            thread.interrupt();
        }

        //on stop
        onStoped();
        logger.info(">>>>>>>>>>> odd-rpc remoting server destroy success.");
    }
}
