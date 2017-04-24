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

package com.hippo.stage;

/*
 * Created by Hippo on 4/20/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;

class SceneStack implements Iterable<Scene> {

  private static final String KEY_ENTRIES = "SceneStack:entries";

  static final int INVALID_INDEX = -1;

  private final ArrayDeque<Scene> stack = new ArrayDeque<>();
  private final Callback callback;

  SceneStack(@NonNull Callback callback) {
    this.callback = callback;
  }

  boolean isEmpty() {
    return stack.isEmpty();
  }

  @Nullable
  Scene peek() {
    return stack.peekFirst();
  }

  @Nullable
  Scene tail() {
    return stack.peekLast();
  }

  void push(@NonNull Scene scene) {
    stack.push(scene);
    callback.onPush(scene);
  }

  @Nullable
  Scene pop() {
    Scene scene = stack.poll();
    if (scene != null) {
      callback.onPop(scene);
    }
    return scene;
  }

  // Index: from top to root
  // Returns INVALID_INDEX if can't find it
  int pop(@NonNull Scene scene) {
    Iterator<Scene> iterator = stack.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      Scene current = iterator.next();
      if (current == scene) {
        // Catch it!
        iterator.remove();
        callback.onPop(scene);
        return index;
      }
      ++index;
    }
    // Can't find the scene
    return INVALID_INDEX;
  }

  @Override
  public Iterator<Scene> iterator() {
    return stack.iterator();
  }

  void saveInstanceState(@NonNull Bundle outState) {
    ArrayList<Bundle> bundles = new ArrayList<>(stack.size());
    for (Scene scene : stack) {
      bundles.add(scene.saveInstanceState());
    }
    outState.putParcelableArrayList(KEY_ENTRIES, bundles);
  }

  void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    ArrayList<Bundle> bundles = savedInstanceState.getParcelableArrayList(KEY_ENTRIES);
    if (bundles != null) {
      int index = bundles.size();
      while (--index >= 0) {
        Bundle bundle = bundles.get(index);
        Scene scene = Scene.newInstance(bundle);
        push(scene);
      }
    }
  }

  interface Callback {

    void onPush(@NonNull Scene scene);

    void onPop(@NonNull Scene scene);
  }
}
