package com.odd.rpc.core.remoting.invoker.route;

import com.odd.rpc.core.remoting.invoker.route.impl.*;

/**
 * @author oddity
 * @create 2023-11-27 11:45
 */
public enum LoadBalance {

    RANDOM(new OddRpcLoadBalanceRandomStrategy()),
    ROUND(new OddRpcLoadBalanceRoundStrategy()),
    LRU(new OddRpcLoadBalanceLRUStrategy()),
    LFU(new OddRpcLoadBalanceLFUStrategy()),
    CONSISTENT_HASH(new OddRpcLoadBalanceConsistentHashStrategy());

    public final OddRpcLoadBalance oddRpcInvokerRouter;

    private LoadBalance(OddRpcLoadBalance oddRpcInvokerRouter){
        this.oddRpcInvokerRouter = oddRpcInvokerRouter;
    }

    public static LoadBalance match(String name, LoadBalance defaultRouter){
        for (LoadBalance item : LoadBalance.values()){
            if (item.name().equals(name)){
                return item;
            }
        }
        return defaultRouter;
    }
}
