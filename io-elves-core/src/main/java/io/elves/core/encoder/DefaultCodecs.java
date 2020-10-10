/*
 * Copyright 2020 panli Group Holding Ltd.
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

/**
 * Caches default encoders and decoders.
 *
 * @author lee
 */
final class DefaultCodecs {

    public static final Encoder<String> STRING_ENCODER = new StringEncoder();
    public static final Decoder<String> STRING_DECODER = new StringDecoder();
    public static final Encoder<Object> JSON_ENCODER = new JsonEncoder();
    public static final Decoder<Object> JSON_DECODER = new JsonDecoder();

    private DefaultCodecs() {
    }
}
