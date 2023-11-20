package com.odd.rpc.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author oddity
 * @create 2023-11-24 16:07
 */
public class ThrowableUtil {

    /**
     * parse error to string
     *
     * @param e
     * @return
     */
    public static String toString(Throwable e){
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String errorMsg = stringWriter.toString();
        return errorMsg;
    }
}
