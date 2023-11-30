package com.odd.rpc.core.remoting.provider;

import com.odd.rpc.core.registry.Register;
import com.odd.rpc.core.remoting.net.Server;
import com.odd.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.odd.rpc.core.remoting.net.params.BaseCallback;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.remoting.net.params.OddRpcResponse;
import com.odd.rpc.core.serialize.Serializer;
import com.odd.rpc.core.serialize.impl.HessianSerializer;
import com.odd.rpc.core.util.IpUtil;
import com.odd.rpc.core.util.NetUtil;
import com.odd.rpc.core.util.OddRpcException;
import com.odd.rpc.core.util.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 生产者基础工厂类
 *
 * @author oddity
 * @create 2023-11-23 15:39
 */
public class OddRpcProviderFactory {

    private static final Logger logger = LoggerFactory.getLogger(OddRpcProviderFactory.class);

    // ---------------------- config ----------------------
    private Class<? extends Server> server = NettyServer.class;
    private Class<? extends Serializer> serializer = HessianSerializer.class;

    private int corePoolSize = 60;
    private int maxPoolSize = 300;

    private String ip = null;             // server ip, for registry
    private int port = 7080;              // server default port
    private String registryAddress;       // default use registryAddress to registry , otherwise use ip:port if registryAddress is null
    private String accessToken = null;    // 我本地的令牌

    private Class<? extends Register> serviceRegistry = null;
    private Map<String, String> serviceRegistryParam = null;

    // set
    public void setServer(Class<? extends Server> server) {
        this.server = server;
    }

    public void setSerializer(Class<? extends Serializer> serializer) {
        this.serializer = serializer;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setServiceRegistry(Class<? extends Register> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }

    // get
    public int getCorePoolSize() {
        return corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getPort() {
        return port;
    }

    public Serializer getSerializerInstance() {
        return serializerInstance;
    }

    // ---------------------- start / stop ----------------------
    private Server serverInstance;
    private Serializer serializerInstance;
    private Register registerInstance;

    public void start() throws Exception{

        // valid
        if (this.server == null) {
            throw new OddRpcException("odd-rpc provider server missing.");
        }
        if (this.serializer == null){
            throw new OddRpcException("odd-rpc provider serializer missing.");
        }
        if (!(this.corePoolSize > 0 && this.maxPoolSize > 0 && this.maxPoolSize >= this.corePoolSize)){
            this.corePoolSize = 60;
            this.maxPoolSize = 300;
        }
        //若服务提供地址为空，则默认为本机地址(调用该方法的机器)
        if (this.ip == null){
            this.ip = IpUtil.getIp();
        }
        if (this.port <= 0){
            this.port = 7080;
        }
        //若无默认为本机地址（调用该方法的机器）
        if (this.registryAddress == null || this.registryAddress.trim().length() == 0){
            this.registryAddress = IpUtil.getIpPort(this.ip, this.port);
        }
        if (NetUtil.isPortUsed(this.port)){
            throw new OddRpcException("odd-rpc provider port["+ this.port +"] is used.");
        }

        // init serializerInstance
        this.serializerInstance = serializer.newInstance();

        // start server(NettyServer default)
        serverInstance = server.newInstance();
        serverInstance.setStartedCallback(new BaseCallback() { // serviceRegistry started
            @Override
            public void run() throws Exception {
                // start registry
                if (serviceRegistry != null){
                    registerInstance = serviceRegistry.newInstance();
                    //启动一个AdminRegistryClient
                    registerInstance.start(serviceRegistryParam);
                    if (serviceData.size() > 0){
                        registerInstance.registry(serviceData.keySet(), registryAddress);
                    }
                }
            }
        });
        serverInstance.setStopedCallback(new BaseCallback() { // serviceRegistry stoped
            @Override
            public void run() {
                //stop registry
                if (registerInstance != null){
                    if (serviceData.size() > 0){
                        registerInstance.remove(serviceData.keySet(), registryAddress);
                    }
                    registerInstance.stop();
                    registerInstance = null;
                }
            }
        });
        //启动NettyServer
        serverInstance.start(this);
    }

    public void stop() throws Exception{
        // stop server
        serverInstance.stop();
    }


    // ---------------------- server invoke ----------------------

    /**
     * init local rpc service map
     * key: serviceKey(xxlRpcRequest.getClassName() + xxlRpcRequest.getVersion())
     * value: serviceBean
     */
    private Map<String, Object> serviceData = new HashMap<String, Object>();

    public Map<String, Object> getServiceData() {
        return serviceData;
    }

    /**
     * make service key
     *
     * @param iface
     * @param version
     * @return
     */
    public static String makeServiceKey(String iface, String version){
        String serviceKey = iface;
        if (version != null && version.trim().length() > 0){
            serviceKey += "#".concat(version);
        }
        return serviceKey;
    }

    /**
     * add service
     *
     * @param iface
     * @param version
     * @param serviceBean
     */
    public void addService(String iface, String version, Object serviceBean){
        String serviceKey = makeServiceKey(iface, version);
        serviceData.put(serviceKey, serviceBean);

        logger.info(">>>>>>>>>>> odd-rpc, provider factory add service success. serviceKey = {}, serviceBean = {}", serviceKey, serviceBean.getClass());
    }

    /**
     * invoke service
     *
     * @param oddRpcRequest
     * @return
s    */
    public OddRpcResponse invokeService(OddRpcRequest oddRpcRequest){
        //make response
        OddRpcResponse oddRpcResponse = new OddRpcResponse();
        oddRpcResponse.setRequestId(oddRpcRequest.getRequestId());

        //match service bean
        String serviceKey = makeServiceKey(oddRpcRequest.getClassName(), oddRpcRequest.getVersion());
        Object serviceBean = serviceData.get(serviceKey);

        //valid
        if (serviceBean == null){
            oddRpcResponse.setErrorMsg("The serviceKey["+ serviceKey +"] not found.");
            return oddRpcResponse;
        }

        if (System.currentTimeMillis() - oddRpcRequest.getCreateMillisTime() > 3 * 60 * 1000){
            oddRpcResponse.setErrorMsg("The timestamp difference between admin and executor exceeds the limit.");
            return oddRpcResponse;
        }
        //验证请求发过来的令牌是否和我本地的令牌一致
        if (accessToken != null && accessToken.trim().length() > 0 && !accessToken.trim().equals(oddRpcRequest.getAccessToken())){
            oddRpcResponse.setErrorMsg("The access token[" + oddRpcRequest.getAccessToken() + "] is wrong.");
            return oddRpcResponse;
        }

        try {
            //invoke
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = oddRpcRequest.getMethodName();
            Class<?>[] parameterTypes = oddRpcRequest.getParameterTypes();
            Object[] parameters = oddRpcRequest.getParameters();

            Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            Object result = method.invoke(serviceBean, parameters);
            oddRpcResponse.setResult(result);
        } catch (Throwable t) {
            // catch error
            logger.error("odd-rpc provider invokeService error.", t);
            oddRpcResponse.setErrorMsg(ThrowableUtil.toString(t));
        }

        return oddRpcResponse;
    }
}
