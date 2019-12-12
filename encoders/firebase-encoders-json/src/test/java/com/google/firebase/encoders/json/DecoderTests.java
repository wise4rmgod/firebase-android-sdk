// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.encoders.json;

import android.util.JsonReader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.firebase.encoders.decode.Decoder;
import com.google.firebase.encoders.decode.JsonDecoder;
import com.google.firebase.encoders.decode.PrimitiveDecoder;
import com.google.firebase.encoders.decode.TypeToken;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DecoderTests {
  @Test
  public void test() throws IOException {
    Map<Class<?>, Decoder<Object>> decoders = new HashMap<>();
//    decoders.put(Pojo.class, (Decoder<Object>) (Decoder<?>) new PojoDecoder<>());
//    decoders.put(Sub.class, (Decoder<Object>) (Decoder<?>) new SubDecoder());
    Map<Class<?>, PrimitiveDecoder<Object, Object>> primitiveDecoders = new HashMap<>();
    primitiveDecoders.put(
        StringWrapper.class,
        (PrimitiveDecoder<Object, Object>) (PrimitiveDecoder<?, ?>) new StringWrapperDecoder());
    JsonDecoder jsonDecoder = new JsonDecoder(decoders, primitiveDecoders);
    JsonReader reader =
        new JsonReader(
            new StringReader(
                "[{\"tField\": \"hello\", \"hello\": 2, \"world\": \"inline\", \"stringField\": \"world\", \"intField\": 23, \"pojo\": {\"tField\": true, \"stringField\": \"sub\", \"intField\": 3}}, {\"tField\": \"hello2\", \"stringField\": \"world2\", \"intField\": 42}]"));
    List<Pojo<String>> result =
        jsonDecoder.decode(TypeToken.of(new TypeToken.Safe<List<Pojo<String>>>() {}), reader);
  }

  @Test
  public void test2() throws IOException {
    Map<Class<?>, Decoder<Object>> decoders = new HashMap<>();
    decoders.put(Pojo.class, (Decoder<Object>) (Decoder<?>) new PojoDecoder<>());
    Map<Class<?>, PrimitiveDecoder<Object, Object>> primitiveDecoders = new HashMap<>();
    primitiveDecoders.put(
        StringWrapper.class,
        (PrimitiveDecoder<Object, Object>) (PrimitiveDecoder<?, ?>) new StringWrapperDecoder());
    JsonDecoder jsonDecoder = new JsonDecoder(decoders, primitiveDecoders);
    JsonReader reader =
        new JsonReader(
            new StringReader(
                "{\"pojos\": [{\"tField\": \"hello\", \"stringField\": \"world\", \"intField\": 23, \"pojo\": {\"tField\": true, \"stringField\": \"sub\", \"intField\": 3}}]}"));
    Map<String, Set<Pojo<String>>> result =
        jsonDecoder.decode(
            TypeToken.of(new TypeToken.Safe<Map<String, Set<Pojo<String>>>>() {}), reader);
  }

  @Test
  public void test3() throws IOException {
    Map<Class<?>, Decoder<Object>> decoders = new HashMap<>();
    decoders.put(Pojo.class, (Decoder<Object>) (Decoder<?>) new PojoDecoder<>());
    Map<Class<?>, PrimitiveDecoder<Object, Object>> primitiveDecoders = new HashMap<>();
    primitiveDecoders.put(
        StringWrapper.class,
        (PrimitiveDecoder<Object, Object>) (PrimitiveDecoder<?, ?>) new StringWrapperDecoder());
    JsonDecoder jsonDecoder = new JsonDecoder(decoders, primitiveDecoders);
    JsonReader reader =
        new JsonReader(
            new StringReader(
                "[{\"tField\": \"hello\", \"stringField\": \"world\", \"intField\": 23, \"pojo\": {\"tField\": true, \"stringField\": \"sub\", \"intField\": 3}}]"));
    Pojo<String>[] decode =
        jsonDecoder.decode(TypeToken.of(new TypeToken.Safe<Pojo<String>[]>() {}), reader);

    TypeToken.GenericClass<Pojo<String>> unsafe =
        TypeToken.unsafe(Pojo.class, TypeToken.of(String.class));
    TypeToken.unsafeArray(unsafe);

    int[] result =
        jsonDecoder.decode(
            TypeToken.of(new TypeToken.Safe<int[]>() {}),
            new JsonReader(new StringReader("[1,2,3]")));
  }
}
