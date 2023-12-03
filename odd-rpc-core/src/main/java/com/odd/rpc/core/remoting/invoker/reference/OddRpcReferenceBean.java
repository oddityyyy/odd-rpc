package com.odd.rpc.core.remoting.invoker.reference;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.call.CallType;
import com.odd.rpc.core.remoting.invoker.call.OddRpcInvokeCallback;
import com.odd.rpc.core.remoting.invoker.call.OddRpcInvokeFuture;
import com.odd.rpc.core.remoting.invoker.generic.OddRpcGenericService;
import com.odd.rpc.core.remoting.invoker.route.LoadBalance;
import com.odd.rpc.core.remoting.net.Client;
import com.odd.rpc.core.remoting.net.impl.netty.client.NettyClient;
import com.odd.rpc.core.remoting.net.params.OddRpcFutureResponse;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.serialize.Serializer;
import com.odd.rpc.core.serialize.impl.HessianSerializer;
import com.odd.rpc.core.util.ClassUtil;
import com.odd.rpc.core.util.OddRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * rpc reference bean, use by api
 *
 * 与Client相耦合
 *
 * @author oddity
 * @create 2023-11-26 21:48
 */
public class OddRpcReferenceBean {

    private static final Logger logger = LoggerFactory.getLogger(OddRpcReferenceBean.class);

    // ---------------------- config ----------------------

    private Class<? extends Client> client = NettyClient.class;
    private Class<? extends Serializer> serializer = HessianSerializer.class;
    private CallType callType = CallType.SYNC;
    private LoadBalance loadBalance = LoadBalance.ROUND; //默认轮询
    
    private Class<?> iface = null;
    private String version = null;
    
    private long timeout = 1000; // 1s
    
    private String address = null;
    private String accessToken = null;
    
    private OddRpcInvokeCallback invokeCallback = null;
    
    private OddRpcInvokerFactory invokerFactory = null;

    // set
    public void setClient(Class<? extends Client> client) {
        this.client = client;
    }
    public void setSerializer(Class<? extends Serializer> serializer) {
        this.serializer = serializer;
    }
    public void setCallType(CallType callType) {
        this.callType = callType;
    }
    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
    public void setIface(Class<?> iface) {
        this.iface = iface;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public void setInvokeCallback(OddRpcInvokeCallback invokeCallback) {
        this.invokeCallback = invokeCallback;
    }
    public void setInvokerFactory(OddRpcInvokerFactory invokerFactory) {
        this.invokerFactory = invokerFactory;
    }
    
    // get
    public Serializer getSerializerInstance() {
        return serializerInstance;
    }
    public long getTimeout() {
        return timeout;
    }
    public OddRpcInvokerFactory getInvokerFactory() {
        return invokerFactory;
    }
    public Class<?> getIface() {
        return iface;
    }
    
    // ---------------------- initClient ----------------------
    
    private Client clientInstance = null;
    private Serializer serializerInstance = null;
    
    public OddRpcReferenceBean initClient() throws Exception {
        
        // valid
        if (this.client == null) {
            throw new OddRpcException("odd-rpc reference client missing.");
        }
        if (this.serializer == null) {
            throw new OddRpcException("odd-rpc reference serializer missing.");
        }
        if (this.callType==null) {
            throw new OddRpcException("odd-rpc reference callType missing.");
        }
        if (this.loadBalance==null) {
            throw new OddRpcException("odd-rpc reference loadBalance missing.");
        }
        if (this.iface==null) {
            throw new OddRpcException("odd-rpc reference iface missing.");
        }
        if (this.timeout < 0){
            this.timeout = 0;
        }
        if (this.invokerFactory == null){
            this.invokerFactory = OddRpcInvokerFactory.getInstance();
        }

        // init serializerInstance
        this.serializerInstance = serializer.newInstance();

        // init Client 相互耦合
        clientInstance = client.newInstance();
        clientInstance.init(this);

        return this;
    }

    // ---------------------- util ----------------------
    // 生成代理类
    public Object getObject() throws Exception {

        // initClient
        initClient();

        // newProxyInstance
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{iface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        // method param
                        String className = method.getDeclaringClass().getName(); //iface.getName()
                        String varsion = version;
                        String methodName = method.getName();
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Object[] parameters = args;

                        // filter for generic
                        if (className.equals(OddRpcGenericService.class.getName()) && methodName.equals("invoke")){

                            Class<?>[] paramTypes = null;
                            if (args[3] != null){
                                String[] paramTypes_str = (String[]) args[3];
                                if (paramTypes_str.length > 0){
                                    paramTypes = new Class[paramTypes_str.length];
                                    for (int i = 0; i < paramTypes_str.length; i++){
                                        paramTypes[i] = ClassUtil.resolveClass(paramTypes_str[i]);
                                    }
                                }
                            }
                            className = (String) args[0];
                            varsion = (String) args[1];
                            methodName = (String) args[2];
                            parameterTypes = paramTypes;
                            parameters = (Object[]) args[4];
                        }

                        // filter method like "Object.toString()"
                        // 当使用 Java 动态代理创建一个对象时，
                        // 如果对象实现了 Object 类的方法（例如 toString()、hashCode()、equals() 等），
                        // 默认情况下这些方法也会被代理，但是对于 RPC 远程调用来说，这些方法通常不需要被代理调用。
                        if (className.equals(Object.class.getName())) {
                            logger.info(">>>>>>>>>>> odd-rpc proxy class-method not support [{}#{}]", className, methodName);
                            throw new OddRpcException("odd-rpc proxy class-method not support");
                        }

                        // address
                        String finalAddress = address;
                        if (finalAddress == null || finalAddress.trim().length() == 0){
                            if (invokerFactory != null && invokerFactory.getRegister() != null){
                                //discovery
                                String serviceKey = OddRpcProviderFactory.makeServiceKey(className, varsion);
                                TreeSet<String> addressSet = invokerFactory.getRegister().discovery(serviceKey);
                                //load balance
                                if (addressSet == null || addressSet.size() == 0){
                                    // pass then throw
                                } else if (addressSet.size() == 1){
                                    finalAddress = addressSet.first();
                                } else {
                                    finalAddress = loadBalance.oddRpcInvokerRouter.route(serviceKey, addressSet);
                                }
                            }
                        }
                        if (finalAddress == null || finalAddress.trim().length() == 0){
                            throw new OddRpcException("odd-rpc reference bean["+ className +"] address empty");
                        }

                        // request
                        OddRpcRequest oddRpcRequest = new OddRpcRequest();
                        oddRpcRequest.setRequestId(UUID.randomUUID().toString());
                        oddRpcRequest.setCreateMillisTime(System.currentTimeMillis());
                        oddRpcRequest.setAccessToken(accessToken);
                        oddRpcRequest.setClassName(className);
                        oddRpcRequest.setMethodName(methodName);
                        oddRpcRequest.setParameterTypes(parameterTypes);
                        oddRpcRequest.setParameters(parameters);
                        oddRpcRequest.setVersion(version);

                        // send
                        // 对于同步调用（CallType.SYNC），创建一个 OddRpcFutureResponse 对象，发送请求，等待响应返回。
                        if (CallType.SYNC == callType){
                            // future-response set
                            OddRpcFutureResponse futureResponse = new OddRpcFutureResponse(invokerFactory, oddRpcRequest, null);

                            try {
                                // do invoke
                                clientInstance.asyncSend(finalAddress, oddRpcRequest);

                                // future get
                                OddRpcResponse oddRpcResponse = futureResponse.get(timeout, TimeUnit.MILLISECONDS);
                                if (oddRpcResponse.getErrorMsg() != null){
                                    throw new OddRpcException(oddRpcResponse.getErrorMsg());
                                }
                                return oddRpcResponse.getResult();
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> odd-rpc, invoke error, address:{}, XxlRpcRequest{}", finalAddress, oddRpcRequest);

                                throw (e instanceof OddRpcException) ? e : new OddRpcException(e);
                            } finally {
                                //双重保证
                                //调用完以后必删除，和Netty-rpc思路一致 ("一次一密""一次一个UUID(requestId)")
                                // future-response remove
                                futureResponse.removeInvokerFuture();
                            }
                        }else if (CallType.FUTURE == callType){
                            // 在异步调用中（CallType.FUTURE），创建一个 OddRpcInvokeFuture 对象，并将未来的响应设置为异步发送请求的结果
                            // future-response set
                            OddRpcFutureResponse futureResponse = new OddRpcFutureResponse(invokerFactory, oddRpcRequest, null);
                            try {
                                // invoke future set
                                OddRpcInvokeFuture invokeFuture = new OddRpcInvokeFuture(futureResponse);
                                OddRpcInvokeFuture.setFuture(invokeFuture);

                                // do invoke
                                clientInstance.asyncSend(finalAddress, oddRpcRequest);

                                return null;
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> odd-rpc, invoke error, address:{}, OddRpcRequest{}", finalAddress, oddRpcRequest);

                                // future-response remove
                                futureResponse.removeInvokerFuture();

                                throw (e instanceof OddRpcException) ? e : new OddRpcException(e);
                            }
                        }else if (CallType.CALLBACK == callType){
                            // 对于回调类型（CallType.CALLBACK）, 获取回调对象 finalInvokeCallback，并在异步发送请求之后执行回调
                            // get callback
                            OddRpcInvokeCallback finalInvokeCallback = invokeCallback;
                            OddRpcInvokeCallback threadInvokeCallback = OddRpcInvokeCallback.getCallback();
                            if (threadInvokeCallback != null) {
                                finalInvokeCallback = threadInvokeCallback;
                            }
                            if (finalInvokeCallback == null) {
                                throw new OddRpcException("odd-rpc OddRpcInvokeCallback（CallType="+ CallType.CALLBACK.name() +"） cannot be null.");
                            }

                            // future-response set
                            OddRpcFutureResponse futureResponse = new OddRpcFutureResponse(invokerFactory, oddRpcRequest, finalInvokeCallback);
                            try {
                                clientInstance.asyncSend(finalAddress, oddRpcRequest);
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> odd-rpc, invoke error, address:{}, OddRpcRequest{}", finalAddress, oddRpcRequest);

                                // future-response remove
                                futureResponse.removeInvokerFuture();

                                throw (e instanceof OddRpcException) ? e : new OddRpcException(e);
                            }

                            return null;
                        } else if (CallType.ONEWAY == callType){
                            // 对于单向调用（CallType.ONEWAY），直接异步发送请求，无需等待返回结果
                            clientInstance.asyncSend(finalAddress, oddRpcRequest);
                            return null;
                        } else {
                            throw new OddRpcException("odd-rpc callType[" + callType + "] invalid");
                        }
                    }
                });
    }
}
