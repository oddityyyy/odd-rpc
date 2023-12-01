package com.odd.rpc.admin.controller.interceptor;

import com.odd.rpc.admin.controller.annotation.PermissionLimit;
import com.odd.rpc.admin.core.util.CookieUtil;
import com.odd.rpc.core.util.OddRpcException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigInteger;

/**
 * 权限拦截, 简易版
 *
 * @author oddity
 * @create 2023-12-02 1:01
 */

@Component
public class PermissionInterceptor implements AsyncHandlerInterceptor, InitializingBean {

    // ---------------------- init ----------------------
    @Value("${odd.rpc.registry.login.username}")
    private String username;
    @Value("${odd.rpc.registry.login.password}")
    private String password;

    @Override
    public void afterPropertiesSet() throws Exception {
        // valid
        if (username==null || username.trim().length()==0 || password==null || password.trim().length()==0) {
            throw new OddRpcException("权限账号密码不可为空");
        }

        // login token
        String tokenTmp = DigestUtils.md5DigestAsHex(String.valueOf(username + "_" + password).getBytes());		//.getBytes("UTF-8")
        tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

        LOGIN_IDENTITY_TOKEN = tokenTmp;
    }

    // ---------------------- tool ----------------------

    public static final String LOGIN_IDENTITY_KEY = "ODD_MQ_LOGIN_IDENTITY";
    private static String LOGIN_IDENTITY_TOKEN;

    public static String getLoginIdentityToken() {
        return LOGIN_IDENTITY_TOKEN;
    }

    public static boolean login(HttpServletResponse response, String username, String password, boolean ifRemember){

        // login token
        String tokenTmp = DigestUtils.md5DigestAsHex(String.valueOf(username + "_" + password).getBytes());
        tokenTmp = new BigInteger(1, tokenTmp.getBytes()).toString(16);

        if (!getLoginIdentityToken().equals(tokenTmp)){
            return false;
        }

        // do login
        CookieUtil.set(response, LOGIN_IDENTITY_KEY, getLoginIdentityToken(), ifRemember);
        return true;
    }

    public static void logout(HttpServletRequest request, HttpServletResponse response){
        CookieUtil.remove(request, response, LOGIN_IDENTITY_KEY);
    }

    public static boolean ifLogin(HttpServletRequest request){
        String indentityInfo = CookieUtil.getValue(request, LOGIN_IDENTITY_KEY);
        if (indentityInfo==null || !getLoginIdentityToken().equals(indentityInfo.trim())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //首先判断处理器对象是否是 `HandlerMethod` 类型的实例，如果不是，
        // 则表示这个请求不需要进一步拦截处理，直接放行。
        if (!(handler instanceof HandlerMethod)) {
            return true;	// proceed with the next interceptor
        }

        if (!ifLogin(request)) {
            HandlerMethod method = (HandlerMethod)handler;
            PermissionLimit permission = method.getMethodAnnotation(PermissionLimit.class);
            if (permission == null || permission.limit()) {
                response.sendRedirect(request.getContextPath() + "/toLogin");
                //request.getRequestDispatcher("/toLogin").forward(request, response);
                return false;
            }
        }

        return true;	// proceed with the next interceptor
    }
}
