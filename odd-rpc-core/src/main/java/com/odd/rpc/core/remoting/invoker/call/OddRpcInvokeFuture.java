package com.odd.rpc.core.remoting.invoker.call;

import com.odd.rpc.core.remoting.net.params.OddRpcFutureResponse;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.util.OddRpcException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * OddRpcFutureResponse的上层封装
 *
 * @author oddity
 * @create 2023-11-29 17:00
 */
public class OddRpcInvokeFuture implements Future {

    private OddRpcFutureResponse futureResponse;

    public OddRpcInvokeFuture(OddRpcFutureResponse futureResponse) {
        this.futureResponse = futureResponse;
    }

    public void stop() {
        // remove-InvokerFuture
        futureResponse.removeInvokerFuture();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return futureResponse.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return futureResponse.isCancelled();
    }

    @Override
    public boolean isDone() {
        return futureResponse.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new OddRpcException(e);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            // future get
            OddRpcResponse oddRpcResponse = futureResponse.get(timeout, unit);
            if (oddRpcResponse.getErrorMsg() != null){
                throw new OddRpcException(oddRpcResponse.getErrorMsg());
            }
            return oddRpcResponse.getResult();
        } finally {
            stop();
        }
    }


    // ---------------------- thread invoke future ----------------------

    private static ThreadLocal<OddRpcInvokeFuture> threadInvokerFuture = new ThreadLocal<OddRpcInvokeFuture>();

    /**
     * get future
     *
     * @param type
     * @param <T>
     * @return
     */
    public static <T> Future<T> getFuture(Class<T> type){
        Future<T> future = (Future<T>) threadInvokerFuture.get();
        threadInvokerFuture.remove();
        return future;
    }

    /**
     * set future
     *
     * @param future
     */
    public static void setFuture(OddRpcInvokeFuture future){
        threadInvokerFuture.set(future);
    }

    /**
     * remove future
     */
    public static void removeFuture() {
        threadInvokerFuture.remove();
    }
}
