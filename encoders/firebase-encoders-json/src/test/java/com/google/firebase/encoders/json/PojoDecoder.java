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
import com.google.firebase.encoders.decode.Decoder;
import com.google.firebase.encoders.decode.FieldRef;
import com.google.firebase.encoders.decode.IntFieldRef;
import com.google.firebase.encoders.decode.ObjectDecoderContext;
import com.google.firebase.encoders.decode.TypeCreator;
import com.google.firebase.encoders.decode.TypeToken;
import com.google.firebase.encoders.decode.TypeTokenContainer;

public class PojoDecoder<T> implements Decoder<Pojo<T>> {

  @Nullable
  @Override
  public TypeCreator<Pojo<T>> decode(
      @NonNull Class<Pojo<T>> type,
      @NonNull TypeTokenContainer typeParameters,
      @NonNull ObjectDecoderContext ctx) {
    FieldRef<T> tField = ctx.decode("tField", typeParameters.at(0));
    FieldRef<StringWrapper> stringField = ctx.decode("stringField", StringWrapper.class);
    FieldRef<Sub> sub = ctx.decodeInline(Sub.class);
    IntFieldRef intField = ctx.decodeInt("intField");
    FieldRef<Pojo<Boolean>> pojoField =
        ctx.decode("pojo", TypeToken.of(new TypeToken.Safe<Pojo<Boolean>>() {}));

    return (creationCtx) ->
        new Pojo<>(
            creationCtx.get(tField),
            creationCtx.get(stringField),
            creationCtx.get(sub),
            creationCtx.get(intField),
            creationCtx.get(pojoField));
  }
}
