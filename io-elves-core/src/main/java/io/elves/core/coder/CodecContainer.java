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
package io.elves.core.coder;

import com.google.common.base.Strings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lee
 */
public final class CodecContainer {

    private static final Map<String, Coder> CODER_MAP = new ConcurrentHashMap<>();

    static {
        registerCoder(ProtobufCoder.INSTANCE);
        registerCoder(JsonCoder.INSTANCE);
        registerCoder(SimpleStringCoder.INSTANCE);
    }

    private CodecContainer() {

    }

    public static void registerCoder(Coder coder) {
        CODER_MAP.put(coder.codeName(), coder);
    }

    public static Coder getCoder(String coderName) {
        String name = Strings.isNullOrEmpty(coderName) ? "text/plain" : coderName;
        return CODER_MAP.get(name);
    }

    public static void clear() {

        CODER_MAP.clear();
    }
}
