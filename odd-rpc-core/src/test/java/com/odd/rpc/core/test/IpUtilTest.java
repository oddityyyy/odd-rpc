package com.odd.rpc.core.test;

import com.odd.rpc.core.util.IpUtil;

import java.net.UnknownHostException;

/**
 * @author oddity
 * @create 2023-11-23 22:27
 */
public class IpUtilTest {

    public static void main(String[] args) throws UnknownHostException {
        System.out.println(IpUtil.getIp());
        System.out.println(IpUtil.getIpPort(8080));
    }
}
