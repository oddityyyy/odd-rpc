package com.odd.rpc.core.registry.impl;

import com.odd.rpc.core.registry.Register;
import com.odd.rpc.core.registry.impl.oddrpcadmin.OddRpcAdminRegistryClient;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryDataItem;

import java.util.*;

/**
 * application registry for "odd-rpc-admin"
 *
 * 对OddRpcAdminRegistryClient的上层封装
 *
 * @author oddity
 * @create 2023-11-26 0:14
 */
public class OddRpcAdminRegister extends Register {

    public static final String ADMIN_ADDRESS = "ADMIN_ADDRESS";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String ENV = "ENV";

    private OddRpcAdminRegistryClient oddRpcAdminRegistryClient;

    @Override
    public void start(Map<String, String> param) {
        String adminAddress = param.get(ADMIN_ADDRESS);
        String accessToken = param.get(ACCESS_TOKEN);
        String env = param.get(ENV);

        oddRpcAdminRegistryClient = new OddRpcAdminRegistryClient(adminAddress, accessToken, env);
    }

    @Override
    public void stop() {
        if (oddRpcAdminRegistryClient != null){
            oddRpcAdminRegistryClient.stop();
        }
    }

    @Override
    public boolean registry(Set<String> keys, String value) {
        if (keys == null || keys.size() == 0 || value == null || value.trim().length() == 0){
            return false;
        }

        //init
        List<OddRpcAdminRegistryDataItem> registryDataList = new ArrayList<>();
        for (String key : keys){
            registryDataList.add(new OddRpcAdminRegistryDataItem(key, value));
        }

        return oddRpcAdminRegistryClient.registry(registryDataList);
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        if (keys == null || keys.size() == 0 || value == null || value.trim().length() == 0){
            return false;
        }

        //init
        List<OddRpcAdminRegistryDataItem> registryDataList = new ArrayList<>();
        for (String key : keys){
            registryDataList.add(new OddRpcAdminRegistryDataItem(key, value));
        }

        return oddRpcAdminRegistryClient.remove(registryDataList);
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> appkeys) {
        return oddRpcAdminRegistryClient.discovery(appkeys);
    }

    @Override
    public TreeSet<String> discovery(String appkey) {
        return oddRpcAdminRegistryClient.discovery(appkey);
    }
}
