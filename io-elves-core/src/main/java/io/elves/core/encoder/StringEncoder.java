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

import com.alibaba.fastjson.JSON;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Encode a string to a byte array.
 *
 * @author lee
 */
public class StringEncoder implements Encoder {

    @Override
    public boolean canEncode(Class<?> clazz) {
        return String.class.isAssignableFrom(clazz);
    }


    @Override
    public <R> byte[] encode(R r, Charset charset) {
        return JSON.toJSONBytes(r);
    }

    @Override
    public <R> byte[] encode(R r) {
        return encode(r, StandardCharsets.UTF_8);
    }

    /**
     * encode name . ig: application/json or text/plain
     *
     * @return
     */
    @Override
    public String encodeName() {
        return "text/plain";
    }
}
