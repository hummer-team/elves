package io.elves.core.coder;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ProtostuffCoder implements Coder {

    public static final Coder INSTANCE = new ProtostuffCoder();

    private static Map<Class<?>, Schema<?>> schemaCache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) schemaCache.get(clazz);
        if (Objects.isNull(schema)) {
            schema = RuntimeSchema.getSchema(clazz);
            if (Objects.nonNull(schema)) {
                schemaCache.put(clazz, schema);
            }
        }
        return schema;
    }

    /**
     * Encode the given object into a byte array with the given charset.
     *
     * @param t       the object to encode
     * @param charset the charset
     * @return the encoded byte buffer
     */
    @Override
    public <T> byte[] encode(T t, Charset charset) {
        Class<T> clazz = (Class<T>) t.getClass();
        Schema<T> schema = getSchema(clazz);
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] data;
        try {
            data = ProtobufIOUtil.toByteArray(t, schema, buffer);
        } finally {
            buffer.clear();
        }
        return data;
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
        Schema<T> schema = getSchema(target);
        T obj = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }

    /**
     * define this coder context name.
     */
    @Override
    public String codeName() {
        return "application/protobuff";
    }
}
