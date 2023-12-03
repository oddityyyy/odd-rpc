package com.odd.rpc.core.remoting.net.impl.netty_http.client;

import com.odd.rpc.core.remoting.net.Client;
import com.odd.rpc.core.remoting.net.common.ConnectClient;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;

/**
 * @author oddity
 * @create 2023-12-03 21:10
 */
public class NettyHttpClient extends Client {

    private Class<? extends ConnectClient> connectClientImpl = NettyHttpConnectClient.class;

    @Override
    public void asyncSend(String address, OddRpcRequest oddRpcRequest) throws Exception {
        ConnectClient.asyncSend(oddRpcRequest, address, connectClientImpl, oddRpcReferenceBean);
    }
}
