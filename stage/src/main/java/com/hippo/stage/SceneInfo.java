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

import static junit.framework.Assert.assertNotNull;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * A {@code SceneInfo} describes the attaching information of {@link Scene}'s view.
 */
public final class SceneInfo {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  /**
   * The described {@link Scene}.
   */
  @NonNull
  public final Scene scene;
  /**
   * The view of the {@code Scene}.
   */
  @NonNull
  public final View view;
  /**
   * Whether the view is newly attached.
   */
  public final boolean newlyAttached;
  /**
   * Whether the view will be detached.
   */
  public final boolean willBeDetached;

  SceneInfo(@NonNull Scene scene, @NonNull View view, boolean newlyAttached, boolean willBeDetached) {
    if (DEBUG) {
      assertNotNull("View of the scene is null: " + scene, view);
    }

    this.scene = scene;
    this.view = view;
    this.newlyAttached = newlyAttached;
    this.willBeDetached = willBeDetached;
  }
}
