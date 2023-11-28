package com.odd.rpc.core.remoting.invoker.reference.impl;

import com.odd.rpc.core.remoting.invoker.reference.OddRpcReferenceBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * rpc reference bean, use by spring xml and annotation (for spring)
 *
 * @author oddity
 * @create 2023-11-26 21:49
 */
public class OddRpcSpringReferenceBean implements FactoryBean<Object>, InitializingBean {

    // ---------------------- util ----------------------

    private OddRpcReferenceBean oddRpcReferenceBean;

    @Override
    public Object getObject() throws Exception {
        return null;
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }


}
