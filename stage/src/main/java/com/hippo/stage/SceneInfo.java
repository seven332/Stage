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

  final boolean isStarted;

  private SceneInfo(Builder builder) {
    scene = builder.scene;
    view = builder.view;
    newlyAttached = builder.newlyAttached;
    willBeDetached = builder.willBeDetached;
    isStarted = builder.isStarted;
  }

  static class Builder {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private Scene scene;
    private View view;
    private boolean newlyAttached;
    private boolean willBeDetached;
    private boolean isStarted;

    public Builder scene(@NonNull Scene scene) {
      this.scene = scene;
      this.view = scene.getView();

      if (DEBUG) {
        assertNotNull("View of the scene is null: " + scene, scene.getView());
      }

      return this;
    }

    public Builder newlyAttached(boolean newlyAttached) {
      this.newlyAttached = newlyAttached;
      return this;
    }

    public Builder willBeDetached(boolean willBeDetached) {
      this.willBeDetached = willBeDetached;
      return this;
    }

    public Builder isStarted(boolean isStarted) {
      this.isStarted = isStarted;
      return this;
    }

    public SceneInfo build() {
      return new SceneInfo(this);
    }
  }
}
