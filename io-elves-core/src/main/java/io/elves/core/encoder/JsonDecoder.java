package io.elves.core.encoder;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonDecoder implements Decoder<Object> {
    /**
     * Check whether the decoder supports the given target type.
     *
     * @param clazz type of the class
     * @return {@code true} if supported, {@code false} otherwise
     */
    @Override
    public boolean canDecode(Class<?> clazz) {
        return true;
    }

    /**
     * Decode the given byte array into an object of type {@code R} with the default charset.
     *
     * @param bytes raw byte buffer
     * @return the decoded target object
     */
    @Override
    public Object decode(byte[] bytes){
        return decode(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Decode the given byte array into an object of type {@code R} with the given charset.
     *
     * @param bytes   raw byte buffer
     * @param charset the charset
     * @return the decoded target object
     */
    @Override
    public Object decode(byte[] bytes, Charset charset) {
        return JSON.parse(bytes);
    }

    /**
     * decode name . ig: application/json or text/plain
     *
     * @return
     */
    @Override
    public String decodeName() {
        return "application/json";
    }
}
