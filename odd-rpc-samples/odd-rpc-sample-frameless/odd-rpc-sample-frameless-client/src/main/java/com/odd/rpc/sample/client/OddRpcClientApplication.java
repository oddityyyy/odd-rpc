package com.odd.rpc.sample.client;

import com.odd.rpc.core.remoting.invoker.OddRpcInvokerFactory;
import com.odd.rpc.core.remoting.invoker.call.CallType;
import com.odd.rpc.core.remoting.invoker.call.OddRpcInvokeCallback;
import com.odd.rpc.core.remoting.invoker.call.OddRpcInvokeFuture;
import com.odd.rpc.core.remoting.invoker.reference.OddRpcReferenceBean;
import com.odd.rpc.core.remoting.invoker.route.LoadBalance;
import com.odd.rpc.core.remoting.net.impl.netty.client.NettyClient;
import com.odd.rpc.core.serialize.impl.HessianSerializer;
import com.odd.rpc.sample.api.DemoService;
import com.odd.rpc.sample.api.dto.UserDTO;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author oddity
 * @create 2023-12-03 15:26
 */
public class OddRpcClientApplication {

    public static void main(String[] args) throws Exception {

		/*String serviceKey = OddRpcProviderFactory.makeServiceKey(DemoService.class.getName(), null);
		OddRpcInvokerFactory.getInstance().getServiceRegistry().registry(new HashSet<String>(Arrays.asList(serviceKey)), "127.0.0.1:7080");*/

        // test
        testSYNC();
        testFUTURE();
        testCALLBACK();
        testONEWAY();

        TimeUnit.SECONDS.sleep(2);

        // stop client invoker factory (default by getInstance, exist inner thread, need destory)
        OddRpcInvokerFactory.getInstance().stop();

    }



    /**
     * CallType.SYNC
     */
    public static void testSYNC() throws Exception {
        // init client
        OddRpcReferenceBean referenceBean = new OddRpcReferenceBean();
        referenceBean.setClient(NettyClient.class);
        referenceBean.setSerializer(HessianSerializer.class);
        referenceBean.setCallType(CallType.SYNC);
        referenceBean.setLoadBalance(LoadBalance.ROUND);
        referenceBean.setIface(DemoService.class);
        referenceBean.setVersion(null);
        referenceBean.setTimeout(500);
        referenceBean.setAddress("127.0.0.1:7080");
        referenceBean.setAccessToken(null);
        referenceBean.setInvokeCallback(null);
        referenceBean.setInvokerFactory(null);

        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        UserDTO userDTO = demoService.sayHi("[SYNC]jack");
        System.out.println(userDTO);


        // test mult
		/*int count = 100;
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			UserDTO userDTO2 = demoService.sayHi("[SYNC]jack"+i );
			System.out.println(i + "##" + userDTO2.toString());
		}
		long end = System.currentTimeMillis();
    	System.out.println("run count:"+ count +", cost:" + (end - start));*/

    }


    /**
     * CallType.FUTURE
     */
    public static void testFUTURE() throws Exception {
        // client test
        OddRpcReferenceBean referenceBean = new OddRpcReferenceBean();
        referenceBean.setClient(NettyClient.class);
        referenceBean.setSerializer(HessianSerializer.class);
        referenceBean.setCallType(CallType.FUTURE);
        referenceBean.setLoadBalance(LoadBalance.ROUND);
        referenceBean.setIface(DemoService.class);
        referenceBean.setVersion(null);
        referenceBean.setTimeout(500);
        referenceBean.setAddress("127.0.0.1:7080");
        referenceBean.setAccessToken(null);
        referenceBean.setInvokeCallback(null);
        referenceBean.setInvokerFactory(null);

        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        demoService.sayHi("[FUTURE]jack" );
        Future<UserDTO> userDTOFuture = OddRpcInvokeFuture.getFuture(UserDTO.class);
        UserDTO userDTO = userDTOFuture.get();

        System.out.println(userDTO.toString());
    }


    /**
     * CallType.CALLBACK
     */
    public static void testCALLBACK() throws Exception {
        // client test
        OddRpcReferenceBean referenceBean = new OddRpcReferenceBean();
        referenceBean.setClient(NettyClient.class);
        referenceBean.setSerializer(HessianSerializer.class);
        referenceBean.setCallType(CallType.CALLBACK);
        referenceBean.setLoadBalance(LoadBalance.ROUND);
        referenceBean.setIface(DemoService.class);
        referenceBean.setVersion(null);
        referenceBean.setTimeout(500);
        referenceBean.setAddress("127.0.0.1:7080");
        referenceBean.setAccessToken(null);
        referenceBean.setInvokeCallback(null);
        referenceBean.setInvokerFactory(null);

        DemoService demoService = (DemoService) referenceBean.getObject();


        // test
        OddRpcInvokeCallback.setCallback(new OddRpcInvokeCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                System.out.println(result);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        demoService.sayHi("[CALLBACK]jack");
    }


    /**
     * CallType.ONEWAY
     */
    public static void testONEWAY() throws Exception {
        // client test
        OddRpcReferenceBean referenceBean = new OddRpcReferenceBean();
        referenceBean.setClient(NettyClient.class);
        referenceBean.setSerializer(HessianSerializer.class);
        referenceBean.setCallType(CallType.ONEWAY);
        referenceBean.setLoadBalance(LoadBalance.ROUND);
        referenceBean.setIface(DemoService.class);
        referenceBean.setVersion(null);
        referenceBean.setTimeout(500);
        referenceBean.setAddress("127.0.0.1:7080");
        referenceBean.setAccessToken(null);
        referenceBean.setInvokeCallback(null);
        referenceBean.setInvokerFactory(null);

        DemoService demoService = (DemoService) referenceBean.getObject();

        // test
        demoService.sayHi("[ONEWAY]jack");
    }
}
