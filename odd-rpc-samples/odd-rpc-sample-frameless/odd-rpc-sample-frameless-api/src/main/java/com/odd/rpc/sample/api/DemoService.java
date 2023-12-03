package com.odd.rpc.sample.api;

import com.odd.rpc.sample.api.dto.UserDTO;

/**
 * Demo API
 */
public interface DemoService {

	public UserDTO sayHi(String name);

}
