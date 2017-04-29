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
 * Created by Hippo on 4/22/2017.
 */

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.ViewGroup;
import java.util.ArrayList;

/**
 * A {@code Director} can direct multiple stage.
 */
public abstract class Director {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  private static final String KEY_STAGE_STATES = "Director:stage_states";

  private boolean isStarted;
  private boolean isResumed;
  private boolean isDestroy;

  private final SparseArray<Stage> stageMap = new SparseArray<>();

  /**
   * Hires a {@link Director} for a {@link Activity}.
   *
   * @param savedInstanceState the {@link Bundle} passed in {@link Activity#onCreate(Bundle)}
   */
  public static Director hire(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    return ActivityDirector.getInstance(activity, savedInstanceState);
  }

  /**
   * Directs a {@link ViewGroup} as a {@link Stage}.
   * <p>
   * Use different container view for each {@code Stage}.
   * Set different ID for each container view.
   */
  public Stage direct(@NonNull ViewGroup container) {
    if (isDestroy) {
      throw new IllegalStateException("Can't call direct() on a destroyed Director");
    }

    int id = container.getId();
    Stage stage = stageMap.get(id);
    if (stage == null) {
      stage = new Stage(this);
      stage.setId(id);

      // Restore activity lifecycle
      if (isStarted) {
        stage.start();
      }
      if (isResumed) {
        stage.resume();
      }

      // setContainer() handles view re-attaching, so call it after restoring state
      stage.setContainer(container);

      stageMap.put(id, stage);
    } else {
      if (!stage.hasContainer()) {
        stage.setContainer(container);
      } else if (stage.getContainer() != container) {
        throw new IllegalStateException("The Stage already has a different container. "
            + "If you want more than one Stage in a Activity, "
            + "please use different container view for each Stage, "
            + "and set different ID for each container view.");
      }
    }
    return stage;
  }

  abstract int requireSceneId();

  @Nullable
  abstract Activity getActivity();

  void start() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    isStarted = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.start();
    }
  }

  void resume() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertFalse(isResumed);
    }

    isResumed = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.resume();
    }
  }

  void pause() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertTrue(isResumed);
    }

    isResumed = false;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.pause();
    }
  }

  void stop() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertFalse(isResumed);
    }

    isStarted = false;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.stop();
    }
  }

  // Called before detach(), just like Fragment
  void destroy() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
      assertFalse(isDestroy);
    }

    isDestroy = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.detach();
      stage.destroy();
    }
    stageMap.clear();
  }

  void detach() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    // destroy() is called before detach()
    // Check it to avoid detach stage twice
    if (!isDestroy) {
      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        Stage stage = stageMap.valueAt(i);
        stage.detach();
      }
    }
  }

  boolean isFinishing() {
    return isDestroy;
  }

  void saveInstanceState(Bundle outState) {
    ArrayList<Bundle> stageStates = new ArrayList<>(stageMap.size());
    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      Bundle bundle = new Bundle();
      stage.saveInstanceState(bundle);
      stageStates.add(bundle);
    }
    outState.putParcelableArrayList(KEY_STAGE_STATES, stageStates);
  }

  void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    ArrayList<Bundle> stageStates = savedInstanceState.getParcelableArrayList(KEY_STAGE_STATES);
    if (stageStates != null) {
      for (Bundle stageState : stageStates) {
        Stage stage = new Stage(this);
        stage.restoreInstanceState(stageState);

        // Restore stage lifecycle
        if (isStarted) {
          stage.start();
        }
        if (isResumed) {
          stage.resume();
        }

        int id = stage.getId();
        stageMap.put(id, stage);
      }
    }
  }
}
