package com.odd.rpc.admin.dao;

import com.odd.rpc.admin.core.model.OddRpcRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author oddity
 * @create 2023-12-01 14:54
 */

@Mapper
public interface IOddRpcRegistryDao {

    public List<OddRpcRegistry> pageList(@Param("offset") int offset,
                                         @Param("pagesize") int pagesize,
                                         @Param("env") String env,
                                         @Param("key") String key);

    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("env") String env,
                             @Param("key") String key);

    public OddRpcRegistry load(@Param("env") String env,
                               @Param("key") String key);

    public OddRpcRegistry loadById(@Param("id") int id);

    public int add(@Param("oddRpcRegistry") OddRpcRegistry oddRpcRegistry);

    public int update(@Param("oddRpcRegistry") OddRpcRegistry oddRpcRegistry);

    public int delete(@Param("id") int id);
}
