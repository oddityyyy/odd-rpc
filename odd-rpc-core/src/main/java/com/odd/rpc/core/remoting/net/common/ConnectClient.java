package com.odd.rpc.core.remoting.net.common;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.annotaion.OddRpcReference;
import com.odd.rpc.core.remoting.invoker.reference.OddRpcReferenceBean;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import com.odd.rpc.core.serialize.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oddity
 * @create 2023-11-25 22:56
 */
public abstract class ConnectClient {

    protected static transient Logger logger = LoggerFactory.getLogger(ConnectClient.class);

    // ---------------------- iface ----------------------

    public abstract void init(String address, final Serializer serializer, final OddRpcInvokerFactory oddRpcInvokerFactory) throws Exception;

    public abstract void close();

    public abstract boolean isValidate();

    public abstract void send(OddRpcRequest oddRpcRequest) throws Exception;


    // ---------------------- client pool map ----------------------

    public static void asyncSend(OddRpcRequest oddRpcRequest, String address,
                                 Class<? extends ConnectClient> connectClientImpl,
                                 final OddRpcReferenceBean oddRpcReferenceBean)
}
