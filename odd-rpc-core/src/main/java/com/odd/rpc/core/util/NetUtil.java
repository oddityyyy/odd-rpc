package com.odd.rpc.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * net util
 *
 * @author oddity
 * @create 2023-11-23 22:37
 */
public class NetUtil {
    private static Logger logger = LoggerFactory.getLogger(NetUtil.class);

    /**
     * find avaliable port
     *
     * @param defaultPort
     * @return
     */
    public static int findAvailablePort(int defaultPort) {
        int portTmp = defaultPort;
        while (portTmp < 65535) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp++;
            }
        }
        portTmp = defaultPort--;
        while (portTmp > 0) {
            if (!isPortUsed(portTmp)) {
                return portTmp;
            } else {
                portTmp--;
            }
        }
        throw new OddRpcException("no available port.");
    }

    /**
     * check port used
     *
     * @param port
     * @return
     */
    public static boolean isPortUsed(int port) {
        boolean used = false;
        ServerSocket serverSocket = null;
        try {
            //如果“ServerSocket”创建成功（未引发异常），则表示该端口可用。
            serverSocket = new ServerSocket(port);
            used = false;
        } catch (IOException e) {
            logger.info(">>>>>>>>>>> odd-rpc, port[{}] is in use.", port);
            used = true;
        } finally {
            if (serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.info("");
                }
            }
        }
        return used;
    }
}
