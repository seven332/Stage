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

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * A {@code Scene} manages a portion of the UI.
 * It is similar to an Activity or Fragment in that it manages its own lifecycle and
 * controls interactions between the UI and whatever logic is required.
 */
public abstract class Scene {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  public static final int INVALID_ID = 0;

  @IntDef({TRANSPARENT, TRANSLUCENT, OPAQUE})
  @Retention(RetentionPolicy.CLASS)
  public @interface Opacity {}
  /**
   * The {@code Scene} under this one is visible.
   */
  public static final int TRANSPARENT = 0;
  /**
   * The {@code Scene} under this one is only visible if this {@code Scene} is the top {@code Scene}.
   */
  public static final int TRANSLUCENT = 1;
  /**
   * The {@code Scene} under this one is invisible.
   */
  public static final int OPAQUE = 2;

  private static final int STATE_NONE = 0;
  private static final int STATE_CREATED = 1;
  private static final int STATE_ATTACHED = 2;
  private static final int STATE_STARTED = 3;
  private static final int STATE_RESUMED = 4;
  private static final int STATE_PAUSED = 5;
  private static final int STATE_STOPPED = 6;
  private static final int STATE_DETACHED = 7;
  private static final int STATE_DESTROYED = 8;

  private static final String KEY_ID = "Scene:id";
  private static final String KEY_TAG = "Scene:tag";
  private static final String KEY_ARGS = "Scene:args";
  private static final String KEY_VIEW_STATE = "Scene:view_state";
  private static final String KEY_VIEW_STATE_HIERARCHY = "Scene:view_state:hierarchy";
  private static final String KEY_VIEW_STATE_BUNDLE = "Scene:view_state:bundle";
  private static final String KEY_CHILD_DIRECTOR = "Scene:child_director";

  private Stage stage;
  private int savedId = INVALID_ID;
  private int id = INVALID_ID;
  private String tag;
  private Bundle args;
  private boolean willRetainView;
  @Opacity
  private int opacity = OPAQUE;

  private View view;
  private Bundle viewState;

  private int state = STATE_NONE;
  private boolean isFinishing;

  private SceneHostedDirector childDirector;

  @NonNull
  static Scene newInstance(String className, @NonNull Bundle bundle) {
    Scene scene = Utils.newInstance(className);
    scene.restoreInstanceState(bundle);
    return scene;
  }

  /**
   * Returns the {@link Stage} which this {@code Scene} is performed on.
   * It only returns a valid value between {@link #onCreate(Bundle)}
   * and {@link #onDestroy()}, or {@code null}.
   */
  public final Stage getStage() {
    return stage;
  }

  private void setSavedId(int id) {
    savedId = id;
  }

  int getSavedId() {
    return savedId;
  }

  /**
   * Returns the id of this {@code Scene}.
   * It only returns a valid id between {@link #onCreate(Bundle)}
   * and {@link #onDestroy()}, or {@link #INVALID_ID}.
   * <p>
   * Each {@code Scene} in the same Activity has a different id.
   */
  public final int getId() {
    return id;
  }

  void setTag(String tag) {
    this.tag = tag;
  }

  /**
   * Returns the tag passed in {@link Announcer#tag(String)}.
   */
  public final String getTag() {
    return tag;
  }

  void setArgs(Bundle args) {
    this.args = args;
  }

  /**
   * Returns arguments passed in {@link Announcer#args(Bundle)}.
   */
  public final Bundle getArgs() {
    return args;
  }

  /**
   * If the view of this {@code Scene} should be retained after detached for next attaching,
   * set this flag. This is useful when a {@code Scene}'s view hierarchy is expensive to
   * tear down and rebuild.
   * <p>
   * The view must be destroyed if the host {@link Activity} is destroyed,
   * or it will cause memory leak.
   * <p>
   * It's can only be called before or in {@link #onCreate(Bundle)}.
   *
   * @see #willRetainView()
   */
  protected final void setWillRetainView(boolean willRetainView) {
    assertState(STATE_NONE);
    this.willRetainView = willRetainView;
  }

  /**
   * Returns whether or not this {@code Scene} retains view after detached.
   * {@code false} in default.
   *
   * @see #setWillRetainView(boolean)
   */
  public final boolean willRetainView() {
    return willRetainView;
  }

  /**
   * Describes How this {@code Scene} affects the visibility of the {@code Scene} below.
   * Must be one of {@link #TRANSPARENT}, {@link #TRANSLUCENT} and {@link #OPAQUE}.
   * <p>
   * It's can only be called before or in {@link #onCreate(Bundle)}.
   *
   * @see #getOpacity()
   */
  protected final void setOpacity(@Opacity int opacity) {
    assertState(STATE_NONE);
    this.opacity = opacity;
  }

  /**
   * Returns opacity value of this {@code Scene}. {@link #OPAQUE} in default.
   *
   * @see #setOpacity(int)
   */
  @Opacity
  public final int getOpacity() {
    return opacity;
  }

  /**
   * Returns the view of this {@code Scene}.
   * Only return non-{@code null} between {@link #onCreateView(LayoutInflater, ViewGroup)} and
   * {@link #onDestroyView(View)}.
   */
  @Nullable
  public View getView() {
    return view;
  }

  /**
   * Returns {@code true} if the view is visible for user.
   */
  public boolean isStarted() {
    return state >= STATE_STARTED && state < STATE_STOPPED;
  }

  /**
   * Returns {@code true} if the view is in the foreground.
   */
  public boolean isResumed() {
    return state == STATE_RESUMED;
  }

  /**
   * Returns {@code true} if the view is attached.
   */
  public boolean isViewAttached() {
    return state >= STATE_ATTACHED && state < STATE_DETACHED;
  }

  /**
   * Check to see whether this {@code Scene} is in the process of finishing.
   * It starts returning {@code true} from this {@code Scene} popped from the stack.
   */
  public boolean isFinishing() {
    return isFinishing;
  }

  /**
   * Returns true if the final {@link #onDestroy()} call has been made
   * on the {@code Scene}, so this instance is now dead.
   */
  public boolean isDestroyed() {
    return state == STATE_DESTROYED;
  }

  /**
   * Pops this {@code Scene} from its {@link Stage}.
   * It's a no-op is it's not in a Stage.
   */
  public void pop() {
    if (stage != null) {
      stage.popScene(this);
    }
  }

  /**
   * Requests focus for its host {@code Stage}.
   */
  public void requestFocus() {
    if (stage != null) {
      stage.requestFocus();
    }
  }

  /**
   * Handle the back button being pressed.
   * Returns {@code true} if the back action is consumed.
   * <p>
   * In default, it calls {@link Director#handleBack()} on its child director.
   */
  public boolean handleBack() {
    if (childDirector != null) {
      return childDirector.handleBack();
    } else {
      return false;
    }
  }

  @Nullable
  Curtain requestCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    return stage != null ? stage.requestCurtain(upper, lower) : null;
  }

  int requireSceneId() {
    return stage.requireSceneId();
  }

  /**
   * Look for a child {@link Scene} with the given id.
   */
  @Nullable
  public Scene findSceneById(int sceneId) {
    if (sceneId == id) {
      return this;
    }
    if (childDirector != null) {
      return childDirector.findSceneById(sceneId);
    }
    return null;
  }

  /**
   * Hires a {@link Director} to direct {@link Stage}s
   * on the {@code ViewGroup} of this {@code Scene}.
   */
  public Director hireChildDirector() {
    if (isDestroyed()) {
      throw new IllegalStateException("Can't call hireChildDirector() on a destroyed Scene");
    }

    if (childDirector == null) {
      childDirector = new SceneHostedDirector();
      childDirector.setScene(this);

      // Restore child director lifecycle
      if (isStarted()) {
        childDirector.start();
      }
      if (isResumed()) {
        childDirector.resume();
      }
      if (isFinishing()) {
        childDirector.finish();
      }
    }

    return childDirector;
  }

  /**
   * Return the {@link Activity} this {@code Scene} is currently associated with.
   */
  @Nullable
  public final Activity getActivity() {
    return stage != null ? stage.getActivity() : null;
  }

  /**
   * Return the {@link Application} this {@code Scene} is currently associated with.
   */
  @Nullable
  public final Application getApplication() {
    Activity activity = getActivity();
    return activity != null ? activity.getApplication() : null;
  }

  /**
   * Same as {@link Activity#startActivity(Intent)}.
   * It's a no-op if this {@code Scene} hasn't been created or it's destroyed.
   */
  public void startActivity(@NonNull Intent intent) {
    if (stage != null) {
      stage.startActivity(intent);
    }
  }

  /**
   * Same as {@link Activity#startActivity(Intent, Bundle)}.
   * It's a no-op if this {@code Scene} hasn't been created or it's destroyed.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  public void startActivity(@NonNull Intent intent, @Nullable Bundle options) {
    if (stage != null) {
      stage.startActivity(intent, options);
    }
  }

  /**
   * Same as {@link Activity#startActivityForResult(Intent, int)}.
   * It's a no-op if this {@code Scene} hasn't been created or it's destroyed.
   */
  public void startActivityForResult(Intent intent, int requestCode) {
    if (stage != null) {
      stage.startActivityForResult(id, intent, requestCode);
    }
  }

  /**
   * Same as {@link Activity#startActivityForResult(Intent, int, Bundle)}.
   * It's a no-op if this {@code Scene} hasn't been created or it's destroyed.
   */
  @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
  public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
    if (stage != null) {
      stage.startActivityForResult(id, intent, requestCode, options);
    }
  }

  /**
   * Same as {@link Activity#onActivityResult(int, int, Intent)}.
   */
  @CallSuper
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (childDirector != null) {
      childDirector.onActivityResult(requestCode, resultCode, data);
    }
  }

  /**
   * Same as {@code ActivityCompat#requestPermissions(Activity, String[], int)}.
   * It's a no-op if this {@code Scene} hasn't been created or it's destroyed.
   */
  public void requestPermissions(@NonNull String[] permissions, int requestCode) {
    if (stage != null) {
      stage.requestPermissions(id, permissions, requestCode);
    }
  }

  /**
   * Same as {@link Activity#onRequestPermissionsResult(int, String[], int[])}.
   */
  @CallSuper
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (childDirector != null) {
      childDirector.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
  }

  private void assertState(int state) {
    if (this.state != state) {
      throw new IllegalStateException("State should be " + state + ", but it's " + this.state);
    }
  }

  private void updateState(int newState, int currentState) {
    if (DEBUG) {
      assertState(currentState);
    }
    state = newState;
  }

  private void updateState(int newState, int currentState1, int currentState2) {
    if (DEBUG) {
      if (state != currentState1 && state != currentState2) {
        throw new IllegalStateException(
            "State should be " + currentState1 + " or " + currentState2 +
                ", but it's " + state);
      }
    }
    state = newState;
  }

  void create(@NonNull Stage stage, int id) {
    if (this.stage != null) {
      throw new IllegalStateException("This Scene has been performed, can't perform is twice: "
          + getClass().getName());
    }
    this.stage = stage;

    if (state == STATE_DESTROYED) {
      throw new IllegalStateException("This scene has been destroyed: " + getClass().getName());
    }

    this.id = id;

    onCreate(args);

    // Update state here to allow getTag() and others can be called in onCreate()
    updateState(STATE_CREATED, STATE_NONE);
  }

  @NonNull
  private View inflate(@NonNull ViewGroup parent) {
    if (view == null) {
      view = onCreateView(LayoutInflater.from(parent.getContext()), parent);
      if (view == parent) {
        throw new IllegalStateException("onCreateView() returned the parent ViewGroup. "
            + "Perhaps you forgot to pass false for "
            + "LayoutInflater.inflate()'s attachToRoot parameter?");
      }
      restoreViewState(view);
    }
    return view;
  }

  void attachView(ViewGroup container) {
    attachView(container, container.getChildCount());
  }

  void attachView(ViewGroup container, int index) {
    updateState(STATE_ATTACHED, STATE_CREATED, STATE_DETACHED);

    View view = inflate(container);

    if (DEBUG) {
      assertNull(view.getParent());
    }

    container.addView(view, index);
    onAttachView(view);
  }

  void start() {
    updateState(STATE_STARTED, STATE_ATTACHED, STATE_STOPPED);

    if (childDirector != null) {
      childDirector.start();
    }

    onStart();
  }

  void resume() {
    updateState(STATE_RESUMED, STATE_STARTED, STATE_PAUSED);

    if (childDirector != null) {
      childDirector.resume();
    }

    onResume();
  }

  void pause() {
    updateState(STATE_PAUSED, STATE_RESUMED);

    if (childDirector != null) {
      childDirector.pause();
    }

    onPause();
  }

  void stop() {
    updateState(STATE_STOPPED, STATE_STARTED, STATE_PAUSED);

    if (childDirector != null) {
      childDirector.stop();
    }

    onStop();
  }

  private void destroyView() {
    if (childDirector != null) {
      childDirector.detach();
    }

    onDestroyView(view);
  }

  private void destroy() {
    updateState(STATE_DESTROYED, STATE_CREATED, STATE_DETACHED);

    if (DEBUG) {
      if (!willRetainView) {
        assertNull(view);
      }
    }

    if (view != null) {
      destroyView();
      view = null;
    }

    if (childDirector != null) {
      childDirector.destroy();
      childDirector = null;
    }

    onDestroy();

    stage = null;
    id = INVALID_ID;
  }

  void finish() {
    isFinishing = true;

    if (childDirector != null) {
      childDirector.finish();
    }

    if (state == STATE_CREATED || state == STATE_DETACHED) {
      // No need to wait view detach, call destroy() now
      destroy();
    }
  }

  void detachView(@NonNull ViewGroup container) {
    detachView(container, false);
  }

  void detachView(@NonNull ViewGroup container, boolean forceDestroyView) {
    updateState(STATE_DETACHED, STATE_ATTACHED, STATE_STOPPED);

    // If retaining view, no need to recreate view before saveViewState() called,
    // no need to restore view state, no need to save view state
    if (!willRetainView) {
      saveInstanceState();
    }

    if (DEBUG) {
      assertNotNull(view);
    }

    if (view.getParent() != container) {
      throw new IllegalStateException("Don't detach view by yourself");
    }
    container.removeView(view);
    onDetachView(view);

    if (!willRetainView || forceDestroyView) {
      destroyView();
      view = null;
    }

    if (isFinishing) {
      destroy();
    }
  }

  private void saveViewState(@NonNull View view) {
    viewState = new Bundle(getClass().getClassLoader());

    SparseArray<Parcelable> hierarchyState = new SparseArray<>();
    view.saveHierarchyState(hierarchyState);
    viewState.putSparseParcelableArray(KEY_VIEW_STATE_HIERARCHY, hierarchyState);

    Bundle stateBundle = new Bundle(getClass().getClassLoader());
    onSaveViewState(view, stateBundle);
    viewState.putBundle(KEY_VIEW_STATE_BUNDLE, stateBundle);
  }

  private void restoreViewState(@NonNull View view) {
    if (viewState != null) {
      view.restoreHierarchyState(viewState.getSparseParcelableArray(KEY_VIEW_STATE_HIERARCHY));

      Bundle savedViewState = viewState.getBundle(KEY_VIEW_STATE_BUNDLE);
      if (savedViewState != null) {
        savedViewState.setClassLoader(getClass().getClassLoader());
        onRestoreViewState(view, savedViewState);
      }
    }
  }

  Bundle saveInstanceState() {
    Bundle outState = new Bundle();
    outState.putInt(KEY_ID, getId());
    outState.putString(KEY_TAG, getTag());
    outState.putBundle(KEY_ARGS, getArgs());

    if (view != null) {
      saveViewState(view);
    }
    outState.putBundle(KEY_VIEW_STATE, viewState);

    if (childDirector != null) {
      Bundle childDirectorState = new Bundle();
      childDirector.saveInstanceState(childDirectorState);
      outState.putBundle(KEY_CHILD_DIRECTOR, childDirectorState);
    }

    onSaveInstanceState(outState);

    return outState;
  }

  void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    setSavedId(savedInstanceState.getInt(KEY_ID, INVALID_ID));
    setTag(savedInstanceState.getString(KEY_TAG, null));
    setArgs(savedInstanceState.getBundle(KEY_ARGS));

    viewState = savedInstanceState.getBundle(KEY_VIEW_STATE);
    if (viewState != null) {
      viewState.setClassLoader(getClass().getClassLoader());
    }

    Bundle childDirectorState = savedInstanceState.getBundle(KEY_CHILD_DIRECTOR);
    if (childDirectorState != null) {
      childDirector = new SceneHostedDirector();
      childDirector.setScene(this);
      childDirector.restoreInstanceState(childDirectorState);
    }

    onRestoreInstanceState(savedInstanceState);
  }

  /**
   * Called when the {@code Scene} is being pushed to the stack.
   * It's where most non-view initialization should go.
   */
  @CallSuper
  protected void onCreate(@Nullable Bundle args) {}

  /**
   * Called when the {@code Scene} is ready to display its view.
   * A valid view must be returned. It's where most view binding should go.
   */
  @NonNull
  protected abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

  /**
   * Called when the {@code Scene} view is attached to its container ViewGroup.
   * <p>
   * If {@link #willRetainView()} returns {@code false},
   * {@link #onCreateView(LayoutInflater, ViewGroup)} is always called before it.
   */
  @CallSuper
  protected void onAttachView(@NonNull View view) {}

  /**
   * Called when the {@code Scene} view becoming visible.
   * <p>
   * Activity lifecycle is integrated to Scene lifecycle.
   * Only if {@link Activity#onStart()} has been called, it could be called.
   */
  @CallSuper
  protected void onStart() {}

  /**
   * Called after the {@code Scene} becoming the top scene.
   * <p>
   * Activity lifecycle is integrated to Scene lifecycle.
   * Only if {@link Activity#onResume()} has been called, it could be called.
   */
  @CallSuper
  protected void onResume() {}

  /**
   * Called when the {@code Scene} is not the top scene. The counterpart to {@link #onResume}.
   * <p>
   * Activity lifecycle is integrated to Scene lifecycle.
   * Once {@link Activity#onPause()} is called, it is called on all resumed Scene.
   */
  @CallSuper
  protected void onPause() {}

  /**
   * Called when the {@code Scene} becoming invisible. The counterpart to {@link #onStart()}.
   * <p>
   * Activity lifecycle is integrated to Scene lifecycle.
   * Once {@link Activity#onStop()} is called, it is called on all started Scene.
   */
  @CallSuper
  protected void onStop() {}

  /**
   * Called when the {@code Scene} view is detached from its container ViewGroup.
   * The counterpart to {@link #onAttachView(View)}.
   */
  @CallSuper
  protected void onDetachView(@NonNull View view) {}

  /**
   * Called when the {@code Scene} view is ready to be destroyed.
   * The counterpart to {@link #onCreateView(LayoutInflater, ViewGroup)}.
   */
  @CallSuper
  protected void onDestroyView(@NonNull View view) {}

  /**
   * Called when the {@code Scene} is being popped from the stack.
   * The counterpart to {@link #onCreate(Bundle)}.
   */
  @CallSuper
  protected void onDestroy() {}

  /**
   * Saves the view state.
   */
  @CallSuper
  protected void onSaveViewState(@NonNull View view, @NonNull Bundle outState) {}

  /**
   * Restores state to the view that was saved in {@link #onSaveViewState(View, Bundle)}.
   */
  @CallSuper
  protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {}

  /**
   * Saves this {@code Scene}'s state.
   */
  @CallSuper
  protected void onSaveInstanceState(@NonNull Bundle outState) {}

  /**
   * Restores state to this {@code Scene} that was saved in {@link #onSaveInstanceState(Bundle)}.
   */
  @CallSuper
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {}

  /**
   * Returns a curtain when this {@code Scene} is upper.
   * <p>
   * It has the highest priority. If it returns {@code null},
   * {@link CurtainSuppler#getCurtain(SceneInfo, List)} is called
   * on the {@code CurtainSuppler} of its host {@link Stage}.
   * <p>
   * If no animation should be played, returns
   * {@link com.hippo.stage.curtain.NoOpCurtain#INSTANCE} instead of {@code null}.
   */
  @Nullable
  protected Curtain onCreateCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    return null;
  }

  @Override
  public String toString() {
    return getClass().getName() + "{"
        + "hash_code: " + Integer.toHexString(System.identityHashCode(this)) + ", "
        + "opacity: " + opacity + ", "
        + "tag: " + tag + "}";
  }
}
