package com.odd.rpc.core.test;

import com.odd.rpc.core.serialize.Serializer;
import com.odd.rpc.core.serialize.impl.HessianSerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerTest {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Serializer serializer = HessianSerializer.class.newInstance();
        System.out.println(serializer);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put("aaa", "111");
            map.put("bbb", "222");
            System.out.println(serializer.deserialize(serializer.serialize("ddddddd"), String.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
