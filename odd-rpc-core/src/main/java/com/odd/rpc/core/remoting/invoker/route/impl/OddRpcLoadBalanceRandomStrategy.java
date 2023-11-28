package com.odd.rpc.core.remoting.invoker.route.impl;

import com.odd.rpc.core.remoting.invoker.route.OddRpcLoadBalance;

import java.util.Random;
import java.util.TreeSet;

/**
 * 随机路由策略
 *
 * @author oddity
 * @create 2023-11-27 11:50
 */
public class OddRpcLoadBalanceRandomStrategy extends OddRpcLoadBalance {

    private Random random = new Random();

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        //arr
        String[] addressArr = addressSet.toArray(new String[addressSet.size()]);

        //random
        String finalAddress = addressArr[random.nextInt(addressSet.size())];
        return finalAddress;
    }
}
