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

import com.google.common.base.Strings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lee
 */
public final class CodecContainer {

    private static final Map<String, Encoder> encoderMap = new ConcurrentHashMap<>();
    private static final Map<String, Decoder> decoderMap = new ConcurrentHashMap<>();

    static {
        // Register default codecs.
        registerEncoder(DefaultCodecs.STRING_ENCODER);
        registerDecoder(DefaultCodecs.STRING_DECODER);
        registerEncoder(DefaultCodecs.JSON_ENCODER);
        registerDecoder(DefaultCodecs.JSON_DECODER);
    }

    private CodecContainer() {

    }

    public static void registerEncoder(Encoder encoder) {
        encoderMap.put(encoder.encodeName(), encoder);
    }

    public static void registerDecoder(Decoder decoder) {
        decoderMap.put(decoder.decodeName(), decoder);
    }

    public static Encoder getEncoder(String encodeName) {
        String name = Strings.isNullOrEmpty(encodeName) ? "text/plain" : encodeName;
        return encoderMap.get(name);
    }

    public static Decoder getDecoder(String decoderName) {
        return decoderMap.get(decoderName);
    }

    public static void clear() {
        encoderMap.clear();
        decoderMap.clear();
    }
}
