package com.odd.rpc.core.remoting.net.params;

import java.io.Serializable;

/**
 * @author oddity
 * @create 2023-11-24 15:25
 */
public class OddRpcResponse implements Serializable {

    private static final long serialVersionUID = 42L;

    private String requestId;
    private String errorMsg;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "OddRpcResponse{" +
                "requestId='" + requestId + '\'' +
                ", errorMsg='" + errorMsg + '\'' +
                ", result=" + result +
                '}';
    }
}
