package com.odd.rpc.admin.test.clinet;

import com.odd.rpc.core.registry.impl.oddrpcadmin.OddRpcAdminRegistryBaseClient;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryDataItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * @author oddity
 * @create 2023-12-03 2:19
 */
public class OddRpcAdminRegistryBaseClientTest {

    public static void main(String[] args) throws InterruptedException {
        OddRpcAdminRegistryBaseClient registryClient = new OddRpcAdminRegistryBaseClient("http://localhost:8080/odd-rpc-admin/", null, "test");

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


        // remove test
        System.out.println("remove:" + registryClient.remove(registryDataList));
        TimeUnit.SECONDS.sleep(2);

        // discovery test
        System.out.println("discovery:" + registryClient.discovery(keys));

        // monitor test
        TimeUnit.SECONDS.sleep(10);
        System.out.println("monitor...");
        registryClient.monitor(keys);
    }
}
