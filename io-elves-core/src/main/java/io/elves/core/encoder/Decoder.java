/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.elves.core.encoder;

import java.nio.charset.Charset;

/**
 * The decoder decodes bytes into an object of type {@code <R>}.
 *
 * @param <R> target type
 * @author lee
 */
public interface Decoder<R> {

    /**
     * Check whether the decoder supports the given target type.
     *
     * @param clazz type of the class
     * @return {@code true} if supported, {@code false} otherwise
     */
    boolean canDecode(Class<?> clazz);

    /**
     * Decode the given byte array into an object of type {@code R} with the default charset.
     *
     * @param bytes raw byte buffer
     * @return the decoded target object
     */
    R decode(byte[] bytes);

    /**
     * Decode the given byte array into an object of type {@code R} with the given charset.
     *
     * @param bytes   raw byte buffer
     * @param charset the charset
     * @return the decoded target object
     */
    R decode(byte[] bytes, Charset charset);

    /**
     * decode name . ig: application/json or text/plain
     *
     * @return
     */
    String decodeName();
}
