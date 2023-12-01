package com.odd.rpc.admin.service.impl;

import com.odd.rpc.admin.core.model.OddRpcRegistry;
import com.odd.rpc.admin.core.model.OddRpcRegistryData;
import com.odd.rpc.admin.core.model.OddRpcRegistryMessage;
import com.odd.rpc.admin.core.result.ReturnT;
import com.odd.rpc.admin.core.util.PropUtil;
import com.odd.rpc.admin.dao.IOddRpcRegistryDao;
import com.odd.rpc.admin.dao.IOddRpcRegistryDataDao;
import com.odd.rpc.admin.dao.IOddRpcRegistryMessageDao;
import com.odd.rpc.admin.service.IOddRpcRegistryService;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryDataItem;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryRequest;
import com.odd.rpc.core.registry.impl.oddrpcadmin.model.OddRpcAdminRegistryResponse;
import com.odd.rpc.core.util.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author oddity
 * @create 2023-12-01 14:44
 */
public class OddRpcRegistryServiceImpl implements IOddRpcRegistryService, InitializingBean, DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(OddRpcRegistryServiceImpl.class);

    @Resource
    private IOddRpcRegistryDao oddRpcRegistryDao;
    @Resource
    private IOddRpcRegistryDataDao oddRpcRegistryDataDao;
    @Resource
    private IOddRpcRegistryMessageDao oddRpcRegistryMessageDao;

    @Value("${odd.rpc.registry.data.filepath}")
    private String registryDataFilePath;
    @Value("${odd.rpc.registry.accessToken}")
    private String accessToken;

    private int registryBeatTime = 10;


    @Override
    public Map<String, Object> pageList(int start, int length, String env, String key) {

        // page list
        List<OddRpcRegistry> list = oddRpcRegistryDao.pageList(start, length, env, key);
        int list_count = oddRpcRegistryDao.pageListCount(start, length, env, key);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @Override
    public ReturnT<String> delete(int id) {
        OddRpcRegistry oddRpcRegistry = oddRpcRegistryDao.loadById(id);
        if (oddRpcRegistry != null) {
            oddRpcRegistryDao.delete(id);
            oddRpcRegistryDataDao.deleteData(oddRpcRegistry.getEnv(), oddRpcRegistry.getKey());

            // sendRegistryDataUpdateMessage (delete)
            oddRpcRegistry.setData("");
            sendRegistryDataUpdateMessage(oddRpcRegistry);
        }

        return ReturnT.SUCCESS;
    }

    /**
     * send RegistryData Update Message
     */
    private void sendRegistryDataUpdateMessage(OddRpcRegistry oddRpcRegistry){
        String registryUpdateJson = GsonTool.toJson(oddRpcRegistry);

        OddRpcRegistryMessage registryMessage = new OddRpcRegistryMessage();
        registryMessage.setType(0);
        registryMessage.setData(registryUpdateJson);
        oddRpcRegistryMessageDao.add(registryMessage);
    }

    @Override
    public ReturnT<String> update(OddRpcRegistry oddRpcRegistry) {

        // valid
        if (oddRpcRegistry.getEnv()==null || oddRpcRegistry.getEnv().trim().length()<2 || oddRpcRegistry.getEnv().trim().length()>255 ) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "环境格式非法[2~255]");
        }
        if (oddRpcRegistry.getKey()==null || oddRpcRegistry.getKey().trim().length()<4 || oddRpcRegistry.getKey().trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Key格式非法[4~255]");
        }
        if (oddRpcRegistry.getData()==null || oddRpcRegistry.getData().trim().length()==0) {
            oddRpcRegistry.setData(GsonTool.toJson(new ArrayList<String>()));
        }
        List<String> valueList = GsonTool.fromJson(oddRpcRegistry.getData(), List.class);
        if (valueList == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Value数据格式非法；限制为字符串数组JSON格式，如 [address,address2]");
        }

        // valid exist
        OddRpcRegistry exist = oddRpcRegistryDao.loadById(oddRpcRegistry.getId());
        if (exist == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "ID参数非法");
        }

        // if refresh
        boolean needMessage = !oddRpcRegistry.getData().equals(exist.getData());

        int ret = oddRpcRegistryDao.update(oddRpcRegistry);
        needMessage = ret>0 ? needMessage : false;

        if (needMessage) {
            // sendRegistryDataUpdateMessage (update)
            sendRegistryDataUpdateMessage(oddRpcRegistry);
        }

        return ret>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> add(OddRpcRegistry oddRpcRegistry) {

        // valid
        if (oddRpcRegistry.getEnv()==null || oddRpcRegistry.getEnv().trim().length()<2 || oddRpcRegistry.getEnv().trim().length()>255 ) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "环境格式非法[2~255]");
        }
        if (oddRpcRegistry.getKey()==null || oddRpcRegistry.getKey().trim().length()<4 || oddRpcRegistry.getKey().trim().length()>255) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Key格式非法[4~255]");
        }
        if (oddRpcRegistry.getData()==null || oddRpcRegistry.getData().trim().length()==0) {
            oddRpcRegistry.setData(GsonTool.toJson(new ArrayList<String>()));
        }
        List<String> valueList = GsonTool.fromJson(oddRpcRegistry.getData(), List.class);
        if (valueList == null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Value数据格式非法；限制为字符串数组JSON格式，如 [address,address2]");
        }

        // valid exist
        OddRpcRegistry exist = oddRpcRegistryDao.load(oddRpcRegistry.getEnv(), oddRpcRegistry.getKey());
        if (exist != null) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "注册Key请勿重复");
        }

        int ret = oddRpcRegistryDao.add(oddRpcRegistry);
        boolean needMessage = ret>0 ? true : false;

        if (needMessage) {
            // sendRegistryDataUpdateMessage (add)
            sendRegistryDataUpdateMessage(oddRpcRegistry);
        }

        return ret>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    // ------------------------ remote registry ------------------------
    
    @Override
    public OddRpcAdminRegistryResponse registry(OddRpcAdminRegistryRequest registryRequest) {

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(registryRequest.getAccessToken())) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "AccessToken Invalid");
        }
        if (registryRequest.getEnv()==null || registryRequest.getEnv().trim().length()<2 || registryRequest.getEnv().trim().length()>255) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (registryRequest.getRegistryDataList()==null || registryRequest.getRegistryDataList().size()==0) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Registry DataList Invalid");
        }
        for (OddRpcAdminRegistryDataItem registryData: registryRequest.getRegistryDataList()) {
            if (registryData.getKey()==null || registryData.getKey().trim().length()<4 || registryData.getKey().trim().length()>255) {
                return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Registry Key Invalid[4~255]");
            }
            if (registryData.getValue()==null || registryData.getValue().trim().length()<4 || registryData.getValue().trim().length()>255) {
                return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Registry Value Invalid[4~255]");
            }
        }

        // fill + add queue
        List<OddRpcRegistryData> registryDataList = new ArrayList<>();
        for (OddRpcAdminRegistryDataItem dataItem: registryRequest.getRegistryDataList()) {
            OddRpcRegistryData registryData = new OddRpcRegistryData();
            registryData.setEnv(registryRequest.getEnv());
            registryData.setKey(dataItem.getKey());
            registryData.setValue(dataItem.getValue());

            registryDataList.add(registryData);
        }
        registryQueue.addAll(registryDataList);

        return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.SUCCESS_CODE, null);
    }

    @Override
    public OddRpcAdminRegistryResponse remove(OddRpcAdminRegistryRequest registryRequest) {
        
        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(registryRequest.getAccessToken())) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "AccessToken Invalid");
        }
        if (registryRequest.getEnv()==null || registryRequest.getEnv().trim().length()<2 || registryRequest.getEnv().trim().length()>255) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (registryRequest.getRegistryDataList()==null || registryRequest.getRegistryDataList().size()==0) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Registry DataList Invalid");
        }
        for (OddRpcAdminRegistryDataItem registryData: registryRequest.getRegistryDataList()) {
            if (registryData.getKey()==null || registryData.getKey().trim().length()<4 || registryData.getKey().trim().length()>255) {
                return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Registry Key Invalid[4~255]");
            }
            if (registryData.getValue()==null || registryData.getValue().trim().length()<4 || registryData.getValue().trim().length()>255) {
                return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Registry Value Invalid[4~255]");
            }
        }

        // fill + add queue
        List<OddRpcRegistryData> registryDataList = new ArrayList<>();
        for (OddRpcAdminRegistryDataItem dataItem: registryRequest.getRegistryDataList()) {
            OddRpcRegistryData registryData = new OddRpcRegistryData();
            registryData.setEnv(registryRequest.getEnv());
            registryData.setKey(dataItem.getKey());
            registryData.setValue(dataItem.getValue());

            registryDataList.add(registryData);
        }
        removeQueue.addAll(registryDataList);

        return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.SUCCESS_CODE, null);
    }

    @Override
    public OddRpcAdminRegistryResponse discovery(OddRpcAdminRegistryRequest registryRequest) {

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(registryRequest.getAccessToken())) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "AccessToken Invalid");
        }
        if (registryRequest.getEnv()==null || registryRequest.getEnv().trim().length()<2 || registryRequest.getEnv().trim().length()>255) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Env Invalid[2~255]");
        }
        if (registryRequest.getKeys()==null || registryRequest.getKeys().size()==0) {
            return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "keys Invalid.");
        }
        for (String key: registryRequest.getKeys()) {
            if (key==null || key.trim().length()<4 || key.trim().length()>255) {
                return new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Key Invalid[4~255]");
            }
        }

        Map<String, TreeSet<String>> result = new HashMap<String, TreeSet<String>>();
        for (String key : registryRequest.getKeys()) {
            // key
            OddRpcRegistryData oddRpcRegistryData = new OddRpcRegistryData();
            oddRpcRegistryData.setEnv(registryRequest.getEnv());
            oddRpcRegistryData.setKey(key);

            // values
            TreeSet<String> dataList = new TreeSet<String>();
            OddRpcRegistry fileOddRpcRegistry = getFileRegistryData(oddRpcRegistryData);
            if (fileOddRpcRegistry !=null) {
                dataList.addAll(fileOddRpcRegistry.getDataList());
            }

            // fill
            result.put(key, dataList);
        }

        return new OddRpcAdminRegistryResponse(result);
    }

    @Override
    public DeferredResult<OddRpcAdminRegistryResponse> monitor(OddRpcAdminRegistryRequest registryRequest) {

        // init
        // 创建了一个延迟的结果对象，并指定了超时时间和默认的响应结果。如果在超时时间内异步处理未完成，则会返回默认的响应结果。
        // 异步处理完成后，可以通过设置 DeferredResult 的结果值来通知 Spring MVC 框架返回数据给客户端。
        DeferredResult deferredResult = new DeferredResult(30 * 1000L, new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.SUCCESS_CODE, "Monitor timeout, no key updated."));

        // valid
        if (this.accessToken!=null && this.accessToken.trim().length()>0 && !this.accessToken.equals(registryRequest.getAccessToken())) {
            deferredResult.setResult(new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "AccessToken Invalid"));
            return deferredResult;
        }
        if (registryRequest.getEnv()==null || registryRequest.getEnv().trim().length()<2 || registryRequest.getEnv().trim().length()>255) {
            deferredResult.setResult(new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Env Invalid[2~255]"));
            return deferredResult;
        }
        if (registryRequest.getKeys()==null || registryRequest.getKeys().size()==0) {
            deferredResult.setResult(new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "keys Invalid."));
            return deferredResult;
        }
        for (String key: registryRequest.getKeys()) {
            if (key==null || key.trim().length()<4 || key.trim().length()>255) {
                deferredResult.setResult(new OddRpcAdminRegistryResponse(OddRpcAdminRegistryResponse.FAIL_CODE, "Key Invalid[4~255]"));
                return deferredResult;
            }
        }

        // monitor by client
        for (String key : registryRequest.getKeys()) {
            String fileName = parseRegistryDataFileName(registryRequest.getEnv(), key);

            List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
            if (deferredResultList == null) {
                deferredResultList = new ArrayList<>();
                registryDeferredResultMap.put(fileName, deferredResultList);
            }

            deferredResultList.add(deferredResult);
        }

        return deferredResult;
    }


    // ------------------------ broadcase + file data ------------------------

    private volatile LinkedBlockingQueue<OddRpcRegistryData> registryQueue = new LinkedBlockingQueue<OddRpcRegistryData>();
    private volatile LinkedBlockingQueue<OddRpcRegistryData> removeQueue = new LinkedBlockingQueue<OddRpcRegistryData>();
    // key : fileName value: List<DeferredResult>
    private Map<String, List<DeferredResult>> registryDeferredResultMap = new ConcurrentHashMap<>();

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }


    // ------------------------ file opt ------------------------

    // get
    public OddRpcRegistry getFileRegistryData(OddRpcRegistryData oddRpcRegistryData){

        // fileName
        String fileName = parseRegistryDataFileName(oddRpcRegistryData.getEnv(), oddRpcRegistryData.getKey());

        // read
        Properties prop = PropUtil.loadProp(fileName);
        if (prop != null) {
            OddRpcRegistry fileOddRpcRegistry = new OddRpcRegistry();
            fileOddRpcRegistry.setData(prop.getProperty("data"));
            fileOddRpcRegistry.setStatus(Integer.valueOf(prop.getProperty("status")));
            fileOddRpcRegistry.setDataList(GsonTool.fromJson(fileOddRpcRegistry.getData(), List.class));
            return fileOddRpcRegistry;
        }
        return null;
    }

    private String parseRegistryDataFileName(String env, String key){
        // fileName
        String fileName = registryDataFilePath
                .concat(File.separator).concat(env)
                .concat(File.separator).concat(key)
                .concat(".properties");
        return fileName;
    }
}
