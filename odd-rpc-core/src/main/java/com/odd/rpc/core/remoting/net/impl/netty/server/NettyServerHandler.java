package com.odd.rpc.core.remoting.net.impl.netty.server;

import com.odd.rpc.core.remoting.net.params.Beat;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.util.ThrowableUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * netty server handler
 *
 * @author oddity
 * @create 2023-11-24 21:41
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<OddRpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private OddRpcProviderFactory oddRpcProviderFactory;
    private ThreadPoolExecutor serverHandlerPool;

    public NettyServerHandler(final OddRpcProviderFactory oddRpcProviderFactory, final ThreadPoolExecutor serverHandlerPool){
        this.oddRpcProviderFactory = oddRpcProviderFactory;
        this.serverHandlerPool = serverHandlerPool;
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final OddRpcRequest oddRpcRequest) throws Exception {

        // filter beat
        if (Beat.BEAT_ID.equalsIgnoreCase(oddRpcRequest.getRequestId())){
            logger.debug(">>>>>>>>>>> odd-rpc provider netty server read beat-ping.");
            return;
        }

        // do invoke
        try {
            serverHandlerPool.execute(new Runnable() {
                @Override
                public void run() {
                    // invoke + response
                    OddRpcResponse oddRpcResponse = oddRpcProviderFactory.invokeService(oddRpcRequest);

                    ctx.writeAndFlush(oddRpcResponse);
                }
            });
        } catch (Exception e) {
            // catch error
            OddRpcResponse oddRpcResponse = new OddRpcResponse();
            oddRpcResponse.setRequestId(oddRpcRequest.getRequestId());
            oddRpcResponse.setErrorMsg(ThrowableUtil.toString(e));

            ctx.writeAndFlush(oddRpcResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(">>>>>>>>>>> odd-rpc provider netty server caught exception", cause);
        ctx.close();
    }

    /**
     * 客户端保证了保活机制，若仍然trigger, 则客户端已死idle，就关闭channel
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent){
            ctx.channel().close();  // beat 3N, close if idle
            logger.debug(">>>>>>>>>>> odd-rpc provider netty server close an idle channel.");
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }
}
