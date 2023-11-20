package com.odd.rpc.sample.api;

import com.odd.rpc.sample.api.dto.UserDTO;

/**
 * 公共的方法接口
 * Demo API
 * @author oddity
 * @create 2023-11-20 17:41
 */
public interface DemoService {

    public UserDTO sayHi(String name);
}
