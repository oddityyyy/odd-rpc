package com.odd.rpc.core.remoting.invoker.route.impl;

import com.odd.rpc.core.remoting.invoker.route.OddRpcLoadBalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * lfu
 *
 * 最不经常使用(频率/次数)页面置换算法，要求在页置换时置换引用技术最小的页
 *
 * @author oddity
 * @create 2023-11-27 11:49
 */
public class OddRpcLoadBalanceLFUStrategy extends OddRpcLoadBalance {

    private ConcurrentMap<String, HashMap<String, Integer>> jobLfuMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    public String doRoute(String serviceKey, TreeSet<String> addressSet){

        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME){
            jobLfuMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        // lfu item init
        HashMap<String, Integer> lfuItemMap = jobLfuMap.get(serviceKey); //Key排序可以用TreeMap+构造入参Compare；Value排序暂时只能通过ArrayList；
        if (lfuItemMap == null){
            lfuItemMap = new HashMap<String, Integer>();
            jobLfuMap.putIfAbsent(serviceKey, lfuItemMap); //避免重复覆盖
        }

        // put new
        for (String address : addressSet){
            if (!lfuItemMap.containsKey(address) || lfuItemMap.get(address) > 1000000){
                lfuItemMap.put(address, 0);
            }
        }

        // remove old
        List<String> delKeys = new ArrayList<>();
        for (String existKey : lfuItemMap.keySet()) {
            if (!addressSet.contains(existKey)) {
                delKeys.add(existKey);
            }
        }
        if (delKeys.size() > 0) {
            for (String delKey : delKeys) {
                lfuItemMap.remove(delKey);
            }
        }

        // load least used count address
        List<Map.Entry<String, Integer>> lfuItemList = new ArrayList<Map.Entry<String, Integer>>(lfuItemMap.entrySet());
        Collections.sort(lfuItemList, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        Map.Entry<String, Integer> addressItem = lfuItemList.get(0);
        String minAddress = addressItem.getKey();
        addressItem.setValue(addressItem.getValue() + 1);

        return minAddress;
    }

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        String finalAddress = doRoute(serviceKey, addressSet);
        return finalAddress;
    }
}
