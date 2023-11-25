package com.odd.rpc.core.registry.impl;

import com.odd.rpc.core.registry.Register;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * application registry for "local"
 *
 * @author oddity
 * @create 2023-11-26 0:14
 */
public class LocalRegister extends Register {
    @Override
    public void start(Map<String, String> param) {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean registry(Set<String> keys, String value) {
        return false;
    }

    @Override
    public boolean remove(Set<String> keys, String value) {
        return false;
    }

    @Override
    public Map<String, TreeSet<String>> discovery(Set<String> keys) {
        return null;
    }

    @Override
    public TreeSet<String> discovery(String key) {
        return null;
    }
}
