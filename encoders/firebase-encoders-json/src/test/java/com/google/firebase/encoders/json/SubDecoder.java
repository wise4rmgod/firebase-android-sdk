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

import com.google.firebase.encoders.decode.Decoder;
import com.google.firebase.encoders.decode.FieldRef;
import com.google.firebase.encoders.decode.IntFieldRef;
import com.google.firebase.encoders.decode.ObjectDecoderContext;
import com.google.firebase.encoders.decode.TypeCreator;
import com.google.firebase.encoders.decode.TypeTokenContainer;

public class SubDecoder implements Decoder<Sub> {
  @NonNull
  @Override
  public TypeCreator<Sub> decode(
      @NonNull Class<Sub> type,
      @NonNull TypeTokenContainer typeParameters,
      @NonNull ObjectDecoderContext ctx) {
    IntFieldRef hello = ctx.decodeInt("hello");
    FieldRef<String> world = ctx.decode("world", String.class);
    return ctx1 -> new Sub(ctx1.get(hello), ctx1.get(world));
  }
}
