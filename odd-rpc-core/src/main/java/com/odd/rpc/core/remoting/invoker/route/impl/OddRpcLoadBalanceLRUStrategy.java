package com.odd.rpc.core.remoting.invoker.route.impl;

import com.odd.rpc.core.remoting.invoker.route.OddRpcLoadBalance;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * LRU 最近最久未使用（时间），页面置换算法，选择最近最久未使用的页面予以淘汰
 *
 * @author oddity
 * @create 2023-11-27 11:49
 */
public class OddRpcLoadBalanceLRUStrategy extends OddRpcLoadBalance {

    private ConcurrentMap<String, LinkedHashMap<String, String>> jobLRUMap = new ConcurrentHashMap<>();
    private long CACHE_VALID_TIME = 0;

    public String doRoute(String serviceKey, TreeSet<String> addressSet){

        //cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            jobLRUMap.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        //init lru
        LinkedHashMap<String, String> lruItem = jobLRUMap.get(serviceKey);
        if (lruItem == null){
            /**
             * LinkedHashMap
             *      a、accessOrder：ture=访问顺序排序（get/put时排序）/ACCESS-LAST；false=插入顺序排期/FIFO；
             *      b、removeEldestEntry：新增元素时将会调用，返回true时会删除最老元素；可封装LinkedHashMap并重写该方法，比如定义最大容量，超出时返回true即可实现固定长度的LRU算法；
             */
            lruItem = new LinkedHashMap<String, String>(16, 0.75f, true){
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    if (super.size() > 1000){
                        return true;
                    }else {
                        return false;
                    }
                }
            };
            jobLRUMap.putIfAbsent(serviceKey, lruItem);
        }

        //put new
        for (String address : addressSet){
            if (!lruItem.containsKey(address)){
                lruItem.put(address, address); //入队尾
            }
        }

        //remove old
        List<String> delKeys = new ArrayList<>();
        for (String existKey : lruItem.keySet()){
            if (!addressSet.contains(existKey)){
                delKeys.add(existKey);
            }
        }
        if (delKeys.size() > 0){
            for (String delKey : delKeys){
                lruItem.remove(delKey);
            }
        }

        //load
        String eldestKey = lruItem.entrySet().iterator().next().getKey();
        String eldestValue = lruItem.get(eldestKey);
        return eldestValue;
    }

    /**
     * LRU 保证了每个机器都能均衡公平地调用，优先使用那些很久没有调用过地老机器
     * @param serviceKey
     * @param addressSet
     * @return
     */
    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        String finalAddress = doRoute(serviceKey, addressSet); //每次都拿最老的（队头），置换也置换最老的，若更新，调整到队列尾部
        return finalAddress;
    }
}
