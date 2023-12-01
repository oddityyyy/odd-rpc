package com.odd.rpc.admin.controller.interceptor;

import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * push cookies to model as cookieMap
 *
 * @author oddity
 * @create 2023-12-02 1:54
 */
public class CookieInterceptor implements AsyncHandlerInterceptor {

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {

        if (modelAndView!=null && request.getCookies()!=null && request.getCookies().length>0) {
            HashMap<String, Cookie> cookieMap = new HashMap<String, Cookie>();
            for (Cookie ck : request.getCookies()) {
                cookieMap.put(ck.getName(), ck);
            }
            modelAndView.addObject("cookieMap", cookieMap);
        }

    }
}
