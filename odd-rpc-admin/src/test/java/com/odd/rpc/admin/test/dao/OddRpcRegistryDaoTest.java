package com.odd.rpc.admin.test.dao;

import com.odd.rpc.admin.core.model.OddRpcRegistry;
import com.odd.rpc.admin.dao.IOddRpcRegistryDao;
import com.odd.rpc.admin.dao.IOddRpcRegistryDataDao;
import com.odd.rpc.admin.dao.IOddRpcRegistryMessageDao;
import com.odd.rpc.core.util.GsonTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author oddity
 * @create 2023-12-03 2:21
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OddRpcRegistryDaoTest {

    private static Logger logger = LoggerFactory.getLogger(OddRpcRegistryDaoTest.class);

    @Resource
    private IOddRpcRegistryDao oddRpcRegistryDao;
    @Resource
    private IOddRpcRegistryDataDao oddRpcRegistryDataDao;
    @Resource
    private IOddRpcRegistryMessageDao oddRpcRegistryMessageDao;

    @Test
    public void test(){
        List<OddRpcRegistry> registryList = oddRpcRegistryDao.pageList(0, 100, null, null);
        logger.info(GsonTool.toJson(registryList));
    }
}
