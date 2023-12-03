package com.odd.rpc.core.registry.impl.oddrpcadmin;

import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryDataItem;
import com.odd.rpc.core.util.GsonTool;
import com.odd.rpc.core.util.OddRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * registry client, auto heartbeat registry info, auto monitor discovery info
 *
 * 对OddRpcAdminRegistryBaseClient的上层封装
 *
 * @author oddity
 * @create 2023-11-30 12:30
 */
public class OddRpcAdminRegistryClient {

    private static Logger logger = LoggerFactory.getLogger(OddRpcAdminRegistryBaseClient.class);

    // 本地缓存
    private volatile Set<OddRpcAdminRegistryDataItem> registryData = new HashSet<>();
    private volatile ConcurrentMap<String, TreeSet<String>> discoveryData = new ConcurrentHashMap<>();

    private Thread registryThread;
    private Thread discoveryThread;
    private volatile boolean registryThreadStop = false;

    private OddRpcAdminRegistryBaseClient registryBaseClient;

    public OddRpcAdminRegistryClient(String adminAddress, String accessToken, String env){
        // init
        registryBaseClient = new OddRpcAdminRegistryBaseClient(adminAddress, accessToken, env);
        logger.info(">>>>>>>>>>> odd-rpc, OddRpcAdminRegistryClient init .... [adminAddress={}, accessToken={}, env={}]", adminAddress, accessToken, env);

        // registry thread 服务注册线程循环注册（从本地缓存中）不断往复，直到stop，每10s注册一次(刷新本地缓存到远程)
        // 这里不清空registryData的原因可能是要保持心跳连接
        // 其实更大的原因是要双重保证服务注册，因为注册中心有可能宕机，在本地缓存中存一份，若宕机，下次注册中心重启时，可以存缓存中通过远程注册到注册中心rpcAdmin
        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!registryThreadStop){
                    try {
                        if (registryData.size() > 0){
                            boolean ret = registryBaseClient.registry(new ArrayList<OddRpcAdminRegistryDataItem>(registryData));
                            logger.debug(">>>>>>>>>>> odd-rpc, refresh registry data {}, registryData = {}", ret ? "success" : "fail", registryData);
                        }
                    } catch (Exception e) {
                        if (!registryThreadStop){
                            logger.error(">>>>>>>>>>> odd-rpc, registryThread error.", e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (Exception e) {
                        if (!registryThreadStop){
                            logger.error(">>>>>>>>>>> odd-rpc, registryThread error.", e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> odd-rpc, registryThread stopped.");
            }
        });
        registryThread.setName("odd-rpc, OddRpcAdminRegistryClient registryThread.");
        registryThread.setDaemon(true);
        registryThread.start();

        // discovery thread 服务发现线程循环发现服务不断往复，直到RegistryClient stop,每3s发现一次
        discoveryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!registryThreadStop){
                    if(discoveryData.size() == 0){
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (Exception e) {
                            if (!registryThreadStop){
                                logger.error(">>>>>>>>>>> odd-rpc, discoveryThread error.", e);
                            }
                        }
                    } else {
                        try {
                            // TODO 这块具体监控什么事件，初步猜测是单个key的机器地址集发生变化，那么将会刷新本地缓存
                            // monitor
                            boolean monitorRet = registryBaseClient.monitor(discoveryData.keySet());

                            // avoid fail-retry request too quick
                            if (!monitorRet){
                                TimeUnit.SECONDS.sleep(10);
                            }

                            // 刷新本地缓存
                            // refreshDiscoveryData, all
                            refreshDiscoveryData(discoveryData.keySet());
                        } catch (Exception e) {
                            if (!registryThreadStop){
                                logger.error(">>>>>>>>>>> odd-rpc, discoveryThread error.", e);
                            }
                        }
                    }
                }
                logger.info(">>>>>>>>>>> odd-rpc, discoveryThread stopped.");
            }
        });
        discoveryThread.setName("odd-rpc, OddRegistryClient discoveryThread.");
        discoveryThread.setDaemon(true);
        discoveryThread.start();

        logger.info(">>>>>>>>>>> odd-rpc, OddRegistryClient init success.");
    }

    public void stop() {
        registryThreadStop = true;
        if (registryThread != null){
            registryThread.interrupt();
        }
        if (discoveryThread != null){
            discoveryThread.interrupt();
        }
    }

    /**
     * registry
     *
     * @param registryDataList
     * @return
     */
    public boolean registry(List<OddRpcAdminRegistryDataItem> registryDataList){
        // valid
        if (registryDataList == null || registryDataList.size() == 0){
            throw new OddRpcException("odd-rpc registryDataList empty");
        }

        for (OddRpcAdminRegistryDataItem registryParam : registryDataList){
            if (registryParam.getKey() == null || registryParam.getKey().trim().length() < 4 || registryParam.getKey().trim().length() > 255){
                throw new OddRpcException("odd-rpc registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue() == null || registryParam.getValue().trim().length() < 4 || registryParam.getValue().trim().length() > 255){
                throw new OddRpcException("odd-rpc registryDataList#value Invalid[4~255]");
            }
        }

        // cache 从缓存中添加
        registryData.addAll(registryDataList);

        // remote 从注册中心添加
        registryBaseClient.registry(registryDataList);

        return true;
    }

    /**
     * remove
     *
     * @param registryDataList
     * @return
     */
    public boolean remove(List<OddRpcAdminRegistryDataItem> registryDataList){
        // valid
        if (registryDataList == null || registryDataList.size() == 0){
            throw new OddRpcException("odd-rpc registryDataList empty");
        }

        for (OddRpcAdminRegistryDataItem registryParam : registryDataList){
            if (registryParam.getKey() == null || registryParam.getKey().trim().length() < 4 || registryParam.getKey().trim().length() > 255){
                throw new OddRpcException("odd-rpc registryDataList#key Invalid[4~255]");
            }
            if (registryParam.getValue() == null || registryParam.getValue().trim().length() < 4 || registryParam.getValue().trim().length() > 255){
                throw new OddRpcException("odd-rpc registryDataList#value Invalid[4~255]");
            }
        }

        // cache 从缓存中删除
        registryData.removeAll(registryDataList);

        // remote 从注册中心删除
        registryBaseClient.remove(registryDataList);

        return true;
    }

    /**
     * discovery 在扫描注解OddRpcReference的时候服务发现，第一次调用时, keys是扫描所有相关注解得到的接口+版本
     *
     * @param keys
     * @return
     */
    public Map<String, TreeSet<String>> discovery(Set<String> keys){
        // valid
        if (keys == null || keys.size() == 0){
            return null;
        }

        // find from local
        Map<String, TreeSet<String>> registryDataTmp = new HashMap<String, TreeSet<String>>();
        for (String key : keys){
            TreeSet<String> valueSet = discoveryData.get(key);
            if (valueSet != null){
                registryDataTmp.put(key, valueSet);
            }
        }

        // not find all, find from remote
        if (keys.size() != registryDataTmp.size()){

            // refreshDiscoveryData, some, first use
            refreshDiscoveryData(keys);

            //此时本地缓存中保存的是更新后的数据
            //再重新覆盖一遍
            //find from local
            for(String key : keys){
                TreeSet<String> valueSet = discoveryData.get(key);
                if (valueSet != null){
                    registryDataTmp.put(key, valueSet);
                }
            }
        }

        return registryDataTmp;
    }

    /**
     * refreshDiscoveryData, some or all
     */
    private void refreshDiscoveryData(Set<String> keys){
        if (keys == null || keys.size() == 0){
            return;
        }

        // discovery multi
        Map<String, TreeSet<String>> updatedData = new HashMap<>();

        Map<String, TreeSet<String>> keyValueListData = registryBaseClient.discovery(keys);

        if (keyValueListData != null){
            for (String keyItem : keyValueListData.keySet()){
                // list > set
                TreeSet<String> valueSet = new TreeSet<>();
                valueSet.addAll(keyValueListData.get(keyItem));
                // valid if updated
                boolean updated = true;
                TreeSet<String> oldValSet = discoveryData.get(keyItem);
                if (oldValSet != null && GsonTool.toJson(oldValSet).equals(GsonTool.toJson(valueSet))){
                    updated = false;
                }

                // set
                if (updated) {
                    // 此处第一次调用的时候才是真正意义上的初始化discoveryData数据
                    discoveryData.put(keyItem, valueSet);
                    updatedData.put(keyItem, valueSet);
                }
            }
        }

        // 打印更新了的数据
        if (updatedData.size() > 0){
            logger.info(">>>>>>>>>>> odd-rpc, refresh discovery data finish, discoveryData(updated) = {}", updatedData);
        }

        // 打印全部更新后的数据
        logger.debug(">>>>>>>>>>> odd-rpc, refresh discovery data finish, discoveryData = {}", discoveryData);
    }

    public TreeSet<String> discovery(String key){
        if (key == null) {
            return null;
        }

        Map<String, TreeSet<String>> keyValueSetTmp = discovery(new HashSet<String>(Arrays.asList(key)));
        if (keyValueSetTmp != null){
            return keyValueSetTmp.get(key);
        }
        return null;
    }
}
