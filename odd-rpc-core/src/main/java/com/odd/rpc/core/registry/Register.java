package com.odd.rpc.core.registry;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * application registry
 *
 * @author oddity
 * @create 2023-11-23 19:56
 */
public abstract class Register {

    /**
     * start
     * @param param
     */
    public abstract void start(Map<String, String> param);

    /**
     * stop
     */
    public abstract void stop();

    /**
     * registry service, for mult
     *
     * @param keys  service key 多个服务 （key = iface + version）
     * @param value service key 一台机器
     * @return
     */
    public abstract boolean registry(Set<String> keys, String value);

    /**
     * remove service, for mult
     *
     * @param keys
     * @param value
     * @return
     */
    public abstract boolean remove(Set<String> keys, String value);

    /**
     * discovery services, for mult
     * Map : key : 服务
     *       value: 机器地址集
     *
     * @param keys 服务集
     * @return
     */
    public abstract Map<String, TreeSet<String>> discovery(Set<String> keys);

    /**
     * discovery service, for one
     *
     * @param key   service key 单个服务
     * @return      service value/ip:port 机器地址集
     */
    public abstract TreeSet<String> discovery(String key);
}
