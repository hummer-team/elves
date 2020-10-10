/*
 * Copyright 1999-2020 panli Group Holding Ltd.
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
import java.nio.charset.StandardCharsets;

/**
 * Decodes from a byte array to string.
 *
 * @author lee
 */
public class StringDecoder implements Decoder {

    @Override
    public boolean canDecode(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz);
    }

    @Override
    public <T> T decode(byte[] bytes, Class<?> target) {
        return decode(bytes, target, StandardCharsets.UTF_8);
    }

    @Override
    public <T> T decode(byte[] bytes, Class<?> target, Charset charset) {
        if (bytes == null || bytes.length <= 0) {
            throw new IllegalArgumentException("Bad byte array");
        }
        return (T) new String(bytes, charset);
    }

    /**
     * decode name . ig: application/json or text/plain
     *
     * @return
     */
    @Override
    public String decodeName() {
        return "text/plain";
    }
}
