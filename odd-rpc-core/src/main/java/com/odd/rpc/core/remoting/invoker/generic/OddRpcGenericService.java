package com.odd.rpc.core.remoting.invoker.generic;

/**
 * 提供 “泛化调用” 支持，服务调用方不依赖服务方提供的API(不依赖公共接口直接调用)
 * 开启 “泛化调用” 时服务方不需要做任何调整，
 * 仅需要调用方初始化一个泛化调用服务Reference （”OddRpcGenericService”） 即可。
 *
 * @author oddity
 * @create 2023-11-24 18:47
 */
public interface OddRpcGenericService {

    /**
     * generic invoke
     *
     * @param iface             iface name
     * @param version           iface version
     * @param method            method name
     * @param parameterTypes    parameter types, limit base type like "int、java.lang.Integer、java.util.List、java.util.Map ..."
     * @param args
     * @return
     */
    public Object invoke(String iface, String version, String method, String[] parameterTypes, Object[] args);
}
