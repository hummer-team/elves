package io.elves.core.encoder;

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class JsonEncoder implements Encoder<Object> {
    /**
     * Check whether the encoder supports the given source type.
     *
     * @param clazz type of the class
     * @return {@code true} if supported, {@code false} otherwise
     */
    @Override
    public boolean canEncode(Class<?> clazz) {
        return true;
    }

    /**
     * Encode the given object into a byte array with the given charset.
     *
     * @param o       the object to encode
     * @param charset the charset
     * @return the encoded byte buffer
     * @throws Exception error occurs when encoding the object (e.g. IO fails)
     */
    @Override
    public byte[] encode(Object o, Charset charset) {
        return JSON.toJSONBytes(o);
    }

    /**
     * Encode the given object into a byte array with the default charset.
     *
     * @param o the object to encode
     * @return the encoded byte buffer, witch is already flipped.
     */
    @Override
    public byte[] encode(Object o) {
        return encode(o, StandardCharsets.UTF_8);
    }

    /**
     * encode name . ig: application/json or text/plain
     *
     * @return
     */
    @Override
    public String encodeName() {
        return "application/json";
    }
}