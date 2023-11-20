package com.odd.rpc.core.remoting.net.params;

/**
 * @author oddity
 *
 * @create 2023-11-24 18:22
 */
public final class Beat {

    public static final int BEAT_INTERVAL = 30;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static OddRpcRequest BEAT_PING;

    static {
        BEAT_PING = new OddRpcRequest();
        BEAT_PING.setRequestId(BEAT_ID);
    }

}
