package com.odd.rpc.core.remoting.net.impl.netty.client;

import com.odd.rpc.core.remoting.net.Client;
import com.odd.rpc.core.remoting.net.common.ConnectClient;
import com.odd.rpc.core.remoting.net.params.OddRpcRequest;

/**
 * netty client
 *
 * 对ConnectClient的上层封装
 *
 * @author oddity
 * @create 2023-11-25 22:54
 */
public class NettyClient extends Client {

    private Class<? extends ConnectClient> connectClientImpl = NettyConnectClient.class;

    @Override
    public void asyncSend(String address, OddRpcRequest oddRpcRequest) throws Exception {
        ConnectClient.asyncSend(oddRpcRequest, address, connectClientImpl, oddRpcReferenceBean);
    }

}
