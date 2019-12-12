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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.encoders.annotations.Encodable;
import com.google.firebase.encoders.annotations.UseToDecode;

public class Pojo<T> {
  private final T tField;
  private final StringWrapper stringField;
  private final Sub sub;
  private final int intField;
  private final Pojo<Boolean> pojo;

  @Nullable
  public T getTField() {
    return tField;
  }

  @UseToDecode
  public Pojo(
      @Encodable.Field(name = "tField") T tField,
      @Encodable.Field(name = "stringField") StringWrapper stringField,
      @Encodable.Field(inline = true) Sub sub,
      @Encodable.Field(name = "intField") int intField,
      @Encodable.Field(name = "pojo") Pojo<Boolean> pojo) {
    this.tField = tField;
    this.stringField = stringField;
    this.sub = sub;
    this.intField = intField;
    this.pojo = pojo;
  }

  @NonNull
  public StringWrapper getStringField() {
    return stringField;
  }

  public int getIntField() {
    return intField;
  }
}
