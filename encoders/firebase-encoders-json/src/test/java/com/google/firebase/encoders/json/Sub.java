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

import com.google.firebase.encoders.annotations.Encodable;
import com.google.firebase.encoders.annotations.UseToDecode;

public class Sub {
  private final int hello;
  private final String world;

  @UseToDecode
  public Sub(
      @Encodable.Field(name = "hello") int hello, @Encodable.Field(name = "world") String world) {
    this.hello = hello;
    this.world = world;
  }
}
