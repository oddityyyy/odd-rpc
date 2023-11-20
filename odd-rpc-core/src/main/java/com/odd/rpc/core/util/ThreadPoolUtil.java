package com.odd.rpc.core.util;

import java.util.concurrent.*;

/**
 * @author oddity
 * @create 2023-11-23 16:39
 */
public class ThreadPoolUtil {

    /**
     * serverHandlerPool这个池子的作用是让serverHandler支持并发，让server能够在同一时刻支持多个请求的远程调用
     * @param serverType 区别NettyServer和NettyHttpServer
     * @param corePoolSize
     * @param maxPoolSize
     * @return
     */
    public static ThreadPoolExecutor makeServerThreadPool(final String serverType, int corePoolSize, int maxPoolSize){

        ThreadPoolExecutor serverHandlerPool = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1000),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "odd-rpc, " + serverType + "-serverHandlerPool-" + r.hashCode());
                    }
                },
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        throw new OddRpcException("odd-rpc " + serverType + " Thread pool is EXHAUSTED!");
                    }
                });// default maxThreads 300, minThreads 60

        return serverHandlerPool;
    }
}
