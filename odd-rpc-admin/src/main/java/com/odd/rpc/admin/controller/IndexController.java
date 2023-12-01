package com.odd.rpc.admin.controller;

import com.odd.rpc.admin.controller.annotation.PermissionLimit;
import com.odd.rpc.admin.controller.interceptor.PermissionInterceptor;
import com.odd.rpc.admin.core.result.ReturnT;
import com.odd.rpc.admin.dao.IOddRpcRegistryDao;
import com.odd.rpc.admin.dao.IOddRpcRegistryDataDao;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * index controller
 * 
 * @author oddity
 * @create 2023-12-02 1:17
 */

@Controller
public class IndexController {

    @Resource
    private IOddRpcRegistryDao oddRpcRegistryDao;
    @Resource
    private IOddRpcRegistryDataDao oddRpcRegistryDataDao;

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {

        int registryNum = oddRpcRegistryDao.pageListCount(0, 1, null, null);
        int registryDataNum = oddRpcRegistryDataDao.count();

        model.addAttribute("registryNum", registryNum);
        model.addAttribute("registryDataNum", registryDataNum);

        return "index";
    }

    @RequestMapping("/toLogin")
    @PermissionLimit(limit=false)
    public String toLogin(Model model, HttpServletRequest request) {
        if (PermissionInterceptor.ifLogin(request)) {
            return "redirect:/";
        }
        return "login";
    }

    @RequestMapping(value="login", method= RequestMethod.POST)
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password, String ifRemember){
        // valid
        if (PermissionInterceptor.ifLogin(request)) {
            return ReturnT.SUCCESS;
        }

        // param
        if (userName==null || userName.trim().length()==0 || password==null || password.trim().length()==0){
            return new ReturnT<String>(500, "请输入账号密码");
        }
        boolean ifRem = (ifRemember!=null && "on".equals(ifRemember))?true:false;

        // do login
        boolean loginRet = PermissionInterceptor.login(response, userName, password, ifRem);
        if (!loginRet) {
            return new ReturnT<String>(500, "账号密码错误");
        }
        return ReturnT.SUCCESS;
    }

    @RequestMapping(value="logout", method=RequestMethod.POST)
    @ResponseBody
    @PermissionLimit(limit=false)
    public ReturnT<String> logout(HttpServletRequest request, HttpServletResponse response){
        if (PermissionInterceptor.ifLogin(request)) {
            PermissionInterceptor.logout(request, response);
        }
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/help")
    public String help() {
        return "help";
    }

    //在请求参数绑定到方法参数之前进行初始化
    //为 `Date` 类型的参数设置统一的日期格式，以便于在 Spring MVC 控制器中统一处理日期格式的转换和绑定操作
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setLenient(false); //必须按照指定的格式严格匹配，否则将会抛出异常
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }
}
