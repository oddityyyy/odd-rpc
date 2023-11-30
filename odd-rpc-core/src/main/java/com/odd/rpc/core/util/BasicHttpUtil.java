package com.odd.rpc.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author oddity
 * @create 2023-11-30 16:21
 */
public class BasicHttpUtil {

    private static Logger logger = LoggerFactory.getLogger(BasicHttpUtil.class);

    /**
     * post
     *
     * @param url
     * @param requestBody
     * @param timeout
     * @return
     */
    public static String postBody(String url, String requestBody, int timeout){

        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try {
            //connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // do connection
            connection.connect();

            // write requestBody
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.write(requestBody.getBytes("UTF-8"));
            dataOutputStream.flush();
            dataOutputStream.close();

            // valid Status Code
            int statusCode = connection.getResponseCode();
            if (statusCode != 200){
                throw new RuntimeException("http request StatusCode("+ statusCode +") invalid. for url : " + url);
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null){
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null){
                    bufferedReader.close();
                }
                if (connection != null){
                    connection.disconnect();
                }
            } catch (IOException e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
        return null;
    }

    /**
     * get
     *
     * @param url
     * @param timeout
     * @return
     */
    public static String get(String url, int timeout){
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;

        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // connection setting
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(3 * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            // do connection
            connection.connect();

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                throw new RuntimeException("Http Request StatusCode("+ statusCode +") Invalid.");
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            return result.toString();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
        return null;
    }
}
