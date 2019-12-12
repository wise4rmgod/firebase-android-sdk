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

package com.google.firebase.encoders.decode;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonDecoder {
  private final Map<Class<?>, Decoder<Object>> decoders;
  private final Map<Class<?>, PrimitiveDecoder<Object, Object>> primitiveDecoders;

  public JsonDecoder(
      Map<Class<?>, Decoder<Object>> decoders,
      Map<Class<?>, PrimitiveDecoder<Object, Object>> primitiveDecoders) {
    this.decoders = decoders;
    this.primitiveDecoders = primitiveDecoders;
  }

  public <T> T decode(TypeToken<T> t, JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      return null;
    }
    if (t instanceof TypeToken.ArrayType) {
      return decodeArray((TypeToken.ArrayType<T>) t, reader);
    }
    Class<T> rawType = getRawType(t);
    TypeTokenContainer typeParameters = getTypeParameters(t);
    if (Collection.class.isAssignableFrom(rawType)) {
      return (T) decodeArray((Class<? extends Collection>) rawType, typeParameters.at(0), reader);
    }
    if (Map.class.isAssignableFrom(rawType)) {
      return (T) decodeMap(typeParameters.at(1), reader);
    }

    if (String.class.isAssignableFrom(rawType)) {
      return (T) reader.nextString();
    }

    if (int.class.isAssignableFrom(rawType)) {
      return (T) (Integer) reader.nextInt();
    }

    if (Boolean.class.isAssignableFrom(rawType)) {
      return (T) (Boolean) reader.nextBoolean();
    }

    Decoder<T> decoder = (Decoder<T>) getDecoder(rawType);
    if (decoder == null) {
      PrimitiveDecoder<Object, Object> primitiveDecoder = primitiveDecoders.get(rawType);
      if (primitiveDecoder == null) {
        throw new IllegalArgumentException("no decoder for " + t);
      } else {
        switch (reader.peek()) {
          case STRING:
            return (T) primitiveDecoder.decode(reader.nextString());
          case BOOLEAN:
            return (T) primitiveDecoder.decode(reader.nextBoolean());
          case NUMBER:
            return (T) primitiveDecoder.decode(reader.nextLong());
          default:
            throw new IllegalArgumentException("Hello");
        }
      }
    }

    return decodeObject(decoder, rawType, typeParameters, reader);
  }

  private <T> T decodeArray(TypeToken.ArrayType<T> arrayType, JsonReader reader)
      throws IOException {
    if (arrayType.getComponentType().isPrimitive()) {
      TypeToken.RegularClass<?> componentType =
          (TypeToken.RegularClass<?>) arrayType.getComponentType();
      if (componentType.getType() == int.class) {
        return (T) decodeIntArray(reader);
      }

      if (componentType.getType() == boolean.class) {
        return (T) decodeBooleanArray(reader);
      }
    }
    reader.beginArray();
    List<Object> result = new ArrayList<>();
    while (reader.peek() != JsonToken.END_ARRAY) {
      result.add(decode(arrayType.getComponentType(), reader));
    }
    reader.endArray();

    T resultArray = (T) Array.newInstance(getRawType(arrayType.getComponentType()), result.size());
    System.arraycopy(result.toArray(), 0, resultArray, 0, result.size());
    return resultArray;
  }

  private int[] decodeIntArray(JsonReader reader) throws IOException {
    IntArrayBuilder builder = new IntArrayBuilder(2);
    reader.beginArray();
    while (reader.peek() != JsonToken.END_ARRAY) {
      builder.add(reader.nextInt());
    }
    reader.endArray();
    return builder.build();
  }

  private boolean[] decodeBooleanArray(JsonReader reader) throws IOException {
    BooleanArrayBuilder builder = new BooleanArrayBuilder(2);
    reader.beginArray();
    while (reader.peek() != JsonToken.END_ARRAY) {
      builder.add(reader.nextBoolean());
    }
    reader.endArray();
    return builder.build();
  }

  private Map<Object, Object> decodeMap(TypeToken<?> valueType, JsonReader reader)
      throws IOException {
    reader.beginObject();
    Map<Object, Object> result = new LinkedHashMap<>();
    while (reader.peek() != JsonToken.END_OBJECT) {
      result.put(reader.nextName(), decode(valueType, reader));
    }
    reader.endObject();
    return result;
  }

  private Collection<?> decodeArray(
      Class<? extends Collection> collectionType, TypeToken<?> elementType, JsonReader reader)
      throws IOException {
    reader.beginArray();
    Collection result = collectionFor(collectionType);
    while (reader.peek() != JsonToken.END_ARRAY) {
      result.add(decode(elementType, reader));
    }
    reader.endArray();
    return result;
  }

  private <T> Class<T> getRawType(TypeToken<T> t) {
    if (t instanceof TypeToken.RegularClass) {
      return ((TypeToken.RegularClass<T>) t).getType();
    }
    if (t instanceof TypeToken.GenericClass) {
      return ((TypeToken.GenericClass<T>) t).getRawType();
    }

    throw new IllegalArgumentException("Unsupported TypeToken type: " + t);
  }

  private TypeTokenContainer getTypeParameters(TypeToken<?> t) {
    if (t instanceof TypeToken.RegularClass) {
      return TypeTokenContainer.EMPTY;
    }
    if (t instanceof TypeToken.GenericClass) {
      return ((TypeToken.GenericClass<?>) t).getTypeParameters();
    }
    throw new IllegalArgumentException("Unsupported TypeToken type: " + t);
  }

  private <T extends Collection> Collection collectionFor(Class<T> type) {
    if (List.class.isAssignableFrom(type)) {
      return new ArrayList<>();
    }
    if (Set.class.isAssignableFrom(type)) {
      return new LinkedHashSet<>();
    }
    throw new IllegalArgumentException(type.getName() + " is not supported");
  }

  private <T> T decodeObject(
      Decoder<T> decoder, Class<T> type, TypeTokenContainer parameters, JsonReader reader)
      throws IOException {
    reader.beginObject();
    JsonDecoderContext ctx = new JsonDecoderContext();
    TypeCreator<T> creator = decoder.decode(type, parameters, ctx);
    Map<FieldRef<?>, Object> objValues = new HashMap<>();
    Map<IntFieldRef, Integer> intValues = new HashMap<>();
    Map<FieldRef<?>, Pair<TypeCreator<Object>, Map<String, Object>>> inlineRefs = new HashMap<>();
    Map<FieldRef<?>, Map<FieldRef<?>, Object>> inlineValues = new HashMap<>();
    Map<FieldRef<?>, Map<IntFieldRef, Integer>> inlineIntValues = new HashMap<>();
    for (FieldRef<?> ref : ctx.inlineFieldRefs) {
      inlineValues.put(ref, new HashMap<>());
      inlineIntValues.put(ref, new HashMap<>());
      Decoder<Object> inlineDecoder = getDecoder(getRawType(ref.typeToken));
      if (inlineDecoder != null) {
        JsonDecoderContext inlineCtx = new JsonDecoderContext();
        TypeCreator<Object> inlineCreator =
            inlineDecoder.decode(
                getRawType((TypeToken<Object>) ref.typeToken),
                getTypeParameters(ref.typeToken),
                inlineCtx);
        Map<String, Object> mp = new HashMap<>();
        mp.putAll(inlineCtx.fieldRefs);
        mp.putAll(inlineCtx.intFieldRefs);
        inlineRefs.put(ref, Pair.create(inlineCreator, mp));
      }
    }
    while (reader.peek() != JsonToken.END_OBJECT) {
      String name = reader.nextName();
      FieldRef<?> fieldRef = ctx.fieldRefs.get(name);
      IntFieldRef intFieldRef = ctx.intFieldRefs.get(name);
      if (fieldRef != null) {
        objValues.put(fieldRef, decode(fieldRef.typeToken, reader));
      } else if (intFieldRef != null) {
        intValues.put(intFieldRef, reader.nextInt());
      } else if (!inlineRefs.isEmpty()) {
        for (Map.Entry<FieldRef<?>, Pair<TypeCreator<Object>, Map<String, Object>>> entry :
            inlineRefs.entrySet()) {
          Object ref = entry.getValue().second.get(name);
          if (ref != null) {
            if (ref instanceof FieldRef) {
              inlineValues
                  .get(entry.getKey())
                  .put((FieldRef<?>) ref, decode(((FieldRef<?>) ref).typeToken, reader));
            } else if (ref instanceof IntFieldRef) {
              inlineIntValues.get(entry.getKey()).put((IntFieldRef) ref, reader.nextInt());
            }
            break;
          }
        }
        // objValues.put()
      } else {
        throw new IllegalArgumentException("Unknown field " + name + " for " + type);
      }
    }
    reader.endObject();
    for (Map.Entry<FieldRef<?>, Pair<TypeCreator<Object>, Map<String, Object>>> entry :
        inlineRefs.entrySet()) {
      objValues.put(
          entry.getKey(),
          entry
              .getValue()
              .first
              .create(
                  new TypeCreationContext() {
                    @Nullable
                    @Override
                    public <U> U get(@NonNull FieldRef<U> ref) {
                      Map<FieldRef<?>, Object> map = inlineValues.get(entry.getKey());
                      if (map == null) return null;
                      return (U) map.get(ref);
                    }

                    @Override
                    public int get(@NonNull IntFieldRef ref) {
                      Map<IntFieldRef, Integer> map = inlineIntValues.get(entry.getKey());
                      if (map == null) return 0;
                      Integer integer = map.get(ref);
                      return integer != null ? integer : 0;
                    }

                    @Override
                    public long get(@NonNull LongFieldRef ref) {
                      return 0;
                    }
                  }));
    }
    return creator.create(
        new TypeCreationContext() {
          @Nullable
          @Override
          public <U> U get(@NonNull FieldRef<U> ref) {
            objValues.get(ref);
            return (U) objValues.get(ref);
          }

          @Override
          public int get(@NonNull IntFieldRef ref) {
            return intValues.get(ref);
          }

          @Override
          public long get(@NonNull LongFieldRef ref) {
            return 0;
          }
        });
  }

  private static final ReflectiveDecoder REFLECTIVE_DECODER = new ReflectiveDecoder();

  private Decoder<Object> getDecoder(Class<?> type) {
    Decoder<Object> objectDecoder = decoders.get(type);
    if (objectDecoder == null && !primitiveDecoders.containsKey(type)) {
      return REFLECTIVE_DECODER;
    }
    return objectDecoder;
  }
}
