package com.odd.rpc.core.registry.impl.oddrpcadmin.model;

import java.util.Objects;

/**
 * @author oddity
 * @create 2023-11-30 14:55
 */
public class OddRpcAdminRegistryDataItem {

    private String key;
    private String value;

    public OddRpcAdminRegistryDataItem() {
    }

    public OddRpcAdminRegistryDataItem(String key, String value) {
        this.key = key;
        this.value = value;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OddRpcAdminRegistryDataItem that = (OddRpcAdminRegistryDataItem) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "OddRpcAdminRegistryDataItem{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
