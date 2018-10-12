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

import static junit.framework.Assert.assertEquals;
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
import java.util.Iterator;
import java.util.List;

/**
 * A {@code Director} can direct multiple stage.
 */
public abstract class Director implements Iterable<Stage> {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  private static final String KEY_STAGE_STATES = "Director:stage_states";

  private boolean isStarted;
  private boolean isResumed;
  private boolean isFinishing;
  private boolean isDestroyed;

  private final SparseArray<Stage> stageMap = new SparseArray<>();
  private final SparseIntArray activityRequestCodeMap = new SparseIntArray();
  private final SparseIntArray permissionRequestCodeMap = new SparseIntArray();

  private CurtainSuppler curtainSuppler;

  private BackHandler<Director> backHandler;

  private Stage focusedStage;

  private boolean saveEnabled = true;

  // Hide Director constructor
  Director() {}

  /**
   * Hires a {@link Director} for a {@link Activity}.
   *
   * @param savedInstanceState the {@link Bundle} passed in {@link Activity#onCreate(Bundle)}
   */
  @NonNull
  public static Director hire(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    return ActivityHostedDirector.getInstance(activity, savedInstanceState);
  }

  /**
   * Returns {@code true} if this {@code Director} contains a {@link Stage} with the id.
   */
  public boolean contains(int id) {
    return stageMap.indexOfKey(id) >= 0;
  }

  /**
   * Returns a {@link Stage} with the id.
   * This method can't create any Stage.
   */
  @Nullable
  public Stage get(int id) {
    return stageMap.get(id);
  }

  /**
   * Directs a {@link ViewGroup} as a {@link Stage}.
   * Same as {@code direct(container, container.getId())}.
   * <p>
   * Use the id of the container as stage id.
   */
  @NonNull
  public Stage direct(@NonNull ViewGroup container) {
    return direct(container, container.getId());
  }

  /**
   * Directs a {@link ViewGroup} as a {@link Stage}.
   * <p>
   * Use different id for each {@code Stage}.
   * One container can be used for multiple Stage.
   * <p>
   * If the id has already been used, return the old Stage.
   * <p>
   * It's better if the {@code container} is an instance of {@link StageLayout}.
   */
  @NonNull
  public Stage direct(@NonNull ViewGroup container, int id) {
    return direct(container, id, null);
  }

  /**
   * Directs a {@link ViewGroup} as a {@link Stage}.
   * <p>
   * {@code savedState} should be from {@link Stage#saveInstanceState(Bundle)}.
   * The old id in {@code savedState} is used. If the Director doesn't contain
   * a {@code Stage} with the id, {@code savedState} will be used to
   * restore the {@code Stage}.
   * <p>
   * If {@code savedState} doesn't contain an id, return {@code null}.
   */
  @Nullable
  public Stage direct(@NonNull ViewGroup container, @NonNull Bundle savedState) {
    if (savedState.containsKey(Stage.KEY_ID)) {
      return direct(container, savedState.getInt(Stage.KEY_ID), savedState);
    } else {
      return null;
    }
  }

  /**
   * Directs a {@link Stage}.
   * <p>
   * If the id hasn't been used, a headless Stage will be created.
   */
  @NonNull
  public Stage direct(int id) {
    return direct(null, id, null);
  }

  /**
   * Directs a {@link Stage}.
   * <p>
   * The old id in {@code savedState} is used.
   * If the id hasn't been used, a headless Stage will be created.
   * <p>
   * If {@code savedState} doesn't contain an id, return {@code null}.
   */
  @Nullable
  public Stage direct(@NonNull Bundle savedState) {
    if (savedState.containsKey(Stage.KEY_ID)) {
      return direct(null, savedState.getInt(Stage.KEY_ID), savedState);
    } else {
      return null;
    }
  }

  @NonNull
  private Stage direct(@Nullable ViewGroup container, int id, @Nullable Bundle savedState) {
    if (isDestroyed) {
      throw new IllegalStateException("Can't call direct() on a destroyed Director");
    }

    Stage stage = stageMap.get(id);
    if (stage == null) {
      stage = new Stage(this);
      if (savedState != null) {
        stage.restoreInstanceState(savedState);
      }
      stage.setId(id);

      // Restore activity lifecycle
      if (isStarted) {
        stage.start();
      }
      if (isResumed) {
        stage.resume();
      }

      if (container != null) {
        // setContainer() handles view re-attaching, so call it after restoring state
        stage.setContainer(container);
      }

      stageMap.put(id, stage);
    } else if (container != null) {
      if (!stage.hasContainer()) {
        stage.setContainer(container);
      } else if (stage.getContainer() != container) {
        throw new IllegalStateException("The Stage already has a different container. "
            + "If you want more than one Stage, "
            + "please use different id for each Stage.");
      }
    }
    return stage;
  }

  void close(Stage stage) {
    if (DEBUG) {
      assertEquals(stage, stageMap.get(stage.getId()));
    }

    if (isResumed) {
      stage.pause();
    }
    if (isStarted) {
      stage.stop();
    }
    stage.detach(false);
    stage.destroy();

    stageMap.remove(stage.getId());
  }

  /**
   * Returns an unmodified Stage iterator which is a mysterious order.
   */
  @NonNull
  @Override
  public Iterator<Stage> iterator() {
    return new Iterator<Stage>() {

      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < stageMap.size();
      }

      @Override
      public Stage next() {
        return stageMap.valueAt(index++);
      }
    };
  }

  /**
   * Controls whether the saving of this director's state is
   * enabled. The state includes stages of the director and
   * scene stack of each stage. Setting the flag to {@code null}
   * is similar to {@code Director.hire(activity, null)}.
   */
  public void setSaveEnabled(boolean enabled) {
    saveEnabled = enabled;
  }

  /**
   * Indicates whether this director will save its state.
   */
  public boolean isSaveEnabled() {
    return saveEnabled;
  }

  /**
   * Requests focus for its host {@code Stage} if it's a child {@code Director} of a {@link Scene}.
   */
  public abstract void requestFocus();

  void setFocusedStage(Stage stage) {
    this.focusedStage = stage;
    requestFocus();
  }

  /**
   * Returns the focused {@link Stage}. The focused one will be the first
   * to be asked in {@link #onHandleBack()}.
   *
   * @see Stage#requestFocus()
   * @see #onHandleBack()
   */
  @Nullable
  public Stage getFocusedStage() {
    return focusedStage;
  }

  /**
   * Sets a {@link BackHandler} for this {@code Director}.
   * It overrides the default back action handling method.
   */
  public void setBackHandler(@Nullable BackHandler<Director> backHandler) {
    this.backHandler = backHandler;
  }

  /**
   * Handles the back button being pressed.
   * Returns {@code true} if the back action is consumed.
   * <p>
   * Usually it's called in {@link Activity#onBackPressed()}, like: <pre>{@code
   * public void onBackPressed() {
   *   if (!director.handleBack()) {
   *     super.onBackPressed();
   *   }
   * }
   * }</pre>
   * <p>
   * The method calls {@link #onHandleBack()} directly, but it could be override by setting
   * a {@link BackHandler}.
   *
   * @see #setBackHandler(BackHandler)
   * @see #onHandleBack()
   */
  public boolean handleBack() {
    if (backHandler != null) {
      return backHandler.handleBack(this);
    } else {
      return onHandleBack();
    }
  }

  /**
   * This method is how a {@code Director} handles back action in default.
   * Returns {@code true} if the back action is consumed.
   * <p>
   * It calls {@link Stage#handleBack()} on the focused {@link Stage}.
   * If the back action isn't consumed, it traversal every {@code Stage} until
   * the back action is consumed.
   */
  public boolean onHandleBack() {
    if (focusedStage != null && focusedStage.handleBack()) {
      return true;
    }
    for (int i = 0, n = stageMap.size(); i < n; i++) {
      Stage stage = stageMap.valueAt(i);
      if (stage == focusedStage) {
        continue;
      }
      if (stage.handleBack()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Sets the curtain suppler for its all {@link Stage}s.
   * {@link Stage} can override it by calling {@link Stage#setCurtainSuppler(CurtainSuppler)}.
   */
  public void setCurtainSuppler(@Nullable CurtainSuppler suppler) {
    this.curtainSuppler = suppler;
  }

  @Nullable
  Curtain requestCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    return curtainSuppler != null ? curtainSuppler.getCurtain(upper, lower) : null;
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

  abstract boolean willDestroyActivity();

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

  void detach(boolean saveViewStateIfNecessary) {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
      assertFalse(isDestroyed);
    }

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      Stage stage = stageMap.valueAt(i);
      stage.detach(saveViewStateIfNecessary);
    }
  }

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

  void finish(boolean willRecreate) {
    assertFalse(isFinishing);
    this.isFinishing = true;

    if (willRecreate) {
      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        Stage stage = stageMap.valueAt(i);
        stage.setWillRecreate();
      }
    }
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
