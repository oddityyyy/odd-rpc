package com.odd.rpc.admin.controller;

import com.odd.rpc.admin.core.model.OddRpcRegistry;
import com.odd.rpc.admin.core.result.ReturnT;
import com.odd.rpc.admin.service.IOddRpcRegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author oddity
 * @create 2023-12-01 21:32
 */

@Controller
@RequestMapping("/registry")
public class RegistryController {

    @Resource
    private IOddRpcRegistryService oddRpcRegistryService;

    @RequestMapping("")
    public String index(Model model){
        return "registry/registry.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String env,
                                        String key){
        return oddRpcRegistryService.pageList(start, length, env, key);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public ReturnT<String> delete(int id){
        return oddRpcRegistryService.delete(id);
    }

    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(OddRpcRegistry oddRpcRegistry){
        return oddRpcRegistryService.update(oddRpcRegistry);
    }

    @RequestMapping("/add")
    @ResponseBody
    public ReturnT<String> add(OddRpcRegistry oddRpcRegistry){
        return oddRpcRegistryService.add(oddRpcRegistry);
    }
}
