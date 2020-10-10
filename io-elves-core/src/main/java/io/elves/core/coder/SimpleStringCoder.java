package io.elves.core.coder;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;

public class SimpleStringCoder implements Coder {
    public static final Coder INSTANCE =new SimpleStringCoder();
    /**
     * Check if it can be encoded
     *
     * @param clazz class
     * @return {@code true} if supported, {@code false} otherwise
     */
    @Override
    public boolean canDecode(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    /**
     * Check if it can be encoded
     *
     * @param clazz class
     * @return {@code true} if supported, {@code false} otherwise
     */
    @Override
    public boolean canEncode(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz);
    }

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
        if (!String.class.isAssignableFrom(target)) {
            throw new IllegalArgumentException("target must string type");
        }
        return (T) new String(bytes, charset);
    }

    /**
     * define this coder context name.
     *
     * @return
     */
    @Override
    public String codeName() {
        return "text/plain";
    }
}
