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
 * Created by Hippo on 4/21/2017.
 */

import android.support.annotation.NonNull;
import java.util.List;

/**
 * A {@code Curtain} shows animations when {@link Scene} changing in a {@link Stage}.
 */
public abstract class Curtain {

  /**
   * Completes this {@code Curtain} right now.
   * The {@link OnCompleteListener} passed in {@link #execute(List, List, OnCompleteListener)}
   * before, must be called in the method.
   */
  protected abstract void completeImmediately();

  /**
   * Executes this {@code Curtain} with {@code upper} and {@code lower}.
   * {@code listener} must be called in this method or after the animation done.
   */
  protected abstract void execute(
      @NonNull List<SceneInfo> upper, @NonNull List<SceneInfo> lower,
      @NonNull OnCompleteListener listener);

  /**
   * A listener for being notified when the {@link Curtain} is complete.
   */
  public interface OnCompleteListener {

    /**
     * Called when the {@code Curtain} is complete.
     */
    void OnComplete();
  }
}
