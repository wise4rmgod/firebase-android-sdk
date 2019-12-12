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

import com.google.firebase.encoders.annotations.Encodable;
import com.google.firebase.encoders.annotations.UseToDecode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

public class ReflectiveDecoder implements Decoder<Object> {
  @NonNull
  @Override
  public TypeCreator<Object> decode(
      @NonNull Class<Object> type,
      @NonNull TypeTokenContainer typeParameters,
      @NonNull ObjectDecoderContext ctx) {
    Constructor<?> constructor = findConstructor(type);
    constructor.setAccessible(true);
    if (constructor.getParameterTypes().length == 0) {
      return setterBasedDecoder(constructor, type, typeParameters, ctx);
    } else {
      return ctorBasedDecoder(constructor, type, typeParameters, ctx);
    }
  }

  private TypeCreator<Object> ctorBasedDecoder(
      Constructor<?> constructor,
      Class<Object> type,
      TypeTokenContainer typeParameters,
      ObjectDecoderContext ctx) {
    List<ParameterSpec> parameterSpecs = getParameterSpecs(constructor, type, typeParameters);
    Type[] parameterTypes = constructor.getGenericParameterTypes();
    Object[] fieldRefs = new Object[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      ParameterSpec spec = parameterSpecs.get(i);
      TypeToken<?> typeToken;
      if (parameterTypes[i] instanceof TypeVariable) {
        typeToken = getParameterType(parameterTypes[i], type, typeParameters);
      } else {
        typeToken = TypeToken.of(parameterTypes[i]);
      }
      if (isInt(spec.type)) {
        fieldRefs[i] = ctx.decodeInt(spec.name);
      } else if (spec.inline) {
        fieldRefs[i] = ctx.decodeInline(typeToken);
      } else {
        fieldRefs[i] = ctx.decode(spec.name, typeToken);
      }
    }
    return ctx1 -> {
      Object[] args = new Object[fieldRefs.length];
      for (int i = 0; i < fieldRefs.length; i++) {
        Object ref = fieldRefs[i];
        if (ref instanceof FieldRef) {
          args[i] = ctx1.get((FieldRef<?>) ref);
        } else if (ref instanceof IntFieldRef) {
          args[i] = ctx1.get((IntFieldRef) ref);
        } else if (ref instanceof LongFieldRef) {
          args[i] = ctx1.get((LongFieldRef) ref);
        }
      }
      try {
        return constructor.newInstance(args);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("oops", e);
      } catch (InstantiationException e) {
        throw new RuntimeException("oops", e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException("oops", e);
      }
    };
  }

  private boolean isInt(TypeToken<?> type) {
    if (type instanceof TypeToken.RegularClass) {
      return type.isPrimitive() && ((TypeToken.RegularClass<?>) type).getType().equals(int.class);
    }
    return false;
  }

  private TypeToken<?> getParameterType(
      Type type, Class<Object> ownerType, TypeTokenContainer typeParameters) {
    if (type instanceof TypeVariable) {

      for (int i = 0; i < ownerType.getTypeParameters().length; i++) {
        if (ownerType.getTypeParameters()[i].getName().equals(((TypeVariable) type).getName())) {
          return typeParameters.at(i);
        }
      }
    }
    return TypeToken.of(type);
  }

  private List<ParameterSpec> getParameterSpecs(
      Constructor<?> ctor, Class<Object> type, TypeTokenContainer typeParameters) {
    List<ParameterSpec> result = new ArrayList<>();
    Annotation[][] parameterAnnotations = ctor.getParameterAnnotations();
    for (int i = 0; i < parameterAnnotations.length; i++) {
      Annotation[] annotations = parameterAnnotations[i];
      for (Annotation annotation : annotations) {
        if (annotation instanceof Encodable.Field) {
          Encodable.Field field = (Encodable.Field) annotation;

          result.add(
              new ParameterSpec(
                  getParameterType(ctor.getGenericParameterTypes()[i], type, typeParameters),
                  field.name(),
                  field.inline()));
          break;
        }
      }
    }
    return result;
  }

  private TypeCreator<Object> setterBasedDecoder(
      Constructor<?> constructor,
      Class<Object> type,
      TypeTokenContainer typeParameters,
      ObjectDecoderContext ctx) {
    return null;
  }

  private static Constructor<?> findConstructor(Class<?> type) {
    for (Constructor<?> ctor : type.getDeclaredConstructors()) {
      if (ctor.isAnnotationPresent(UseToDecode.class)) {
        return ctor;
      }
    }
    try {
      return type.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("No suitable constructors found for type " + type);
    }
  }

  static class ParameterSpec {
    final TypeToken<?> type;
    final String name;
    final boolean inline;

    ParameterSpec(TypeToken<?> type, String name, boolean inline) {
      this.type = type;
      this.name = name;
      this.inline = inline;
    }
  }
}
