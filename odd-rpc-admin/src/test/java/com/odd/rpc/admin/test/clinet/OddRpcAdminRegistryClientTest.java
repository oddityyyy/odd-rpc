package com.odd.rpc.admin.test.clinet;

import com.odd.rpc.core.registry.impl.oddrpcadmin.OddRpcAdminRegistryClient;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryDataItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * @author oddity
 * @create 2023-12-03 2:20
 */
public class OddRpcAdminRegistryClientTest {

    public static void main(String[] args) throws InterruptedException {
        OddRpcAdminRegistryClient registryClient = new OddRpcAdminRegistryClient("http://localhost:8080/odd-rpc-admin/", null, "test");

        // registry test
        List<OddRpcAdminRegistryDataItem> registryDataList = new ArrayList<>();
        registryDataList.add(new OddRpcAdminRegistryDataItem("service01", "address01"));
        registryDataList.add(new OddRpcAdminRegistryDataItem("service02", "address02"));
        System.out.println("registry:" + registryClient.registry(registryDataList));
        TimeUnit.SECONDS.sleep(2);

        // discovery test
        Set<String> keys = new TreeSet<>();
        keys.add("service01");
        keys.add("service02");
        System.out.println("discovery:" + registryClient.discovery(keys));

        while (true) {
            TimeUnit.SECONDS.sleep(1);
        }

    }
    
}
