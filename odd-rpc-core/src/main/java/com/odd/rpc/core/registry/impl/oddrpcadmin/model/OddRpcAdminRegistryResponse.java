package com.odd.rpc.core.registry.impl.oddrpcadmin.model;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author oddity
 * @create 2023-11-30 15:07
 */
public class OddRpcAdminRegistryResponse implements Serializable {

    public static final long serialVersionUID = 42L;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;

    private int code;
    private String msg;
    private Map<String, TreeSet<String>> registryData;

    public OddRpcAdminRegistryResponse() {
    }

    public OddRpcAdminRegistryResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public OddRpcAdminRegistryResponse(Map<String, TreeSet<String>> registryData) {
        this.code = SUCCESS_CODE;
        this.registryData = registryData;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, TreeSet<String>> getRegistryData() {
        return registryData;
    }

    public void setRegistryData(Map<String, TreeSet<String>> registryData) {
        this.registryData = registryData;
    }

    @Override
    public String toString() {
        return "OddRpcAdminRegistryResponse{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", registryData=" + registryData +
                '}';
    }
}
