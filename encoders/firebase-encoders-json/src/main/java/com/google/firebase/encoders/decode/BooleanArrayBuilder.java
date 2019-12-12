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

public final class BooleanArrayBuilder {
  private boolean[] array;
  private int size;
  private final int initialCapacity;
  private int growthFactor;

  public BooleanArrayBuilder(int initialCapacity) {
    array = new boolean[initialCapacity];
    this.initialCapacity = initialCapacity;
    this.growthFactor = 1;
  }

  public void add(boolean value) {
    maybeReallocate();
    array[size++] = value;
  }

  private void maybeReallocate() {
    if (size == array.length) {
      boolean[] current = array;
      array = new boolean[initialCapacity * (int) Math.pow(2, growthFactor++)];
      System.arraycopy(current, 0, array, 0, current.length);
    }
  }

  public boolean[] build() {
    return array;
  }
}
