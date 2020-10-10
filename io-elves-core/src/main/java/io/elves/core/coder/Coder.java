package io.elves.core.coder;

import java.nio.charset.Charset;

/**
 * this is message encode and decode contract.
 */
public interface Coder {
    /**
     * Check if it can be encoded
     *
     * @param clazz class
     * @return {@code true} if supported, {@code false} otherwise
     */
    default boolean canEncode(Class<?> clazz) {
        return true;
    }


    /**
     * Check if it can be decoded
     *
     * @param clazz class
     * @return {@code true} if supported, {@code false} otherwise
     */
    default boolean canDecode(Class<?> clazz) {
        return true;
    }


    /**
     * Encode the given object into a byte array with the given charset.
     *
     * @param t
     * @param charset
     * @param <R>
     * @return
     */
    <T> byte[] encode(T t, Charset charset);

    /**
     * Decode the given byte array into an object of type {@code R} with the default charset.
     *
     * @param bytes   origin input bytes
     * @param target  target object class
     * @param charset
     * @param <T>
     * @return
     */
    <T> T decode(byte[] bytes, Class<T> target, Charset charset);

    /**
     * define this coder context name.
     *
     * @return
     */
    String codeName();
}
