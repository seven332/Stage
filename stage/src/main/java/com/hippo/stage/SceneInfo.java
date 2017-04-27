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

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@code SceneInfo} describes the attaching information of {@link Scene}'s view.
 */
public final class SceneInfo {

  @IntDef({NONE, NEWLY_ATTACHED, WILL_BE_DETACHED})
  @Retention(RetentionPolicy.CLASS)
  public @interface ViewState {}

  /**
   * The view is still attached.
   */
  public static final int NONE = 0;
  /**
   * The view is newly attached.
   */
  public static final int NEWLY_ATTACHED = 1;
  /**
   * The view will be detached.
   */
  public static final int WILL_BE_DETACHED = 2;

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
   * One of {@link #NONE}, {@link #NEWLY_ATTACHED} or {@link #WILL_BE_DETACHED}.
   */
  @ViewState
  public final int viewState;

  final boolean isStarted;

  private SceneInfo(Builder builder) {
    scene = builder.scene;
    view = builder.view;
    viewState = builder.viewState;
    isStarted = builder.isStarted;
  }

  static class Builder {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private Scene scene;
    private View view;
    @ViewState
    private int viewState;
    private boolean isStarted;

    public Builder scene(@NonNull Scene scene) {
      this.scene = scene;
      this.view = scene.getView();

      if (DEBUG) {
        assertNotNull("View of the scene is null: " + scene, scene.getView());
      }

      return this;
    }

    public Builder viewState(@ViewState int viewState) {
      this.viewState = viewState;
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
