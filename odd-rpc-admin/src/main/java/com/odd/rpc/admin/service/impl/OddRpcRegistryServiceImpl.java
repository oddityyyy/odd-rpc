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
import com.odd.rpc.core.util.OddRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author oddity
 * @create 2023-12-01 14:44
 */
@Service
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

            // 对指定fileName创建一个可供监控的结果列表，在列表中加入这次请求待返回的DeferredResult对象
            deferredResultList.add(deferredResult);
        }

        return deferredResult;
    }

    /**
     * update registry and message
     *
     * @param oddRpcRegistryData
     */
    private void checkRegistryDataAndSendMessage(OddRpcRegistryData oddRpcRegistryData){

        // data json
        List<OddRpcRegistryData> oddRpcRegistryDataList = oddRpcRegistryDataDao.findData(oddRpcRegistryData.getEnv(), oddRpcRegistryData.getKey());
        List<Object> valueList = new ArrayList<>();
        if (oddRpcRegistryDataList != null && oddRpcRegistryDataList.size() > 0){
            for (OddRpcRegistryData dataItem : oddRpcRegistryDataList){
                valueList.add(dataItem.getValue());
            }
        }
        String dataJson = GsonTool.toJson(valueList);

        // update registry and message
        OddRpcRegistry oddRpcRegistry = oddRpcRegistryDao.load(oddRpcRegistryData.getEnv(), oddRpcRegistryData.getKey());
        boolean needMessage = false;
        if (oddRpcRegistry == null){
            oddRpcRegistry = new OddRpcRegistry();
            oddRpcRegistry.setEnv(oddRpcRegistryData.getEnv());
            oddRpcRegistry.setKey(oddRpcRegistryData.getKey());
            oddRpcRegistry.setData(dataJson);
            oddRpcRegistryDao.add(oddRpcRegistry);
            needMessage = true;
        } else {

            // check status, locked and disabled not use
            if (oddRpcRegistry.getStatus() != 0){
                return;
            }

            if (!oddRpcRegistry.getData().equals(dataJson)){
                oddRpcRegistry.setData(dataJson);
                oddRpcRegistryDao.update(oddRpcRegistry);
                needMessage = true;
            }
        }

        if (needMessage){
            // sendRegistryDataUpdatedMessage (registry update)
            sendRegistryDataUpdateMessage(oddRpcRegistry);
        }
    }


    // ------------------------ broadcase + file data ------------------------

    // 用于创建一个可根据需要创建新线程的线程池。这种类型的线程池会根据任务的数量动态地调整线程池的大小，
    // 如果有空闲线程可用则重用，否则创建新的线程。适用于执行大量短期异步任务的场景。
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStopped = false;
    // 同步的、线程安全的 `List` 实例
    private volatile List<Integer> readMessageIds = Collections.synchronizedList(new ArrayList<Integer>());

    private volatile LinkedBlockingQueue<OddRpcRegistryData> registryQueue = new LinkedBlockingQueue<OddRpcRegistryData>();
    private volatile LinkedBlockingQueue<OddRpcRegistryData> removeQueue = new LinkedBlockingQueue<OddRpcRegistryData>();
    // key : fileName value: List<DeferredResult>
    private Map<String, List<DeferredResult>> registryDeferredResultMap = new ConcurrentHashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {

        // valid
        if (registryDataFilePath == null || registryDataFilePath.trim().length() == 0){
            throw new OddRpcException("odd-rpc, registryDataFilePath empty.");
        }

        /**
         * 10 个注册线程，更新registry <-- registry data, 但不写文件
         * registry registry data         (client-num/10 s)
         */
        for (int i = 0; i < 10; i++){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStopped){
                        try {
                            // 从名为 `registryQueue` 的队列中取出一个元素
                            // 如果队列当前为空，则 `take()` 方法会使当前线程进入阻塞状态，
                            // 直到队列中有新的元素被添加进来，或者线程被中断（Interrupted）
                            OddRpcRegistryData oddRpcRegistryData = registryQueue.take();
                            if (oddRpcRegistryData != null){

                                // refresh or add
                                int ret = oddRpcRegistryDataDao.refresh(oddRpcRegistryData);
                                if (ret == 0){
                                    oddRpcRegistryDataDao.add(oddRpcRegistryData);
                                }

                                // valid file status
                                OddRpcRegistry fileOddRpcRegistry = getFileRegistryData(oddRpcRegistryData);
                                if (fileOddRpcRegistry == null){
                                    // go on
                                } else if (fileOddRpcRegistry.getStatus() != 0){
                                    continue; // Status limited
                                } else {
                                    if (fileOddRpcRegistry.getDataList().contains(oddRpcRegistryData.getValue())){
                                        continue; // Repeated limited
                                    }
                                }

                                // checkRegistryDataAndSendMessage
                                checkRegistryDataAndSendMessage(oddRpcRegistryData);
                            }
                        } catch (Exception e) {
                            if (!executorStopped){
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            });
        }

        /**
         * 10 个删除线程，更新registry <-- registry data, 但不删文件
         *
         * remove registry data         (client-num/start-interval s)
         */
        for (int i = 0; i < 10; i++){
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStopped){
                        try {
                            OddRpcRegistryData oddRpcRegistryData = removeQueue.take();
                            if (oddRpcRegistryData != null){

                                // delete
                                oddRpcRegistryDataDao.deleteDataValue(oddRpcRegistryData.getEnv(), oddRpcRegistryData.getKey(), oddRpcRegistryData.getValue());

                                // valid file status
                                OddRpcRegistry fileOddRpcRegistry = getFileRegistryData(oddRpcRegistryData);
                                if (fileOddRpcRegistry == null){
                                    // go on
                                } else if (fileOddRpcRegistry.getStatus() != 0){
                                    continue;
                                } else{
                                    if (!fileOddRpcRegistry.getDataList().contains(oddRpcRegistryData.getValue())){
                                        continue;
                                    }
                                }

                                // checkRegistryDataAndSendMessage
                                checkRegistryDataAndSendMessage(oddRpcRegistryData);
                            }
                        } catch (Exception e) {
                            if (!executorStopped){
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            });
        }

        /**
         * broadcast new one registry-data-file
         *
         * 单线程：一分钟执行一次。通过message来更新并写文件，若有监听，返回监听对象，最后清理message
         *
         * clean old message
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStopped){
                    try {
                        // new message, filter read
                        List<OddRpcRegistryMessage> messageList = oddRpcRegistryMessageDao.findMessage(readMessageIds);
                        if (messageList != null && messageList.size() > 0){
                            for (OddRpcRegistryMessage message : messageList){
                                readMessageIds.add(message.getId());

                                if (message.getType() == 0){// from registry、add、update、delete， need sync from db, only write

                                    OddRpcRegistry oddRpcRegistry = GsonTool.fromJson(message.getData(), OddRpcRegistry.class);

                                    // process data by status
                                    if (oddRpcRegistry.getStatus() == 1){
                                        // locked, not updated
                                    } else if (oddRpcRegistry.getStatus() == 2){
                                        // disabled, write empty
                                        oddRpcRegistry.setData(GsonTool.toJson(new ArrayList<String>()));
                                    } else{
                                        // default, sync from db (already sync before message, only write)
                                    }

                                    // sync file
                                    setFileRegistryData(oddRpcRegistry);
                                }
                            }
                        }

                        // clean old message
                        if ((System.currentTimeMillis() / 1000) % registryBeatTime == 0){
                            oddRpcRegistryMessageDao.cleanMessage(registryBeatTime);
                            readMessageIds.clear();
                        }
                    } catch (Exception e) {
                        if (!executorStopped){
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {
                        if (!executorStopped){
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         *  clean old registry-data     (1/10s) 清理宕机后的RegistryData
         *
         *  sync total registry-data db + file      (1+N/10s) 从RegistryData同步到Registry，从Registry同步到file
         *
         *  clean old registry-data file   从同步file集中删除掉老的不属于本轮同步file集中的文件（产生这些文件的原因是remove线程删除时只删除RegistryData和Registry，不删除file, 这些老的僵尸文件在这个线程中被删除）
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStopped){

                    // align to beatTime
                    try {
                        long sleepSecond = registryBeatTime - (System.currentTimeMillis() / 1000) % registryBeatTime;
                        if (sleepSecond > 0 && sleepSecond < registryBeatTime){
                            TimeUnit.SECONDS.sleep(sleepSecond);
                        }
                    } catch (Exception e) {
                        if (!executorStopped){
                            logger.error(e.getMessage(), e);
                        }
                    }

                    try {
                        // 1. clean old registry-data in db 清理掉长期不用的数据，
                        // 这里是因为生产者有一个线程一直循环注册（心跳连接），当生产者宕机时会执行此处逻辑
                        oddRpcRegistryDataDao.cleanData(registryBeatTime * 3);

                        // sync registry-data, db + file
                        int offset = 0;
                        int pagesize = 1000;
                        List<String> registryDataFileList = new ArrayList<>();

                        List<OddRpcRegistry> registryList = oddRpcRegistryDao.pageList(offset, pagesize, null, null);
                        while(registryList != null && registryList.size() > 0){

                            for (OddRpcRegistry registryItem : registryList){
                                // process data by status
                                if (registryItem.getStatus() == 1){
                                    // locked, not updated
                                } else if (registryItem.getStatus() == 2){
                                    // disabled, write empty
                                    String dataJson = GsonTool.toJson(new ArrayList<String>());
                                    registryItem.setData(dataJson);
                                } else {
                                    // 2. 把RegistryData中的数据同步到Registry
                                    // default, sync from db
                                    List<OddRpcRegistryData> oddRpcRegistryDataList = oddRpcRegistryDataDao.findData(registryItem.getEnv(), registryItem.getKey());
                                    List<String> valueList = new ArrayList<>();
                                    if (oddRpcRegistryDataList != null && oddRpcRegistryDataList.size() > 0){
                                        for (OddRpcRegistryData dataItem : oddRpcRegistryDataList){
                                            valueList.add(dataItem.getValue());
                                        }
                                    }
                                    String dataJson = GsonTool.toJson(valueList);

                                    // check update, sync db
                                    if (!registryItem.getData().equals(dataJson)){
                                        registryItem.setData(dataJson);
                                        oddRpcRegistryDao.update(registryItem);
                                    }
                                }

                                // sync file
                                String registryDataFile = setFileRegistryData(registryItem);

                                // collect registryDataFile
                                registryDataFileList.add(registryDataFile);
                            }

                            offset += 1000;
                            registryList = oddRpcRegistryDao.pageList(offset, pagesize, null, null);
                        }

                        // clean old registry-data file 删除不包括在registryList中的文件
                        cleanFileRegistryData(registryDataFileList);

                    } catch (Exception e) {
                        if (!executorStopped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(registryBeatTime);
                    } catch (Exception e) {
                        if (!executorStopped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        executorStopped = true;
        executorService.shutdown();
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

    // set
    public String setFileRegistryData(OddRpcRegistry oddRpcRegistry){

        // fileName
        String fileName = parseRegistryDataFileName(oddRpcRegistry.getEnv(), oddRpcRegistry.getKey());

        // valid repeat update
        Properties existProp = PropUtil.loadProp(fileName);
        if (existProp != null
                && existProp.getProperty("data").equals(oddRpcRegistry.getData())
                && existProp.getProperty("status").equals(String.valueOf(oddRpcRegistry.getStatus()))){
            return new File(fileName).getPath();
        }

        // write
        Properties prop = new Properties();
        prop.setProperty("data", oddRpcRegistry.getData());
        prop.setProperty("status", String.valueOf(oddRpcRegistry.getStatus()));

        PropUtil.writeProp(prop, fileName);

        logger.info(">>>>>>>>>>> odd-rpc, setFileRegistryData: env={}, key={}, data={}"
                , oddRpcRegistry.getEnv(), oddRpcRegistry.getKey(), oddRpcRegistry.getData());

        // broadcast monitor client
        List<DeferredResult> deferredResultList = registryDeferredResultMap.get(fileName);
        if (deferredResultList != null){
            registryDeferredResultMap.remove(fileName);
            //遍历列表中所有待返回的DeferredResult对象，并设置结果和异步返回
            for (DeferredResult deferredResult : deferredResultList){
                //监控到的结果是 key update
                deferredResult.setResult(new ReturnT<>(ReturnT.SUCCESS_CODE, "Monitor key update."));
            }
        }

        return new File(fileName).getPath();
    }

    // clean
    // 接受一个 registryDataFileList，表示要保留的文件路径列表。
    public void cleanFileRegistryData(List<String> registryDataFileList){
        filterChildPath(new File(registryDataFilePath), registryDataFileList);
    }

    // 过滤指定目录下的文件和子文件夹，删除不在 registryDataFileList 中的文件，同时递归处理子文件夹
    public void filterChildPath(File parentPath, final List<String> registryDataFileList){
        if (!parentPath.exists() || parentPath.list()==null || parentPath.list().length==0) {
            return;
        }
        File[] childFileList = parentPath.listFiles();
        for (File childFile: childFileList) {
            if (childFile.isFile() && !registryDataFileList.contains(childFile.getPath())) {
                childFile.delete();

                logger.info(">>>>>>>>>>> odd-rpc, cleanFileRegistryData, RegistryData Path={}", childFile.getPath());
            }
            if (childFile.isDirectory()) {
                if (parentPath.listFiles()!=null && parentPath.listFiles().length>0) {
                    filterChildPath(childFile, registryDataFileList);
                } else {
                    childFile.delete();
                }

            }
        }

    }
}
