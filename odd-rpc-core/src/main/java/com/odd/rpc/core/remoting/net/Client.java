package com.odd.rpc.core.remoting.net;

import com.odd.rpc.core.remoting.invoker.reference.OddRpcReferenceBean;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * i client
 * 与RpcRefercenBean互相耦合
 *
 * @author oddity
 * @create 2023-11-23 15:43
 */
public abstract class Client {

    protected static final Logger logger = LoggerFactory.getLogger(Client.class);

    // ---------------------- init ----------------------

    protected volatile OddRpcReferenceBean oddRpcReferenceBean;

    public void init(OddRpcReferenceBean oddRpcReferenceBean) {
        this.oddRpcReferenceBean = oddRpcReferenceBean;
    }

    // ---------------------- send ----------------------

    /**
     * async send, bind requestId and future-response
     * 绑定操作具体见OddRpcInvokerFactory
     *
     * @param address
     * @param oddRpcRequest
     * @throws Exception
     */
    public abstract void asyncSend(String address, OddRpcRequest oddRpcRequest) throws Exception;
}
