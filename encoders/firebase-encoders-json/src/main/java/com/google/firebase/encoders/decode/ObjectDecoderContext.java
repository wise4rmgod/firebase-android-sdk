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

public interface ObjectDecoderContext {

  @NonNull
  <T> FieldRef<T> decode(@NonNull String name, @NonNull TypeToken<T> typeToken);

  @NonNull
  <T> FieldRef<T> decode(@NonNull String name, @NonNull Class<T> type);

  <T> FieldRef<T> decodeInline(@NonNull TypeToken<T> typeToken);

  <T> FieldRef<T> decodeInline(@NonNull Class<T> type);

  @NonNull
  IntFieldRef decodeInt(@NonNull String name);
}
