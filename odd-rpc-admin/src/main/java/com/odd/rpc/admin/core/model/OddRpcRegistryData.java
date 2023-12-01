package com.odd.rpc.admin.core.model;

import java.util.Date;

/**
 * @author oddity
 * @create 2023-12-01 15:50
 */
public class OddRpcRegistryData {

    private int id;
    private String env;         // 环境标识
    private String key;         // 注册Key
    private String value;       // 注册Value
    private Date updateTime;    // 更新时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
