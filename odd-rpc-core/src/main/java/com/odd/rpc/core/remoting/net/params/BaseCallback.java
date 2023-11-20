package com.odd.rpc.core.remoting.net.params;

/**
 * 回调抽象基类
 *
 * @author oddity
 * @create 2023-11-23 15:46
 */
public abstract class BaseCallback {

    //定义一个任务以运行
    public abstract void run() throws Exception;
}
