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

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@code Scene} manages a portion of the UI.
 * It is similar to an Activity or Fragment in that it manages its own lifecycle and
 * controls interactions between the UI and whatever logic is required.
 */
public abstract class Scene {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  @IntDef({TRANSPARENT, TRANSLUCENT, OPAQUE})
  @Retention(RetentionPolicy.CLASS)
  public @interface Opacity {}
  public static final int INVALID = -1;
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

  private static final String KEY_CLASS_NAME = "Scene:class_name";
  private static final String KEY_TAG = "Scene:tag";
  private static final String KEY_ARGS = "Scene:args";
  private static final String KEY_WILL_RETAIN_VIEW = "Scene:will_retain_view";
  private static final String KEY_VIEW_STATE = "Scene:view_state";
  private static final String KEY_VIEW_STATE_HIERARCHY = "Scene:view_state:hierarchy";
  private static final String KEY_VIEW_STATE_BUNDLE = "Scene:view_state:bundle";

  private Stage stage;
  private String tag;
  private Bundle args;
  private boolean willRetainView;
  private int opacity = INVALID;

  private View view;
  private Bundle viewState;

  private int state = STATE_NONE;
  private boolean isFinishing;

  @NonNull
  static Scene newInstance(@NonNull Bundle bundle) {
    final String className = bundle.getString(KEY_CLASS_NAME);
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

  /**
   * Sets tag for this {@code Scene}.
   * It's can only be called before {@link #onCreate(Bundle)}.
   *
   * @see #getTag()
   */
  public final void setTag(String tag) {
    assertState(STATE_NONE);
    this.tag = tag;
  }

  /**
   * Returns tag.
   *
   * @see #setTag(String)
   */
  public final String getTag() {
    return tag;
  }

  /**
   * Sets arguments for this {@code Scene}.
   * It's can only be called before {@link #onCreate(Bundle)}.
   *
   * @see #getArgs()
   */
  public final void setArgs(Bundle args) {
    assertState(STATE_NONE);
    this.args = args;
  }

  /**
   * Returns arguments.
   *
   * @see #setArgs(Bundle)
   */
  public final Bundle getArgs() {
    return args;
  }

  /**
   * If the view of this {@code Scene} should be retained after detached for next attaching,
   * set this flag. This is useful when a {@code Scene}'s view hierarchy is expensive to
   * tear down and rebuild.
   * It's can only be called before {@link #onCreate(Bundle)}.
   *
   * @see #willRetainView()
   */
  public final void setWillRetainView(boolean willRetainView) {
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
   * Returns the view of this {@code Scene}.
   * Only return non-{@code null} between {@link #onCreateView(LayoutInflater, ViewGroup)} and
   * {@link #onDestroyView(View)}.
   */
  @Nullable
  public View getView() {
    return view;
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

  int opacity() {
    if (opacity == INVALID) {
      throw new IllegalStateException("Shouldn't call opacity() now");
    }
    return opacity;
  }

  void create(@NonNull Stage stage) {
    if (this.stage != null) {
      throw new IllegalStateException("This Scene has been performed, can't perform is twice: "
          + getClass().getName());
    }
    this.stage = stage;

    if (state == STATE_DESTROYED) {
      throw new IllegalStateException("This scene has been destroyed: " + getClass().getName());
    }

    onCreate(args);

    opacity = onGetOpacity();

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
    onStart();
  }

  void resume() {
    updateState(STATE_RESUMED, STATE_STARTED, STATE_PAUSED);
    onResume();
  }

  void pause() {
    updateState(STATE_PAUSED, STATE_RESUMED);
    onPause();
  }

  void stop() {
    updateState(STATE_STOPPED, STATE_STARTED, STATE_PAUSED);
    onStop();
  }


  private void destroy() {
    updateState(STATE_DESTROYED, STATE_CREATED, STATE_DETACHED);

    if (DEBUG) {
      if (!willRetainView) {
        assertNull(view);
      }
    }

    if (view != null) {
      onDestroyView(view);
      view = null;
    }

    onDestroy();

    stage = null;
  }

  void finish() {
    isFinishing = true;

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
      onDestroyView(view);
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
      savedViewState.setClassLoader(getClass().getClassLoader());
      onRestoreViewState(view, savedViewState);
    }
  }

  Bundle saveInstanceState() {
    Bundle outState = new Bundle();
    outState.putString(KEY_TAG, getTag());
    outState.putBundle(KEY_ARGS, getArgs());
    outState.putBoolean(KEY_WILL_RETAIN_VIEW, willRetainView());

    if (view != null) {
      saveViewState(view);
    }
    outState.putBundle(KEY_VIEW_STATE, viewState);

    return outState;
  }

  void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    setTag(savedInstanceState.getString(KEY_TAG, null));
    setArgs(savedInstanceState.getBundle(KEY_ARGS));
    setWillRetainView(savedInstanceState.getBoolean(KEY_WILL_RETAIN_VIEW, false));

    viewState = savedInstanceState.getBundle(KEY_VIEW_STATE);
    if (viewState != null) {
      viewState.setClassLoader(getClass().getClassLoader());
    }
  }

  protected void onCreate(@Nullable Bundle args) {}

  @NonNull
  protected abstract View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

  protected void onAttachView(@NonNull View view) {}

  protected void onStart() {}

  protected void onResume() {}

  protected void onPause() {}

  protected void onStop() {}

  protected void onDetachView(@NonNull View view) {}

  protected void onDestroyView(@NonNull View view) {}

  protected void onDestroy() {}

  protected void onSaveViewState(@NonNull View view, @NonNull Bundle outState) {}

  protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {}

  /**
   * Describes How this {@code Scene} affects the visibility of the {@code Scene} below.
   * Must be one of {@link #TRANSPARENT}, {@link #TRANSLUCENT} and {@link #OPAQUE}.
   */
  @Opacity
  protected abstract int onGetOpacity();

  @Override
  public String toString() {
    return getClass().getName() + "{"
        + "hash_code: " + Integer.toHexString(System.identityHashCode(this)) + ", "
        + "opacity: " + opacity + ", "
        + "tag: " + tag + "}";
  }
}
