package com.odd.rpc.core.remoting.invoker.call;

/**
 * 抽象的异步回调类
 *
 * 使用了 `ThreadLocal` 来存储和获取当前线程的回调对象，以便在异步环境中进行回调处理
 *
 * @author oddity
 * @create 2023-11-26 1:24
 */
public abstract class OddRpcInvokeCallback<T> {

    public abstract void onSuccess(T result);

    public abstract void onFailure(Throwable exception);


    // ---------------------- thread invoke callback ----------------------

    /**
     * 这里声明了一个 `ThreadLocal` 变量 `threadInvokerFuture`，
     * 用于存储当前线程的 `OddRpcInvokeCallback` 对象。
     */
    private static ThreadLocal<OddRpcInvokeCallback> threadInvokerFuture = new ThreadLocal<OddRpcInvokeCallback>();

    /**
     * get callback
     * 用于从 `ThreadLocal` 中获取当前线程的 `OddRpcInvokeCallback` 对象，并移除它
     *
     * @return
     */
    public static OddRpcInvokeCallback getCallback(){
        OddRpcInvokeCallback invokeCallback = threadInvokerFuture.get();
        threadInvokerFuture.remove();
        return invokeCallback;
    }

    /**
     * set future
     * 用于设置当前线程的 `OddRpcInvokeCallback` 对象到 `ThreadLocal` 中
     *
     * @param invokeCallback
     */
    public static void setCallback(OddRpcInvokeCallback invokeCallback){
        threadInvokerFuture.set(invokeCallback);
    }

    /**
     *
     * remove future
     * 用于移除当前线程的 `OddRpcInvokeCallback` 对象。
     */
    public static void removeCallback() {
        threadInvokerFuture.remove();
    }
}
