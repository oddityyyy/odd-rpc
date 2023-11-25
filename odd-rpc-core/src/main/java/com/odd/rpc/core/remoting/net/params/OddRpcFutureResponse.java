package com.odd.rpc.core.remoting.net.params;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.call.OddRpcInvokeCallback;
import com.odd.rpc.core.util.OddRpcException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * call back future
 *
 * 对OddRpcResponse的上层封装, 异步返回response
 * 原装属性RpcInvokerFactory RpcRequest RpcInvokeCallback
 *
 * @author oddity
 * @create 2023-11-26 0:49
 */
public class OddRpcFutureResponse implements Future<OddRpcResponse> {

    private OddRpcInvokerFactory invokerFactory;

    //net data
    private OddRpcRequest request;
    private OddRpcResponse response;

    //future lock
    private boolean done = false;
    private Object lock = new Object();

    // callback, can be null **CallType.CALLBACK == callType**才会设置
    private OddRpcInvokeCallback invokeCallback;

    public OddRpcFutureResponse(final OddRpcInvokerFactory invokerFactory, OddRpcRequest request, OddRpcInvokeCallback invokeCallback) {
        this.invokerFactory = invokerFactory;
        this.request = request;
        this.invokeCallback = invokeCallback;

        //set InvokerFuture
        setInvokerFuture();
    }


    // ---------------------- response pool ----------------------

    //用于管理响应对象的方法，将当前的 `OddRpcFutureResponse`
    //对象添加到 `invokerFactory`的futureResponsePool中以便后续的处理。(封装成为一个InvokerFuture)
    //只要有4中调用方式中的一种调用，都会调用此类的构造器，从而调用此方法，然后必将此FutureResponse注册到invokerFactory中
    public void setInvokerFuture(){
        this.invokerFactory.setInvokerFuture(request.getRequestId(), this);
    }
    //关闭资源时调用
    public void removeInvokerFuture(){
        this.invokerFactory.removeInvokerFuture(request.getRequestId());
    }


    // ---------------------- get ----------------------
    public OddRpcRequest getRequest() {
        return request;
    }
    public OddRpcInvokeCallback getInvokeCallback() {
        return invokeCallback;
    }


    // ---------------------- for invoke back ----------------------
    //设置响应对象的方法，当接收到响应时调用，设置响应对象，并通知等待的线程。
    public void setResponse(OddRpcResponse response){
        this.response = response;
        synchronized (lock){
            done = true;
            lock.notifyAll();
        }
    }


    // ---------------------- for invoke ----------------------
    //这五个方法来自 `Future` 接口的实现，用于判断任务是否被取消、是否完成等状态。

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    //实现Future的方法，用于获取响应对象，支持等待超时的功能。会阻塞当前线程直到接收到响应或超时。
    @Override
    public OddRpcResponse get() throws InterruptedException, ExecutionException {
        try {
            return get(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new OddRpcException(e);
        }
    }

    @Override
    public OddRpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            synchronized (lock) {
                try {
                    if (timeout < 0){
                        lock.wait();
                    }else {
                        long timeoutMills = (TimeUnit.MILLISECONDS == unit) ? timeout : TimeUnit.MILLISECONDS.convert(timeout, unit);
                        lock.wait(timeoutMills);
                    }
                } catch (InterruptedException e) {
                    throw e;
                }
            }
        }

        if (!done){
            throw new OddRpcException("odd-rpc, request timeout at:"+ System.currentTimeMillis() +", request:" + request.toString());
        }
        return response;
    }
}
