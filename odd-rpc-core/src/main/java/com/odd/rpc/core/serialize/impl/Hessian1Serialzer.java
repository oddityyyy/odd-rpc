package com.odd.rpc.core.serialize.impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.odd.rpc.core.serialize.Serializer;
import com.odd.rpc.core.util.OddRpcException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author oddity
 * @create 2023-11-23 18:09
 */
public class Hessian1Serialzer extends Serializer {
    @Override
    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HessianOutput ho = new HessianOutput(os);
        try {
            ho.writeObject(obj);
            ho.flush();
            byte[] result = os.toByteArray();
            return result;
        } catch (IOException e) {
            throw new OddRpcException(e);
        } finally {
            try {
                ho.close();
            } catch (IOException e) {
                throw new OddRpcException(e);
            }
            try {
                os.close();
            } catch (IOException e) {
                throw new OddRpcException(e);
            }
        }
    }

    @Override
    public <T> Object deserialize(byte[] bytes, Class<T> clazz) {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        HessianInput hi = new HessianInput(is);
        try {
            Object result = hi.readObject();
            return result;
        } catch (IOException e) {
            throw new OddRpcException(e);
        } finally {
            try {
                hi.close();
            } catch (Exception e) {
                throw new OddRpcException(e);
            }
            try {
                is.close();
            } catch (IOException e) {
                throw new OddRpcException(e);
            }
        }
    }
}
