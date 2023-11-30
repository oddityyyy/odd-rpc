package com.odd.rpc.core.remoting.invoker.annotaion;

import com.odd.rpc.core.remoting.invoker.call.CallType;
import com.odd.rpc.core.remoting.invoker.route.LoadBalance;
import com.odd.rpc.core.remoting.net.Client;
import com.odd.rpc.core.remoting.net.impl.netty.client.NettyClient;
import com.odd.rpc.core.serialize.Serializer;
import com.odd.rpc.core.serialize.impl.HessianSerializer;

import java.lang.annotation.*;

/**
 * rpc service annotation, skeleton of stub ("@Inherited" allow service use "Transactional")
 *
 * 此注解标注在公共接口属性变量上
 *
 * Stub 位于客户端，是客户端用来调用远程服务的本地代理对象；
 * 而 Skeleton 位于服务器端，是服务器用来处理客户端请求并实际执行远程服务的代理对象
 *
 * @author oddity
 * @create 2023-11-25 22:27
 */

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited //指定子类从其父类继承此注释
public @interface OddRpcReference {

    Class<? extends Client> client() default NettyClient.class;
    Class<? extends Serializer> serializer() default HessianSerializer.class;
    CallType callType() default CallType.SYNC;
    LoadBalance loadBalance() default LoadBalance.ROUND;

    //Class<?> iface;
    String version() default "";

    long timeout() default 1000;

    String address() default "";
    String accessToken() default "";
}
