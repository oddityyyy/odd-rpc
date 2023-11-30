package com.odd.rpc.sample.client.conf;

import com.odd.rpc.core.registry.impl.OddRpcAdminRegister;
import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.impl.OddRpcSpringInvokerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * odd-rpc invoker config
 * 初始化RpcSpringInvokerFactory
 *
 * @author oddity
 * @create 2023-11-25 23:23
 */

@Configuration
public class OddRpcInvokerConfig {

    private Logger logger = LoggerFactory.getLogger(OddRpcInvokerConfig.class);

    @Value("${odd-rpc.registry.oddrpcadmin.address}")
    private String address;

    @Value("${odd-rpc.registry.oddrpcadmin.env}")
    private String env;

    @Bean
    public OddRpcSpringInvokerFactory oddRpcSpringInvokerFactory(){

        OddRpcSpringInvokerFactory invokerFactory = new OddRpcSpringInvokerFactory();
        invokerFactory.setServiceRegistryClass(OddRpcAdminRegister.class);
        invokerFactory.setServiceRegistryParam(new HashMap<String, String>(){{
            put(OddRpcAdminRegister.ADMIN_ADDRESS, address);
            put(OddRpcAdminRegister.ENV, env);
        }});

        logger.info(">>>>>>>>>>> odd-rpc invoker config init finish.");
        return invokerFactory;
    }
}
