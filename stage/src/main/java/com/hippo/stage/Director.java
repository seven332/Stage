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
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import android.util.SparseIntArray;
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
  private boolean isFinishing;
  private boolean isDestroyed;

  private final SparseArray<Stage> stageMap = new SparseArray<>();
  private final SparseIntArray activityRequestCodeMap = new SparseIntArray();
  private final SparseIntArray permissionRequestCodeMap = new SparseIntArray();

  /**
   * Hires a {@link Director} for a {@link Activity}.
   *
   * @param savedInstanceState the {@link Bundle} passed in {@link Activity#onCreate(Bundle)}
   */
  public static Director hire(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    return ActivityHostedDirector.getInstance(activity, savedInstanceState);
  }

  /**
   * Directs a {@link ViewGroup} as a {@link Stage}.
   * <p>
   * Use different container view for each {@code Stage}.
   * Set different ID for each container view.
   */
  public Stage direct(@NonNull ViewGroup container) {
    if (isDestroyed) {
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

  /**
   * Look for a child {@link Scene} with the given id.
   */
  @Nullable
  public Scene findSceneById(int sceneId) {
    for(int i = 0, n = stageMap.size(); i < n; i++) {
      Stage stage = stageMap.valueAt(i);
      Scene result = stage.findSceneById(sceneId);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  @Nullable
  abstract Activity getActivity();

  abstract void startActivity(@NonNull Intent intent);

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  abstract void startActivity(@NonNull Intent intent, @Nullable Bundle options);

  void startActivityForResult(int stageId, Intent intent, int requestCode) {
    // TODO check duplicate request code
    activityRequestCodeMap.put(requestCode, stageId);
    startActivityForResult(intent, requestCode);
  }

  abstract void startActivityForResult(Intent intent, int requestCode);

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  void startActivityForResult(int stageId, Intent intent, int requestCode, Bundle options) {
    // TODO check duplicate request code
    activityRequestCodeMap.put(requestCode, stageId);
    startActivityForResult(intent, requestCode, options);
  }

  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  abstract void startActivityForResult(Intent intent, int requestCode, Bundle options);

  void onActivityResult(int requestCode, int resultCode, Intent data) {
    int index = activityRequestCodeMap.indexOfKey(requestCode);
    if (index >= 0) {
      int stageId = activityRequestCodeMap.valueAt(index);
      activityRequestCodeMap.removeAt(index);
      Stage stage = stageMap.get(stageId);
      if (stage != null) {
        stage.onActivityResult(requestCode, resultCode, data);
      }
    }
  }

  void requestPermissions(int stageId, @NonNull String[] permissions, int requestCode) {
    // TODO check duplicate request code
    permissionRequestCodeMap.put(requestCode, stageId);
    requestPermissions(permissions, requestCode);
  }

  abstract void requestPermissions(@NonNull String[] permissions, int requestCode);

  void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    int index = permissionRequestCodeMap.indexOfKey(requestCode);
    if (index >= 0) {
      int stageId = permissionRequestCodeMap.valueAt(index);
      permissionRequestCodeMap.removeAt(index);
      Stage stage = stageMap.get(stageId);
      if (stage != null) {
        stage.onRequestPermissionsResult(requestCode, permissions, grantResults);
      }
    }
  }

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

  void detach() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
      assertFalse(isDestroyed);
    }

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.detach();
    }
  }

  // Called before detach(), just like Fragment
  void destroy() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
      assertTrue(isFinishing);
      assertFalse(isDestroyed);
    }

    isDestroyed = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.destroy();
    }
    stageMap.clear();
  }

  void finish() {
    assertFalse(isFinishing);
    isFinishing = true;
  }

  boolean isFinishing() {
    return isFinishing;
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
