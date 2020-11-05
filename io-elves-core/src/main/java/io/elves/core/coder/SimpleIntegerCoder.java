package io.elves.core.coder;

import io.elves.common.exception.CommandException;
import io.elves.core.response.CommandResponse;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static io.elves.core.ElvesConstants.TEXT_PLAIN_INTEGER_CODER;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

public class SimpleIntegerCoder implements Coder {
    public static final Coder INSTANCE = new SimpleIntegerCoder();

    /**
     * Check if it can be encoded
     *
     * @param clazz class
     * @return {@code true} if supported, {@code false} otherwise
     */
    @Override
    public boolean canEncode(Class<?> clazz) {
        return Integer.class.isAssignableFrom(clazz);
    }

    /**
     * Check if it can be decoded
     *
     * @param clazz class
     * @return {@code true} if supported, {@code false} otherwise
     */
    @Override
    public boolean canDecode(Class<?> clazz) {
        return Integer.class.isAssignableFrom(clazz);
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
        if (t instanceof CommandResponse) {
            CommandResponse<Integer> response = (CommandResponse) t;
            return Unpooled.copiedBuffer(String.valueOf(response.getData()), StandardCharsets.US_ASCII)
                    .array();
        }
        if (t instanceof Integer) {
            return Unpooled.copiedBuffer(String.valueOf((Integer) t), StandardCharsets.US_ASCII).array();
        }
        throw new CommandException(INTERNAL_SERVER_ERROR, "not support coder ->" + t.getClass());
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
        return (T) ((Integer) Unpooled.buffer(4).readBytes(bytes).getInt(4));
    }

    /**
     * define this coder context name.
     *
     * @return
     */
    @Override
    public String codeName() {
        return TEXT_PLAIN_INTEGER_CODER;
    }
}
