package com.odd.rpc.core.remoting.net.impl.netty.client;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.net.params.Beat;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc netty client handler
 *
 * @author oddity
 * @create 2023-11-29 21:02
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<OddRpcResponse> {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    private OddRpcInvokerFactory oddRpcInvokerFactory;
    private NettyConnectClient nettyConnectClient;

    public NettyClientHandler(final OddRpcInvokerFactory oddRpcInvokerFactory, NettyConnectClient nettyConnectClient) {
        this.oddRpcInvokerFactory = oddRpcInvokerFactory;
        this.nettyConnectClient = nettyConnectClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, OddRpcResponse oddRpcResponse) throws Exception {

        // notify response RpcResponse.getRequestId()保证了request和response一一对应
        oddRpcInvokerFactory.notifyInvokerFuture(oddRpcResponse.getRequestId(), oddRpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(">>>>>>>>>>> odd-rpc netty client caught exception", cause);
        ctx.close();
    }

    /**
     * IdleStateHandler心跳断开时，调用trigger回调
     * 当接收到空闲状态事件时，发送心跳消息来维持连接的活跃性，以防止连接由于长时间的空闲而被关闭。
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            /*ctx.channel().close();      // close idle channel
			logger.debug(">>>>>>>>>>> xxl-rpc netty client close an idle channel.");*/

            //如果连接处于空闲状态，通过发送心跳消息来维持连接的有效性，并且记录了发送了心跳消息的日志
            nettyConnectClient.send(Beat.BEAT_PING); // beat N, close if fail(may throw error)
            logger.debug(">>>>>>>>>>> odd-rpc netty client send beat-ping.");
        }else {
            //如果不是空闲状态事件，那么会调用父类的 `userEventTriggered` 方法，继续处理其他类型的事件。
            super.userEventTriggered(ctx, evt);
        }
    }
}
