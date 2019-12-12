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

public final class TypeTokenContainer {
  public static final TypeTokenContainer EMPTY = new TypeTokenContainer(new TypeToken<?>[0]);
  private final TypeToken<?>[] tokens;

  public TypeTokenContainer(@NonNull TypeToken<?>[] tokens) {
    this.tokens = tokens;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public <T> TypeToken<T> at(int index) {
    return (TypeToken<T>) tokens[index];
  }

  @NonNull
  public String asGenericString() {
    StringBuilder sb = new StringBuilder("<");
    for (int i = 0; i < tokens.length; i++) {
      sb.append(tokens[i].toString());
      if (i < tokens.length - 1) {
        sb.append(", ");
      }
    }
    sb.append('>');
    return sb.toString();
  }
}
