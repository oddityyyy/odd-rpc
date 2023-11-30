package com.odd.rpc.core.remoting.invoker.impl;

import com.odd.rpc.core.registry.Register;
import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.annotaion.OddRpcReference;
import com.odd.rpc.core.remoting.invoker.reference.OddRpcReferenceBean;
import com.odd.rpc.core.remoting.provider.OddRpcProviderFactory;
import com.odd.rpc.core.util.OddRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * odd-rpc invoker factory, init service-registry and spring-bean by annotation (for spring)
 *
 * 实现了 Spring 框架的几个接口 `InitializingBean`、`DisposableBean`、`BeanFactoryAware` 和 `InstantiationAwareBeanPostProcessor`。
 * 它用于在 Spring 容器启动过程中初始化和配置 RPC 服务的消费者
 *
 * 这个类的作用是在 Spring 环境中处理 RPC 服务消费者的初始化和销毁。
 * 它会扫描带有 `@OddRpcReference` 注解的字段，并根据注解信息配置和创建相应的代理对象，
 * 并在初始化完成后启动 RPC 服务消费者，在销毁时停止 RPC 服务消费者。
 *
 * @author oddity
 * @create 2023-11-25 23:14
 */
public class OddRpcSpringInvokerFactory implements InitializingBean, DisposableBean, BeanFactoryAware, InstantiationAwareBeanPostProcessor {

    private Logger logger = LoggerFactory.getLogger(OddRpcSpringInvokerFactory.class);

    // ---------------------- config ----------------------

    private Class<? extends Register> serviceRegistryClass; // class.forname
    private Map<String, String> serviceRegistryParam;

    public void setServiceRegistryClass(Class<? extends Register> serviceRegistryClass) {
        this.serviceRegistryClass = serviceRegistryClass;
    }

    public void setServiceRegistryParam(Map<String, String> serviceRegistryParam) {
        this.serviceRegistryParam = serviceRegistryParam;
    }


    // ---------------------- util ----------------------

    private OddRpcInvokerFactory oddRpcInvokerFactory;

    //在属性设置完成后，会调用这个方法
    @Override
    public void afterPropertiesSet() throws Exception {
        // start invoker factory
        oddRpcInvokerFactory = new OddRpcInvokerFactory(serviceRegistryClass, serviceRegistryParam);
        oddRpcInvokerFactory.start();
    }

    /**
     * `InstantiationAwareBeanPostProcessor` 接口的实现，在实例化 Bean 后调用
     * 遍历传入的对象的字段，检查是否有 `@OddRpcReference` 注解
     * 如果发现有被 `@OddRpcReference` 注解标记的字段
     *  检查字段类型是否是接口类型，如果不是则抛出异常
     *  根据字段的注解信息创建 `OddRpcReferenceBean` 实例，并设置相关属性
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public boolean postProcessAfterInstantiation(final Object bean, final String beanName) throws BeansException {

        // collection
        final Set<String> serviceKeyList = new HashSet<>();

        // 循环遍历所有被`@OddRpcReference` 注解标记的字段
        // parse OddRpcReferenceBean
        ReflectionUtils.doWithFields(bean.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
                if (field.isAnnotationPresent(OddRpcReference.class)){
                    //valid
                    Class iface = field.getType();
                    if (!iface.isInterface()){
                        throw new OddRpcException("odd-rpc, reference(OddRpcReference) must be interface.");
                    }

                    OddRpcReference rpcReference = field.getAnnotation(OddRpcReference.class);

                    //init referenceBean
                    OddRpcReferenceBean referenceBean = new OddRpcReferenceBean();
                    referenceBean.setClient(rpcReference.client());
                    referenceBean.setSerializer(rpcReference.serializer());
                    referenceBean.setCallType(rpcReference.callType());
                    referenceBean.setLoadBalance(rpcReference.loadBalance());
                    referenceBean.setIface(iface);
                    referenceBean.setVersion(rpcReference.version());
                    referenceBean.setTimeout(rpcReference.timeout());
                    referenceBean.setAddress(rpcReference.address());
                    referenceBean.setAccessToken(rpcReference.accessToken());
                    referenceBean.setInvokeCallback(null);
                    referenceBean.setInvokerFactory(oddRpcInvokerFactory);
                    
                    //get proxyObj
                    Object serviceProxy = null;
                    try {
                        serviceProxy = referenceBean.getObject();
                    } catch (Exception e) {
                        throw new OddRpcException(e);
                    }

                    // set bean
                    field.setAccessible(true);
                    field.set(bean, serviceProxy);

                    logger.info(">>>>>>>>>>> odd-rpc, invoker factory init reference bean success. serviceKey = {}, bean.field = {}.{}",
                            OddRpcProviderFactory.makeServiceKey(iface.getName(), rpcReference.version()), beanName, field.getName());

                    // collection
                    String serviceKey = OddRpcProviderFactory.makeServiceKey(iface.getName(), rpcReference.version());
                    serviceKeyList.add(serviceKey);
                }
            }
        });

        // multi discovery （服务发现）
        if (oddRpcInvokerFactory.getRegister() != null){
            try {
                oddRpcInvokerFactory.getRegister().discovery(serviceKeyList);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return true;
    }

    //在 Bean 销毁时被调用
    @Override
    public void destroy() throws Exception {
        // stop invoker factory
        oddRpcInvokerFactory.stop();
    }

    private BeanFactory beanFactory;

    // `BeanFactoryAware` 接口的实现，用于获取 Spring 的 `BeanFactory`
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
