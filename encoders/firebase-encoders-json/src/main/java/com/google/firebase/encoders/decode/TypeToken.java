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

import androidx.annotation.NonNull;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class TypeToken<T> {
  public abstract static class Safe<T> {}

  public abstract boolean isPrimitive();

  private TypeToken() {}

  @NonNull
  public static <T> RegularClass<T> of(@NonNull Class<T> type) {
    return new RegularClass<>(type);
  }

  @NonNull
  public static <T> GenericClass<T> unsafe(@NonNull Class<?> type, TypeToken<?>... typeParameters) {
    return new GenericClass<>((Class<T>) type, new TypeTokenContainer(typeParameters));
  }

  public static class RegularClass<T> extends TypeToken<T> {
    private final Class<T> type;

    private RegularClass(@NonNull Class<T> type) {
      this.type = type;
    }

    public Class<T> getType() {
      return type;
    }

    @NonNull
    @Override
    public String toString() {
      return type.getCanonicalName();
    }

    @Override
    public boolean isPrimitive() {
      return type.isPrimitive();
    }
  }

  public static class ArrayType<T> extends TypeToken<T> {
    private final TypeToken<?> componentType;

    public ArrayType(TypeToken<?> componentType) {
      this.componentType = componentType;
    }

    public TypeToken<?> getComponentType() {
      return componentType;
    }

    @NonNull
    @Override
    public String toString() {
      return componentType.toString() + "[]";
    }

    @Override
    public boolean isPrimitive() {
      return false;
    }
  }

  public static class GenericClass<T> extends TypeToken<T> {
    private final Class<T> rawType;
    private final TypeTokenContainer typeParameters;

    private GenericClass(@NonNull Class<T> rawType, @NonNull TypeTokenContainer typeParameters) {
      this.rawType = rawType;
      this.typeParameters = typeParameters;
    }

    @NonNull
    public TypeTokenContainer getTypeParameters() {
      return typeParameters;
    }

    public Class<T> getRawType() {
      return rawType;
    }

    @NonNull
    @Override
    public String toString() {
      return rawType.getName() + typeParameters.asGenericString();
    }

    @Override
    public boolean isPrimitive() {
      return false;
    }
  }

  public static void main(@NonNull String[] args) {
    TypeToken<List<Map<? extends String, ? extends Double>>> of =
        TypeToken.of(new Safe<List<Map<? extends String, ? extends Double>>>() {});
    GenericClass<?> unsafe = TypeToken.unsafe(String.class, TypeToken.of(int.class));

    ArrayType<Optional<String>[]> arrayType1 =
        TypeToken.unsafeArray(TypeToken.of(new Safe<Optional<String>>() {}));

    TypeToken<int[]> of1 = TypeToken.of(new Safe<int[]>() {});
  }

  @NonNull
  public static <T> TypeToken<T> of(@NonNull Safe<T> token) {
    ParameterizedType genericSuperclass =
        (ParameterizedType) token.getClass().getGenericSuperclass();

    Type type = genericSuperclass.getActualTypeArguments()[0];

    return (TypeToken<T>) of(type);
  }

  @NonNull
  public static <T> TypeToken.ArrayType<T[]> unsafeArray(TypeToken<T> typeToken) {
    return (TypeToken.ArrayType<T[]>) (TypeToken.ArrayType<?>) new ArrayType<>(typeToken);
  }

  @NonNull
  public static <T> TypeToken.ArrayType<T[]> unsafeArray(Class<T> type) {
    return (TypeToken.ArrayType<T[]>) (TypeToken.ArrayType<?>) new ArrayType<>(TypeToken.of(type));
  }

  @NonNull
  public static TypeToken<?> of(@NonNull Type type) {
    if (type instanceof Class) {
      Class<?> cls = (Class<?>) type;
      if (cls.isArray()) {
        return unsafeArray(cls.getComponentType());
      }
      return TypeToken.of((Class<?>) type);
    }

    if (type instanceof ParameterizedType) {
      ParameterizedType pType = (ParameterizedType) type;
      Type rawType = pType.getRawType();
      return TypeToken.unsafe(
          (Class<?>) rawType,
          Arrays.stream(pType.getActualTypeArguments())
              .map(TypeToken::of)
              .toArray(TypeToken[]::new));
    }
    if (type instanceof WildcardType) {
      WildcardType wType = (WildcardType) type;
      if (wType.getLowerBounds().length > 0) {
        throw new IllegalArgumentException("'? super T' are not supported.");
      }
      return of(wType.getUpperBounds()[0]);
    }
    if (type instanceof GenericArrayType) {
      return unsafeArray(of(((GenericArrayType) type).getGenericComponentType()));
    }
    throw new IllegalArgumentException(type.getClass() + " is not supported.");
  }
}
