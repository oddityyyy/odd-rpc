package com.odd.rpc.core.remoting.invoker.route.impl;

import com.odd.rpc.core.remoting.invoker.route.OddRpcLoadBalance;
import com.odd.rpc.core.util.OddRpcException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * consustent hash
 *
 * @author oddity
 * @create 2023-11-27 11:47
 */
public class OddRpcLoadBalanceConsistentHashStrategy extends OddRpcLoadBalance {

    private int VIRTUAL_NODE_NUM = 100;

    private long hash(String key){

        // md5 byte
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new OddRpcException("MD5 not supported", e);
        }
        md5.reset();
        byte[] keyBytes = null;
        try {
            keyBytes = key.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new OddRpcException("Unknown string :" + key, e);
        }

        md5.update(keyBytes);
        byte[] digest = md5.digest();

        // hash code, Truncate to 32-bits
        long hashCode = ((long) (digest[3] & 0xFF) << 24)
                | ((long) (digest[2] & 0xFF) << 16)
                | ((long) (digest[1] & 0xFF) << 8)
                | (digest[0] & 0xFF);

        long truncateHashCode = hashCode & 0xffffffffL;
        return truncateHashCode;
    }

    /**
     * 利用了一致性哈希算法的思想，将服务地址映射到一个虚拟环上，并通过计算服务键的哈希值来选择一个合适的服务地址，
     * 以用于路由请求。这种方法具有较好的均衡性和扩展性，使得在动态服务增减的情况下，请求可以比较平均地分布到不同的节点上。
     *
     * @param serviceKey
     * @param addressSet
     * @return
     */
    public String doRoute(String serviceKey, TreeSet<String> addressSet){

        // 构建地址环
        TreeMap<Long, String> addressRing = new TreeMap<Long, String>();
        for (String address : addressSet) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++){
                long addressHash = hash("SHARD-" + address + "-NODE-" + i);
                addressRing.put(addressHash, address);
            }
        }

        // 计算服务的哈希值
        long jobHash = hash(serviceKey);
        // 通过 `addressRing.tailMap(jobHash)` 获取大于等于 `jobHash` 的部分环，
        SortedMap<Long, String> lastRing = addressRing.tailMap(jobHash);
        if (!lastRing.isEmpty()){
            // 并返回第一个节点作为目标地址。
            // 这个操作用于在环上找到比服务哈希值 `jobHash` 大的最近的地址。
            return lastRing.get(lastRing.firstKey());
        }
        // 如果找不到大于 `jobHash` 的节点，则返回环上的第一个节点作为目标地址。
        return addressRing.firstEntry().getValue();
    }

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        String finalAddress = doRoute(serviceKey, addressSet);
        return finalAddress;
    }
}
