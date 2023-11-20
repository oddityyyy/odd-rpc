package com.odd.rpc.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * ip tool
 *
 * @author oddity
 * @create 2023-11-23 20:40
 */
public class IpUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);

    private static final String ANYHOST_VALUE = "0.0.0.0";
    private static final String LOCALHOST_VALUE = "127.0.0.1";
    //匹配 IP 地址的正则表达式模式
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    private static volatile InetAddress LOCAL_ADDRESS = null;

    // ---------------------- valid ----------------------

    private static InetAddress toValidAddress(InetAddress address){
        if (address instanceof Inet6Address){
            Inet6Address v6Address = (Inet6Address) address;
            if(isPreferIPV6Address()){
                return normalizeV6Address(v6Address);
            }
        }
        if (isValidV4Address(address)) {
            return address;
        }
        return null;
    }

    /**
     * 该方法检查 JVM 系统属性是否设置为 java.net.preferIPv6Addresses=true。
     * 此系统属性可用于在各种 Java 网络操作中优先使用 IPv6 地址而不是 IPv4 地址
     * @return
     */
    private static boolean isPreferIPV6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Addresses");
    }

    /**
     * valid Inet4Address
     *
     * @param address
     * @return
     */
    private static boolean isValidV4Address(InetAddress address){
        //如果 'address' 为 null 或表示环回地址，则立即返回 'false'。
        // 环回地址是分配给计算机以引用自身的地址（例如，“localhost”）
        if (address == null || address.isLoopbackAddress()){
            return false;
        }
        String name = address.getHostAddress();
        boolean result = (name != null
                && IP_PATTERN.matcher(name).matches()
                && !ANYHOST_VALUE.equals(name)
                && !LOCALHOST_VALUE.equals(name));
        return result;
    }

    /**
     * normalize the ipv6 Address, convert scope name to scope id.
     * e.g.
     * convert
     * fe80:0:0:0:894:aeec:f37d:23e1%en0
     * to
     * fe80:0:0:0:894:aeec:f37d:23e1%5
     * <p>
     * The %5 after ipv6 address is called scope id.
     * see java doc of {@link Inet6Address} for more details.
     *
     * @param address the input address
     * @return the normalized address, with scope id converted to int
     */
    private static InetAddress normalizeV6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                // ignore
                logger.debug("Unknown IPV6 address: ", e);
            }
        }
        return address;
    }

    // ---------------------- find ip ----------------------

    /**
     * 尝试检索计算机的本地 IP 地址
     *
     * 此方法尝试首先通过获取本地主机地址来查找有效的非环回本地 IP 地址，
     * 然后遍历网络接口及其关联地址，直到找到合适的地址，同时考虑指定时间范围内的可访问性。
     *
     * @return
     */
    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;

        // **尝试 LocalHost 地址检索**
        try {
            localAddress = InetAddress.getLocalHost();
            InetAddress addressItem = toValidAddress(localAddress);
            if (addressItem != null) {
                return addressItem;
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        try {
            //**从网络接口检索地址**
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (null == interfaces) {
                return localAddress;
            }
            while (interfaces.hasMoreElements()) {
                try {
                    NetworkInterface network = interfaces.nextElement();
                    // 跳过环回、虚拟或非活动接口
                    if (network.isLoopback() || network.isVirtual() || !network.isUp()) {
                        continue;
                    }
                    Enumeration<InetAddress> addresses = network.getInetAddresses();
                    // 对于每个活动和非环回接口，它检索关联的 IP 地址
                    while (addresses.hasMoreElements()) {
                        try {
                            InetAddress addressItem = toValidAddress(addresses.nextElement());
                            if (addressItem != null) {
                                try {
                                    if(addressItem.isReachable(100)){
                                        return addressItem;
                                    }
                                } catch (IOException e) {
                                    // ignore
                                }
                            }
                        } catch (Throwable e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return localAddress;
    }

    // ---------------------- tool ----------------------

    /**
     * Find first valid IP from local network card
     *
     * @return first valid local IP
     */
    public static InetAddress getLocalAddress() {
        if (LOCAL_ADDRESS != null){
            return LOCAL_ADDRESS;
        }
        InetAddress localAddress = getLocalAddress0();
        LOCAL_ADDRESS = localAddress;
        return localAddress;
    }

    /**
     * get ip address
     *
     * @return
     */
    public static String getIp(){
        return getLocalAddress().getHostAddress();
    }

    /**
     * get ip:port
     *
     * @param port
     * @return String
     */
    public static String getIpPort(int port){
        String ip = getIp();
        return getIpPort(ip, port);
    }

    public static String getIpPort(String ip, int port){
        if (ip == null) {
            return null;
        }
        return ip.concat(":").concat(String.valueOf(port));
    }

    public static Object[] parseIpPort(String address){
        String[] array = address.split(":");

        String host = array[0];
        int port = Integer.parseInt(array[1]);

        return new Object[]{host, port};
    }
}
