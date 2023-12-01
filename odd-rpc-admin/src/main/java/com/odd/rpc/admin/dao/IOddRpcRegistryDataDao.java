package com.odd.rpc.admin.dao;

import com.odd.rpc.admin.core.model.OddRpcRegistryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author oddity
 * @create 2023-12-01 14:55
 */

@Mapper
public interface IOddRpcRegistryDataDao {

    public int refresh(@Param("oddRpcRegistryData") OddRpcRegistryData oddRpcRegistryData);

    public int add(@Param("oddRpcRegistryData") OddRpcRegistryData oddRpcRegistryData);


    public List<OddRpcRegistryData> findData(@Param("env") String env,
                                             @Param("key") String key);

    public int cleanData(@Param("timeout") int timeout);

    public int deleteData(@Param("env") String env,
                          @Param("key") String key);

    public int deleteDataValue(@Param("env") String env,
                               @Param("key") String key,
                               @Param("value") String value);

    public int count();
}
