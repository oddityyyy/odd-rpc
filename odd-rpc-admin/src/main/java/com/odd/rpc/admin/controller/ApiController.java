package com.odd.rpc.admin.controller;

import com.odd.rpc.admin.controller.annotation.PermissionLimit;
import com.odd.rpc.admin.service.IOddRpcRegistryService;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryRequest;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryResponse;
import com.odd.rpc.core.util.GsonTool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @author oddity
 * @create 2023-12-01 14:13
 */

@Controller
@RequestMapping("/api")
public class ApiController {

    @Resource
    private IOddRpcRegistryService oddRpcRegistryService;

    @RequestMapping("/{uri}")
    @ResponseBody
    @PermissionLimit(limit = false)
    public Object api(@PathVariable("uri") String uri, @RequestBody(required = false) String data){

        if (uri==null || uri.trim().length()==0) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "invalid request, uri-mapping empty.");
        }

        // services mapping
        if ("registry".equals(uri)) {
            /**
             * 服务注册 & 续约 API
             * 说明：新服务注册上线1s内广播通知接入方；需要接入方循环续约，否则服务将会过期（三倍于注册中心心跳时间）下线；
             */
            OddRpcAdminRegistryRequest registryRequest = (OddRpcAdminRegistryRequest) GsonTool.fromJson(data, OddRpcAdminRegistryRequest.class);
            return oddRpcRegistryService.registry(registryRequest);
        } else if ("remove".equals(uri)) {
            /**
             * 服务摘除 API
             * 说明：新服务摘除下线1s内广播通知接入方；
             */
            OddRpcAdminRegistryRequest registryRequest = (OddRpcAdminRegistryRequest) GsonTool.fromJson(data, OddRpcAdminRegistryRequest.class);
            return oddRpcRegistryService.remove(registryRequest);
        } else if ("discovery".equals(uri)) {
            /**
             * 服务发现 API
             * 说明：查询在线服务地址列表；
             */
            OddRpcAdminRegistryRequest registryRequest = (OddRpcAdminRegistryRequest) GsonTool.fromJson(data, OddRpcAdminRegistryRequest.class);
            return oddRpcRegistryService.discovery(registryRequest);
        } else if ("monitor".equals(uri)) {
            /**
             * 服务监控 API
             * 说明：long-polling 接口，主动阻塞一段时间（三倍于注册中心心跳时间）；直至阻塞超时或服务注册信息变动时响应；
             */
            OddRpcAdminRegistryRequest registryRequest = (OddRpcAdminRegistryRequest) GsonTool.fromJson(data, OddRpcAdminRegistryRequest.class);
            return oddRpcRegistryService.monitor(registryRequest);
        } else {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "invalid request, uri-mapping("+ uri +") not found.");
        }

    }
}
