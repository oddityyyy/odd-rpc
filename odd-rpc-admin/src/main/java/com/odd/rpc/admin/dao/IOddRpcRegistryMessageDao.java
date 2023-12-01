package com.odd.rpc.admin.dao;

import com.odd.rpc.admin.core.model.OddRpcRegistryMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author oddity
 * @create 2023-12-01 14:55
 */

@Mapper
public interface IOddRpcRegistryMessageDao {

    public int add(@Param("oddRpcRegistryMessage") OddRpcRegistryMessage oddRpcRegistryMessage);

    // 过滤查询
    public List<OddRpcRegistryMessage> findMessage(@Param("excludeIds") List<Integer> excludeIds);

    // clean过期数据（超过延时）
    public int cleanMessage(@Param("messageTimeout") int messageTimeout);
}
