package com.odd.rpc.admin.service;

import com.odd.rpc.admin.core.model.OddRpcRegistry;
import com.odd.rpc.admin.core.result.ReturnT;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryRequest;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryResponse;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

/**
 * @author oddity
 * @create 2023-12-01 14:37
 */
public interface IOddRpcRegistryService {

    // admin
    Map<String,Object> pageList(int start, int length, String env, String key);
    ReturnT<String> delete(int id);
    ReturnT<String> update(OddRpcRegistry oddRpcRegistry);
    ReturnT<String> add(OddRpcRegistry oddRpcRegistry);


    // ------------------------ remote registry ------------------------

    /**
     * refresh registry-value, check update and broacase
     */
    OddRpcAdminRegistryResponse registry(OddRpcAdminRegistryRequest registryRequest);

    /**
     * remove registry-value, check update and broacase
     */
    OddRpcAdminRegistryResponse remove(OddRpcAdminRegistryRequest registryRequest);

    /**
     * discovery registry-data, read file
     */
    OddRpcAdminRegistryResponse discovery(OddRpcAdminRegistryRequest registryRequest);

    /**
     * monitor update
     */
    DeferredResult<OddRpcAdminRegistryResponse> monitor(OddRpcAdminRegistryRequest registryRequest);
}
