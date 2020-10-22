package io.elves.core.coder;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

import static io.elves.core.ElvesConstants.JSON_CODER;

public class JsonCoder implements Coder {
    public static final Coder INSTANCE = new JsonCoder();
    /**
     * Encode the given object into a byte array with the given charset.
     *
     * @param t
     * @param charset
     * @return
     */
    @Override
    public <T> byte[] encode(T t, Charset charset) {
        return JSON.toJSONBytes(t);
    }

    /**
     * Decode the given byte array into an object of type {@code R} with the default charset.
     *
     * @param bytes   origin input bytes
     * @param target  target object class
     * @param charset
     * @return
     */
    @Override
    public <T> T decode(byte[] bytes, Class<T> target, Charset charset) {
        return JSON.parseObject(bytes, target);
    }

    /**
     * define this coder context name.
     *
     * @return
     */
    @Override
    public String codeName() {
        return JSON_CODER;
    }
}
