package com.odd.rpc.core.remoting.net;

import com.odd.rpc.core.remoting.net.params.BaseCallback;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server抽象基类
 * @author oddity
 * @create 2023-11-23 15:43
 */
public abstract class Server {

    protected static final Logger logger = LoggerFactory.getLogger(Server.class);

    private BaseCallback startedCallback;
    private BaseCallback stopedCallback;

    public void setStartedCallback(BaseCallback startedCallback) {
        this.startedCallback = startedCallback;
    }

    public void setStopedCallback(BaseCallback stopedCallback) {
        this.stopedCallback = stopedCallback;
    }

    /**
     * start server
     *
     * @param oddRpcProviderFactory
     * @throws Exception
     */
    public abstract void start(final OddRpcProviderFactory oddRpcProviderFactory) throws Exception;

    /**
     * callback when started
     */
    //在下层实现中具体做法是查找并注册服务
    public void onStarted() {
        if(startedCallback != null){
            try {
                startedCallback.run();
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> odd-rpc, server startedCallback error.", e);
            }
        }
    }

    /**
     * stop server
     *
     * @throws Exception
     */
    public abstract void stop() throws Exception;

    /**
     * 设置stop()的回调，暂时useless
     * callback when stoped
     */
    public void onStoped() {
        if (stopedCallback != null) {
            try {
                stopedCallback.run();
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> odd-rpc, server stopedCallback error.", e);
            }
        }
    }
}
