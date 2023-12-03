package com.odd.rpc.sample.server;

import com.odd.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.serialize.impl.HessianSerializer;
import com.odd.rpc.sample.api.DemoService;
import com.odd.rpc.sample.server.service.DemoServiceImpl;

import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-10-21 20:48:40
 */
public class OddRpcServerApplication {

    public static void main(String[] args) throws Exception {

        // init
        OddRpcProviderFactory providerFactory = new OddRpcProviderFactory();
        providerFactory.setServer(NettyServer.class);
        providerFactory.setSerializer(HessianSerializer.class);
        providerFactory.setCorePoolSize(-1);
        providerFactory.setMaxPoolSize(-1);
        providerFactory.setIp(null);
        providerFactory.setPort(7080);
        providerFactory.setAccessToken(null);
        providerFactory.setServiceRegistry(null);
        providerFactory.setServiceRegistryParam(null);

        // add services
        providerFactory.addService(DemoService.class.getName(), null, new DemoServiceImpl());

        // start
        providerFactory.start();

        while (!Thread.currentThread().isInterrupted()) {
            TimeUnit.HOURS.sleep(1);
        }

        // stop
        providerFactory.stop();

    }

}
