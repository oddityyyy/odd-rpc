package com.odd.rpc.core.remoting.invoker.route.impl;

import com.odd.rpc.core.remoting.invoker.route.OddRpcLoadBalance;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * round 轮询策略(基于计数器)
 *
 * 通过维护计数器来实现轮询的负载均衡策略，
 * 每次根据计数器选择下一个服务地址。这种方法确保了每个地址被平均选中，
 * 以实现基本的负载均衡。
 *
 * @author oddity
 * @create 2023-11-27 11:51
 */
public class OddRpcLoadBalanceRoundStrategy extends OddRpcLoadBalance {

    private ConcurrentMap<String, AtomicInteger> routeCountEachJob = new ConcurrentHashMap<String, AtomicInteger>();
    private long CACHE_VALID_TIME = 0;

    private int count(String serviceKey){
        //cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME){
            routeCountEachJob.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 24 * 60 * 60 * 1000; //one day
        }

        //count++
        AtomicInteger count = routeCountEachJob.get(serviceKey);
        if (count == null || count.get() > 1000000){
            //初始化时主动Random一次，缓解首次压力
            count = new AtomicInteger(new Random().nextInt(100)); //[0, 100)
        }else{
            //count++
            count.addAndGet(1);
        }

        routeCountEachJob.put(serviceKey, count);
        return count.get();
    }

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        //arr
        String[] addressArr = addressSet.toArray(new String[addressSet.size()]);
        //round
        String finalAddress = addressArr[count(serviceKey) % addressArr.length];
        return finalAddress;
    }
}
