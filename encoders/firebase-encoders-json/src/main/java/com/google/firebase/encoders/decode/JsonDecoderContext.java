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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class JsonDecoderContext implements ObjectDecoderContext {
  final Map<String, FieldRef<?>> fieldRefs = new HashMap<>();
  final Set<FieldRef<?>> inlineFieldRefs = new HashSet<>();
  final Map<String, IntFieldRef> intFieldRefs = new HashMap<>();

  JsonDecoderContext() {}

  @NonNull
  @Override
  public <T> FieldRef<T> decode(@NonNull String name, @NonNull TypeToken<T> typeToken) {
    FieldRef<T> field = new FieldRef<>(typeToken);
    fieldRefs.put(name, field);
    return field;
  }

  @NonNull
  @Override
  public <T> FieldRef<T> decode(@NonNull String name, @NonNull Class<T> type) {
    FieldRef<T> field = new FieldRef<>(TypeToken.of(type));
    fieldRefs.put(name, field);
    return field;
  }

  @Override
  public <T> FieldRef<T> decodeInline(@NonNull TypeToken<T> typeToken) {
    FieldRef<T> fieldRef = new FieldRef<>(typeToken);
    inlineFieldRefs.add(fieldRef);
    return fieldRef;
  }

  @Override
  public <T> FieldRef<T> decodeInline(@NonNull Class<T> type) {
    return decodeInline(TypeToken.of(type));
  }

  @NonNull
  @Override
  public IntFieldRef decodeInt(@NonNull String name) {
    IntFieldRef field = new IntFieldRef();
    intFieldRefs.put(name, field);
    return field;
  }
}
