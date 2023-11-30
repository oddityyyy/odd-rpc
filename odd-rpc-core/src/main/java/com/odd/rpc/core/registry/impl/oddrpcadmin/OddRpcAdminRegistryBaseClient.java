package com.odd.rpc.core.registry.impl.oddrpcadmin;

import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryDataItem;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryRequest;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryResponse;
import com.odd.rpc.core.util.BasicHttpUtil;
import com.odd.rpc.core.util.GsonTool;
import com.odd.rpc.core.util.OddRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * base util for registry
 *
 * @author oddity
 * @create 2023-11-30 14:39
 */
public class OddRpcAdminRegistryBaseClient {

    private static Logger logger = LoggerFactory.getLogger(OddRpcAdminRegistryBaseClient.class);

    private String adminAddress;
    private String accessToken;
    private String env;

    private List<String> adminAddressArr;

    public OddRpcAdminRegistryBaseClient(String adminAddress, String accessToken, String env) {

        // fill
        this.adminAddress = adminAddress;
        this.accessToken = accessToken;
        this.env = env;

        // valid
        if (this.adminAddress == null || this.adminAddress.trim().length() == 0){
            throw new OddRpcException("odd-rpc, admin registry config[ADMIN_ADDRESS] empty.");
        }
        if (this.env == null || this.env.trim().length() < 2 || this.env.trim().length() > 255){
            throw new OddRpcException("odd-rpc, admin registry config[ENV] Invalid[2~255].");
        }

        // parse
        adminAddressArr = new ArrayList<>();
        if (this.adminAddress.contains(",")){
            adminAddressArr.addAll(Arrays.asList(this.adminAddress.split(",")));
        }else {
            adminAddressArr.add(adminAddress);
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

        // pathUrl
        String pathUrl = "/api/registry";

        // param
        OddRpcAdminRegistryRequest registryParamVO = new OddRpcAdminRegistryRequest();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setEnv(this.env);
        registryParamVO.setRegistryDataList(registryDataList);

        String paramsJson = GsonTool.toJson(registryParamVO);

        //result
        OddRpcAdminRegistryResponse respObj = requestAndValid(pathUrl, paramsJson, 5);
        return respObj != null;
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

        // pathUrl
        String pathUrl = "/api/remove";

        // param
        OddRpcAdminRegistryRequest registryParamVO = new OddRpcAdminRegistryRequest();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setEnv(this.env);
        registryParamVO.setRegistryDataList(registryDataList);

        String paramsJson = GsonTool.toJson(registryParamVO);

        //result
        OddRpcAdminRegistryResponse respObj = requestAndValid(pathUrl, paramsJson, 5);
        return respObj != null;
    }

    /**
     * discovery 服务发现，给一组key(iface + version)集合，查出各个key下对应的机器集合
     *
     * @param keys
     * @return
     */
    public Map<String, TreeSet<String>> discovery(Set<String> keys){
        // valid
        if (keys == null || keys.size() == 0){
            throw new OddRpcException("odd-rpc keys empty");
        }

        // pathUrl
        String pathUrl = "/api/discovery";

        // param
        OddRpcAdminRegistryRequest registryParamVO = new OddRpcAdminRegistryRequest();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setEnv(this.env);
        registryParamVO.setKeys(new ArrayList<String>(keys));

        String paramsJson = GsonTool.toJson(registryParamVO);

        // result
        OddRpcAdminRegistryResponse respObj = requestAndValid(pathUrl, paramsJson, 5);

        // parse
        if (respObj != null && respObj.getRegistryData() != null){
            return respObj.getRegistryData();
        }

        return null;
    }

    /**
     * monitor
     *
     * @param keys
     * @return
     */
    public boolean monitor(Set<String> keys){
        // valid
        if (keys == null || keys.size() == 0){
            throw new OddRpcException("odd-rpc keys empty");
        }

        // pathUrl
        String pathUrl = "/api/monitor";

        // param
        OddRpcAdminRegistryRequest registryParamVO = new OddRpcAdminRegistryRequest();
        registryParamVO.setAccessToken(this.accessToken);
        registryParamVO.setEnv(this.env);
        registryParamVO.setKeys(new ArrayList<String>(keys));

        String paramsJson = GsonTool.toJson(registryParamVO);

        // result
        OddRpcAdminRegistryResponse respObj = requestAndValid(pathUrl, paramsJson, 60);
        return respObj != null;
    }

    // ---------------------- net for registry

    private OddRpcAdminRegistryResponse requestAndValid(String pathUrl, String requestBody, int timeout){

        //轮询，一旦合法就返回
        for (String adminAddressUrl : adminAddressArr){
            String finalUrl = adminAddressUrl + pathUrl;

            // request
            String responseData = BasicHttpUtil.postBody(finalUrl, requestBody, timeout);
            if (responseData == null){
                return null;
            }

            // parse response
            OddRpcAdminRegistryResponse responseMap = null;
            try {
                responseMap = GsonTool.fromJson(responseData, OddRpcAdminRegistryResponse.class);
            } catch (Exception e) {
                logger.debug("OddRegistryBaseClient response error, responseData={}", responseData);
            }

            // valid response
            if (responseMap != null && responseMap.getCode() == OddRpcAdminRegistryResponse.SUCCESS_CODE){
                return responseMap;
            }else {
                logger.warn("OddRegistryBaseClient response fail, responseData={}", responseData);
            }
        }
        return null;
    }
}
