package com.odd.rpc.core.remoting.invoker;

import com.odd.rpc.core.registry.Register;
import com.odd.rpc.core.registry.impl.LocalRegister;
import com.odd.rpc.core.remoting.net.params.BaseCallback;
import com.odd.rpc.core.remoting.net.params.OddRpcFutureResponse;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.util.OddRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * odd-rpc invoker factory, init service-registry
 *
 * @author oddity
 * @create 2023-11-25 23:06
 */
public class OddRpcInvokerFactory {

    private static Logger logger = LoggerFactory.getLogger(OddRpcInvokerFactory.class);

    // ---------------------- default instance ----------------------
    //实现了单例模式 以防在RpcReferenceBean封装RpcInvokerFactory时没有封装成功，则调用getInstance获取此单例
    //TODO 具体这块得结合LocalRegister的逻辑
    private static volatile OddRpcInvokerFactory instance = new OddRpcInvokerFactory(LocalRegister.class, null);
    public static OddRpcInvokerFactory getInstance() {
        return instance;
    }

    // ---------------------- config ----------------------

    private Class<? extends Register> serviceRegistryClass;  // class.forname
    private Map<String, String> serviceRegistryParam;

    public OddRpcInvokerFactory() {
    }
    public OddRpcInvokerFactory(Class<? extends Register> serviceRegistryClass, Map<String, String> serviceRegistryParam) {
        this.serviceRegistryClass = serviceRegistryClass;
        this.serviceRegistryParam = serviceRegistryParam;
    }

    // ---------------------- start / stop ----------------------

    public void start() throws Exception {
        // start registry
        if (serviceRegistryClass != null){
            register = serviceRegistryClass.newInstance();
            register.start(serviceRegistryParam);
        }
    }

    public void stop() throws Exception{
        //stop registry
        if (register != null){
            register.stop();
        }
        //stop callback
        if (stopCallbackList.size() > 0){
            for (BaseCallback callback : stopCallbackList){
                try {
                    callback.run();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        //stop CallbackThreadPool
        stopCallbackThreadPool();
    }

    // ---------------------- service registry ----------------------

    private Register register;
    public Register getRegister() {
        return register;
    }

    // ---------------------- service callback ----------------------

    private List<BaseCallback> stopCallbackList = new ArrayList<BaseCallback>();
    public void addStopCallBack(BaseCallback callback) { //关闭TCP连接池、NioEventLoopGroup等等
        stopCallbackList.add(callback);
    }

    // ---------------------- future-response pool ----------------------

    //一个InvokerFactory对各个请求与调用结果的Map封装，
    // 有结果，有回调执行回调（指示成功/失败，并打印），
    // 无回调执行封装结果到map
    //key: requestId
    //val: FutureResponse
    private ConcurrentHashMap<String, OddRpcFutureResponse> futureResponsePool = new ConcurrentHashMap<String, OddRpcFutureResponse>();
    public void setInvokerFuture(String requestId, OddRpcFutureResponse futureResponse){
        futureResponsePool.put(requestId, futureResponse);
    }
    public void removeInvokerFuture(String requestId) {
        futureResponsePool.remove(requestId);
    }
    public void notifyInvokerFuture(String requestId, final OddRpcResponse oddRpcResponse){

        // get
        final OddRpcFutureResponse futureResponse = futureResponsePool.get(requestId);
        if (futureResponse == null){
            return;
        }

        //notify
        if (futureResponse.getInvokeCallback() != null) {
            //callback type
            try {
                executeResponseCallback(new Runnable() {
                    @Override
                    public void run() {
                        if (oddRpcResponse.getErrorMsg() != null){
                            futureResponse.getInvokeCallback().onFailure(new OddRpcException(oddRpcResponse.getErrorMsg()));
                        }else {
                            futureResponse.getInvokeCallback().onSuccess(oddRpcResponse.getResult());
                        }
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            //other normal type
            futureResponse.setResponse(oddRpcResponse);
        }

        //调用完以后必删除，和Netty-rpc思路一致 ("一次一密""一次一个UUID(requestId)")
        // do remove
        futureResponsePool.remove(requestId);
    }

    // ---------------------- response callback ThreadPool ----------------------
    //执行异步回调的线程池，**CallType.CALLBACK == callType**所特有,默认空

    private ThreadPoolExecutor responseCallbackThreadPool = null;
    public void executeResponseCallback(Runnable runnable){
        if (responseCallbackThreadPool == null){
            //初始化responseCallbackThreadPool
            synchronized (this) {
                if (responseCallbackThreadPool == null) {
                    responseCallbackThreadPool = new ThreadPoolExecutor(
                            10,
                            100,
                            60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<Runnable>(1000),
                            new ThreadFactory() {
                                @Override
                                public Thread newThread(Runnable r) {
                                    return new Thread(r, "odd-rpc, OddRpcInvokerFactory-responseCallbackThreadPool-" + r.hashCode());
                                }
                            },
                            new RejectedExecutionHandler() {
                                @Override
                                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                                    throw new OddRpcException("odd-rpc Invoke Callback Thread pool is EXHAUSTED!");
                                }
                            });
                }
            }
        }
        responseCallbackThreadPool.execute(runnable);
    }

    public void stopCallbackThreadPool() {
        if (responseCallbackThreadPool != null) {
            responseCallbackThreadPool.shutdown();
        }
    }

}
