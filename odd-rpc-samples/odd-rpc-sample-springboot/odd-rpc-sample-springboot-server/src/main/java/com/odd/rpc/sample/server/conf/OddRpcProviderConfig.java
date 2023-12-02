package com.odd.rpc.sample.server.conf;

import com.odd.rpc.core.registry.impl.OddRpcAdminRegister;
import com.odd.rpc.core.remoting.net.impl.netty.server.NettyServer;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.remoting.provider.impl.OddRpcSpringProviderFactory;
import com.odd.rpc.core.serialize.impl.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 初始化 SpringProviderFactory
 *
 * @author oddity
 * @create 2023-11-20 18:04
 */

@Configuration
public class OddRpcProviderConfig {

    private Logger logger = LoggerFactory.getLogger(OddRpcProviderConfig.class);

    //和配置文件中的属性绑定
    @Value("${odd-rpc.remoting.port}")
    private int port;

    @Value("${odd-rpc.registry.oddrpcadmin.address}")
    private String address;

    @Value("${odd-rpc.registry.oddrpcadmin.env}")
    private String env;

    @Bean
    public OddRpcSpringProviderFactory oddRpcSpringProviderFactory() {

        OddRpcSpringProviderFactory providerFactory = new OddRpcSpringProviderFactory();
        providerFactory.setServer(NettyServer.class);
        providerFactory.setSerializer(HessianSerializer.class);
        providerFactory.setCorePoolSize(-1); //默认60
        providerFactory.setMaxPoolSize(-1);  //默认300
        providerFactory.setIp(null);         //默认本机地址
        providerFactory.setPort(port);       //默认7080
        providerFactory.setAccessToken(null);//默认空
        providerFactory.setServiceRegistry(OddRpcAdminRegister.class);
        providerFactory.setServiceRegistryParam(new HashMap<String, String>(){{
            put(OddRpcAdminRegister.ADMIN_ADDRESS, address);
            put(OddRpcAdminRegister.ENV, env);
        }});

        logger.info(">>>>>>>>>>> odd-rpc provider config init finish.");
        return providerFactory;
    }
}
