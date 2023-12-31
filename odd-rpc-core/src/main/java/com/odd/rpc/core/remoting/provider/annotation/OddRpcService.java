package com.odd.rpc.core.remoting.provider.annotation;

import java.lang.annotation.*;

/**
 * rpc service annotation, skeleton of stub ("@Inherited" allow service use "Transactional")
 *
 * 此注解标注在实现了公共接口的service Bean上
 *
 * @author oddity
 * @create 2023-11-20 20:28
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface OddRpcService {

    /**
     * 提供服务的版本
     * @return
     */
    String version() default "";
}
