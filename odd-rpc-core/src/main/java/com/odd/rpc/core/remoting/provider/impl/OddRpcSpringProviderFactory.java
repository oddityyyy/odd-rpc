package com.odd.rpc.core.remoting.provider.impl;

import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.remoting.provider.annotation.OddRpcService;
import com.odd.rpc.core.util.OddRpcException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * xxl-rpc provider (for spring)
 * 这个类主要作用是在 Spring 环境中管理 RPC 服务提供者，
 * 通过扫描带有 `@XxlRpcService` 注解的 Bean，将其注册到 RPC 服务工厂中，
 * 并在 Spring 容器启动和关闭时分别启动和停止 RPC 服务提供者。
 *
 * @author oddity
 * @create 2023-11-23 23:14
 */
public class OddRpcSpringProviderFactory extends OddRpcProviderFactory implements ApplicationContextAware, InitializingBean, DisposableBean {

    /**
     * `ApplicationContextAware` 接口的实现，用于获取 Spring 应用上下文
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // 获取带有 `@XxlRpcService` 注解的 Bean 对象(serviceBean才是真正具体的实现类/和抽象出来的RpcReferenceBean对标)，这些 Bean 对象是需要发布为 RPC 服务的对象
        // key为Bean的名称（类名小写形式）
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(OddRpcService.class);
        if (serviceBeanMap != null && serviceBeanMap.size() > 0){
            for (Object serviceBean : serviceBeanMap.values()){
                //valid
                if (serviceBean.getClass().getInterfaces().length == 0){
                    throw new OddRpcException("odd-rpc, service(OddRpcService) must inherit interface.");
                }
                //add service
                OddRpcService oddRpcService = serviceBean.getClass().getAnnotation(OddRpcService.class);

                String iface = serviceBean.getClass().getInterfaces()[0].getName();
                String version = oddRpcService.version();

                // 发布为 RPC 服务的对象，serviceBean才是真正具体的实现类，
                // 这里把iface+version糅合为serviceKey,和serviceBean封装注册到serviceData中
                super.addService(iface, version, serviceBean);
            }
        }
    }

    /**
     * InitializingBean` 接口的实现，在属性设置完成后被调用
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //启动ProviderFactory
        super.start();
    }

    /**
     * DisposableBean` 接口的实现，在 Bean 销毁时被调用。
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        super.stop();
    }
}
