/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.stage.util;

/*
 * Created by Hippo on 4/24/2017.
 */

import com.github.dakusui.combinatoradix.Enumerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomogeniousPermutator<T> extends Enumerator.Base<T> {

  private final Map<Integer, Long> powCache = new HashMap<>();

  public HomogeniousPermutator(List<? extends T> items, int k) {
    this(items, k, pow(items.size(), k));
  }

  public HomogeniousPermutator(List<? extends T> items, int k, long size) {
    super(items, k, size);
  }

  private long pow(int k) {
    Long pow = powCache.get(k);
    if (pow == null) {
      pow = pow(items.size(), k);
      powCache.put(k, pow);
    }
    return pow;
  }

  @Override
  protected List<T> getElement(long index) {
    List<T> list = new ArrayList<>(k);
    int i = k;
    while (--i >= 0) {
      list.add(items.get((int) ((index / pow(i)) % items.size())));
    }
    return list;
  }

  private static long pow(int n, int k) {
    double size = Math.pow(n, k);
    if (size > Long.MAX_VALUE) {
      throw new IllegalArgumentException("Overflow. Too big numbers are used");
    }
    return (long) size;
  }
}
