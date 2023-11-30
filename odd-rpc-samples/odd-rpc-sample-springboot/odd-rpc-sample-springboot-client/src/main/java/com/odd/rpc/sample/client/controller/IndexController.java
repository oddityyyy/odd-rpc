package com.odd.rpc.sample.client.controller;

import com.odd.rpc.core.remoting.invoker.annotaion.OddRpcReference;
import com.odd.rpc.core.remoting.provider.annotation.OddRpcService;
import com.odd.rpc.sample.api.DemoService;
import com.odd.rpc.sample.api.dto.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author oddity
 * @create 2023-11-25 1:16
 */

@Controller
public class IndexController {

    @OddRpcReference
    private DemoService demoService;

    @RequestMapping("")
    @ResponseBody
    public UserDTO http(String name){
        try {
            return demoService.sayHi(name);
        } catch (Exception e) {
            e.printStackTrace();
            return new UserDTO(null, e.getMessage());
        }
    }
}
