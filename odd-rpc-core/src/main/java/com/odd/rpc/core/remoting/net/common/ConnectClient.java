package com.odd.rpc.core.remoting.net.common;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.reference.OddRpcReferenceBean;
import com.odd.rpc.core.remoting.net.params.BaseCallback;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author oddity
 * @create 2023-11-25 22:56
 */
public abstract class ConnectClient {

    protected static transient Logger logger = LoggerFactory.getLogger(ConnectClient.class);

    // ---------------------- iface ----------------------

    // 创建channel等
    public abstract void init(String address, final Serializer serializer, final OddRpcInvokerFactory oddRpcInvokerFactory) throws Exception;

    public abstract void close();

    public abstract boolean isValidate();

    public abstract void send(OddRpcRequest oddRpcRequest) throws Exception;


    // ---------------------- client pool map ----------------------

    /**
     * async send
     */
    public static void asyncSend(OddRpcRequest oddRpcRequest, String address,
                                 Class<? extends ConnectClient> connectClientImpl,
                                 final OddRpcReferenceBean oddRpcReferenceBean) throws Exception {

        // client pool	[tips03 : may save 35ms/100invoke if move it to constructor, but it is necessary. cause by ConcurrentHashMap.get]
        // 得到的是connectClientImpl, 针对于特定的address(复用)
        ConnectClient clientPool = ConnectClient.getPool(address, connectClientImpl, oddRpcReferenceBean);

        try {
            // do invoke
            clientPool.send(oddRpcRequest);
        } catch (Exception e) {
            throw e;
        }
    }

    private static volatile ConcurrentMap<String, ConnectClient> connectClientMap;  // (static) already add StopCallBack
    private static volatile ConcurrentMap<String, Object> connectClientLockMap = new ConcurrentHashMap<>();

    private static ConnectClient getPool(String address, Class<? extends ConnectClient> connectClientImpl,
                                         final OddRpcReferenceBean oddRpcReferenceBean) throws Exception {

        // init base component , avoid repeat init
        if (connectClientMap == null){
            //获取当前类锁，此时锁的是connectClientMap,防止并发写,保证从一个地址只能有一个线程可以获取到ConnectClient对象
            synchronized (ConnectClient.class){
                if (connectClientMap == null){
                    // init     String存Address ConnectClient存ConnectClientImpl实现类
                    connectClientMap = new ConcurrentHashMap<String, ConnectClient>();
                    // stop callback     此回调作用是关闭客户端连接池
                    oddRpcReferenceBean.getInvokerFactory().addStopCallBack(new BaseCallback() {
                        @Override
                        public void run() throws Exception {
                            if (connectClientMap.size() > 0){
                                for (String key : connectClientMap.keySet()){
                                    ConnectClient clientPool = connectClientMap.get(key);
                                    clientPool.close();
                                }
                                connectClientMap.clear();
                            }
                        }
                    });
                }
            }
        }

        // get-valid client ConnectClient里面的channel不为空且在活动即为合法
        ConnectClient connectClient = connectClientMap.get(address);
        if (connectClient != null && connectClient.isValidate()){
            return connectClient;
        }

        // lock
        Object clinetLock = connectClientLockMap.get(address);
        if (clinetLock == null){
            connectClientLockMap.putIfAbsent(address, new Object());
            clinetLock = connectClientLockMap.get(address);
        }

        // remove-create new client
        synchronized (clinetLock) {

            // get-valid client, avoid repeat
            connectClient = connectClientMap.get(address);
            if (connectClient != null && connectClient.isValidate()){
                return connectClient;
            }

            // remove old connectClient有但是不合法(去掉死的channel)
            if (connectClient != null){
                // 关里面的channel
                connectClient.close();
                connectClientMap.remove(address);
            }

            // set pool
            ConnectClient connectClient_new = connectClientImpl.newInstance();
            try {
                // 创建channel等
                connectClient_new.init(address, oddRpcReferenceBean.getSerializerInstance(), oddRpcReferenceBean.getInvokerFactory());
                connectClientMap.put(address, connectClient_new);
            } catch (Exception e) {
                connectClient_new.close();
                throw e;
            }

            return connectClient_new;
        }
    }
}
