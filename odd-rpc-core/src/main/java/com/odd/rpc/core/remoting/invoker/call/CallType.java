package com.odd.rpc.core.remoting.invoker.call;

/**
 * rpc call type
 *
 * @author oddity
 * @create 2023-11-27 0:14
 */
public enum CallType {

    SYNC,

    FUTURE,

    CALLBACK,

    ONEWAY;

    public static CallType match(String name, CallType defaultCallType){
        for (CallType item : CallType.values()){
            if (item.name().equals(name)){
                return item;
            }
        }
        return defaultCallType;
    }
}
