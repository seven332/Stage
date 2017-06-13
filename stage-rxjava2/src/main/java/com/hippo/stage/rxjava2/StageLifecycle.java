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

package com.hippo.stage.rxjava2;

/*
 * Created by Hippo on 6/12/2017.
 */

import android.support.annotation.NonNull;
import com.hippo.stage.Scene;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public final class StageLifecycle {

  static final int INIT = 0;
  public static final int CREATE = 1;
  public static final int CREATE_VIEW = 2;
  public static final int ATTACH_VIEW = 3;
  public static final int START = 4;
  public static final int RESUME = 5;
  public static final int PAUSE = 6;
  public static final int STOP = 7;
  public static final int DETACH_VIEW = 8;
  public static final int DESTROY_VIEW = 9;
  public static final int DESTROY = 10;

  /**
   * Creates an Observable to emit integer which represent the step of lifecycle.
   * <p>
   * All missing steps are emitted to newly subscribed observer. For example, if the scene
   * has already created view, {@link #CREATE} and {@link #CREATE_VIEW} will be emitted.
   */
  @NonNull
  public static Observable<Integer> create(@NonNull Scene scene) {
    final StageLifecycleListener listener = new StageLifecycleListener(scene);
    return Observable.create(new ObservableOnSubscribe<Integer>() {
      @Override
      public void subscribe(@NonNull ObservableEmitter<Integer> emitter)
          throws Exception {
        listener.addEmitter(emitter);
      }
    });
  }
}
