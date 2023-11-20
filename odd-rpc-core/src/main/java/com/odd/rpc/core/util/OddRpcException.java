package com.odd.rpc.core.util;

/**
 * @author oddity
 * @create 2023-11-23 17:29
 */
public class OddRpcException extends RuntimeException{

    private static final long serialVersionUID = 42L;

    public OddRpcException(String msg){
        super(msg);
    }

    public OddRpcException(String msg, Throwable cause){
        super(msg, cause);
    }

    public OddRpcException(Throwable cause) {
        super(cause);
    }
}
