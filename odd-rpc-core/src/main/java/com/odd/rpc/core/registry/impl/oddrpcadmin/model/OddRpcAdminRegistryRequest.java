package com.odd.rpc.core.registry.impl.oddrpcadmin.model;

import java.util.List;

/**
 * @author oddity
 * @create 2023-11-30 15:05
 */
public class OddRpcAdminRegistryRequest {

    private String accessToken;
    private String env;

    private List<OddRpcAdminRegistryDataItem> registryDataList;
    private List<String> keys;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public List<OddRpcAdminRegistryDataItem> getRegistryDataList() {
        return registryDataList;
    }

    public void setRegistryDataList(List<OddRpcAdminRegistryDataItem> registryDataList) {
        this.registryDataList = registryDataList;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}
