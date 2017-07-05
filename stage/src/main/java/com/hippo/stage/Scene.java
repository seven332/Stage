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
import android.content.Context;
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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
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
  @Retention(RetentionPolicy.SOURCE)
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

  private static final String KEY_ID = "Scene:id";
  private static final String KEY_TAG = "Scene:tag";
  private static final String KEY_ARGS = "Scene:args";
  private static final String KEY_WILL_RETAIN_VIEW = "Scene:will_retain_view";
  private static final String KEY_OPACITY = "Scene:opacity";
  private static final String KEY_THEME = "Scene:theme";
  private static final String KEY_TARGET = "Scene:target";
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
  private int theme;
  private int target = INVALID_ID;

  private Context context;
  private View view;
  private Bundle viewState;

  private LifecycleState lifecycleState = new LifecycleState();
  private boolean willDestroy;
  private boolean willRecreate;

  private SceneHostedDirector childDirector;

  private final List<LifecycleListener> lifecycleListeners = new ArrayList<>();

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
  @Nullable
  public final Stage getStage() {
    return stage;
  }

  /**
   * Returns the lifecycle state of this scene.
   */
  @NonNull
  public final LifecycleState getLifecycleState() {
    return lifecycleState;
  }

  // The id from saveInstanceState Bundle
  private void setSavedId(int id) {
    savedId = id;
  }

  int getSavedId() {
    return savedId;
  }

  /**
   * Returns the id of this {@code Scene}.
   * It only returns a valid id after the {@code Scene} pushed to a {@link Stage},
   * or {@link #INVALID_ID}.
   * <p>
   * Each {@code Scene} in the same Activity has a different id.
   */
  public final int getId() {
    return id;
  }

  /**
   * Sets a tag for this {@code Scene}.
   * The tag could be used for {@link Stage#findSceneByTag(String)}.
   * <p>
   * The tag supplied here will be retained across scene destroy and
   * creation.
   *
   * @see #getTag()
   * @see Stage#findSceneByTag(String)
   */
  public void setTag(@Nullable String tag) {
    this.tag = tag;
  }

  /**
   * Returns the tag passed in {@link #setTag(String)}.
   *
   * @see #setTag(String)
   * @see Stage#findSceneByTag(String)
   */
  @Nullable
  public final String getTag() {
    return tag;
  }

  /**
   * Supply the construction arguments for this scene. It can only
   * be called before being pushed to a stage.
   * <p>
   * The arguments supplied here will be retained across scene destroy and
   * creation.
   *
   * @see #getArgs()
   */
  public void setArgs(@Nullable Bundle args) {
    lifecycleState.assertState(LifecycleState.STATE_NONE);
    this.args = args;
  }

  /**
   * Returns the arguments passed in {@link #setArgs(Bundle)}.
   *
   * @see #setArgs(Bundle)
   */
  @Nullable
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
   * It can only be called before the scene has been pushed to a stage,
   * or in {@link #onCreate(Bundle)}.
   * <p>
   * The value supplied here will be retained across scene destroy and
   * creation.
   *
   * @see #willRetainView()
   */
  public final void setWillRetainView(boolean willRetainView) {
    lifecycleState.assertState(LifecycleState.STATE_NONE);
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
   * It can only be called before the scene has been pushed to a stage,
   * or in {@link #onCreate(Bundle)}.
   * <p>
   * The value supplied here will be retained across scene destroy and
   * creation.
   *
   * @see #getOpacity()
   */
  public final void setOpacity(@Opacity int opacity) {
    lifecycleState.assertState(LifecycleState.STATE_NONE);
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
   * Sets the theme of this {@code Scene}. {@code 0} for the default theme.
   * It takes affect in next {@link #onCreateView(LayoutInflater, ViewGroup)}.
   * <p>
   * The arguments supplied here will be retained across scene destroy and
   * creation.
   *
   * @see #getTheme()
   */
  public final void setTheme(int theme) {
    this.theme = theme;
  }

  /**
   * Returns theme of this {@code Scene}. {@code 0} in default.
   *
   * @see #setTheme(int)
   */
  public final int getTheme() {
    return theme;
  }

  /**
   * Sets the target scene. {@code null} to clear target.
   * The target scene should be stage before it called.
   *
   * @see #getTarget()
   */
  public final void setTarget(Scene scene) {
    if (scene != null) {
      target = scene.id;
    } else {
      target = INVALID_ID;
    }
  }

  /**
   * Gets the target scene.
   *
   * @see #setTarget(Scene)
   */
  @Nullable
  public final Scene getTarget() {
    if (target == INVALID_ID) {
      return null;
    }

    // Get root director
    Scene scene = this;
    Director root = null;
    for (;;) {
      if (scene == null) {
        break;
      }
      Stage stage = scene.getStage();
      if (stage == null) {
        break;
      }
      Director director = stage.getDirector();
      if (director instanceof SceneHostedDirector) {
        scene = ((SceneHostedDirector) director).getScene();
      } else {
        root = director;
        break;
      }
    }

    if (root != null) {
      return root.findSceneById(target);
    } else {
      return null;
    }
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
   * Adds a listener for all of this {@code Scene}'s lifecycle events.
   *
   * @param lifecycleListener The listener
   */
  public void addLifecycleListener(@NonNull LifecycleListener lifecycleListener) {
    if (!lifecycleListeners.contains(lifecycleListener)) {
      lifecycleListeners.add(lifecycleListener);
    }
  }

  /**
   * Removes a previously added lifecycle listener.
   *
   * @param lifecycleListener The listener to be removed
   */
  public void removeLifecycleListener(@NonNull LifecycleListener lifecycleListener) {
    lifecycleListeners.remove(lifecycleListener);
  }

  /**
   * Check to see whether this {@code Scene} will be destroyed.
   * It starts returning {@code true} from this {@code Scene} popped from the stack.
   */
  public boolean willDestroy() {
    return willDestroy;
  }

  /**
   * Returns {@code true} if this {@code Scene} will be recreated.
   */
  public boolean willRecreate() {
    return willRecreate;
  }

  /**
   * Pops this {@code Scene} from its {@link Stage}.
   * It's a no-op is it's not in a Stage.
   */
  public void pop() {
    // No need to pop a finishing scene
    if (!willDestroy && stage != null) {
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
  @NonNull
  public Director hireChildDirector() {
    if (lifecycleState.hasDestroyed()) {
      throw new IllegalStateException("Can't call hireChildDirector() on a destroyed Scene");
    }

    if (childDirector == null) {
      childDirector = new SceneHostedDirector();
      childDirector.setScene(this);

      // Restore child director lifecycle
      if (lifecycleState.isStarted()) {
        childDirector.start();
      }
      if (lifecycleState.isResumed()) {
        childDirector.resume();
      }
      if (willDestroy()) {
        childDirector.finish(willRecreate);
      }
    }

    return childDirector;
  }

  /**
   * Returns {@code true} if its host {@link Activity} is destroyed or this {@code Scene}
   * is not attached to a {@link Stage}.
   */
  public boolean willDestroyActivity() {
    return stage == null || stage.willDestroyActivity();
  }

  /**
   * Returns the {@link Context} the view of this {@code Scene} is currently associated with.
   * It's guaranteed that this method returns {@code non-null} between
   * {@link #onCreateView(LayoutInflater, ViewGroup)} and {@link #onDestroyView(View)}.
   */
  @Nullable
  public final Context getContext() {
    return context;
  }

  /**
   * Return the {@link Activity} this {@code Scene} is currently associated with.
   * It's guaranteed that this method returns {@code non-null} between
   * {@link #onCreateView(LayoutInflater, ViewGroup)} and {@link #onDestroyView(View)}.
   */
  @Nullable
  public final Activity getActivity() {
    return stage != null ? stage.getActivity() : null;
  }

  /**
   * Return the {@link Application} this {@code Scene} is currently associated with.
   * It's guaranteed that this method returns {@code non-null} between
   * {@link #onCreateView(LayoutInflater, ViewGroup)} and {@link #onDestroyView(View)}.
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

  void create(@NonNull Stage stage, int id) {
    if (this.stage != null) {
      throw new IllegalStateException("This Scene has been performed, can't perform is twice: "
          + getClass().getName());
    }
    this.stage = stage;

    if (lifecycleState.hasDestroyed()) {
      throw new IllegalStateException("This scene has been destroyed: " + getClass().getName());
    }

    this.id = id;

    onCreate(args);

    lifecycleState.updateState(LifecycleState.STATE_CREATED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onCreate(this);
      }
    }
  }

  @NonNull
  private View inflate(@NonNull ViewGroup parent) {
    if (view == null) {

      if (DEBUG) {
        assertNull(context);
      }

      context = parent.getContext();
      if (theme != 0) {
        context = new ContextThemeWrapper(context, theme);
      }

      view = onCreateView(LayoutInflater.from(context), parent);
      if (view == parent) {
        throw new IllegalStateException("onCreateView() returned the parent ViewGroup. "
            + "Perhaps you forgot to pass false for "
            + "LayoutInflater.inflate()'s attachToRoot parameter?");
      }

      lifecycleState.updateState(LifecycleState.STATE_VIEW_CREATED);

      if (!lifecycleListeners.isEmpty()) {
        for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
          listener.onCreateView(this);
        }
      }

      restoreViewState(view);
    }
    return view;
  }

  void attachView(ViewGroup container) {
    attachView(container, container.getChildCount());
  }

  void attachView(ViewGroup container, int index) {
    View view = inflate(container);

    if (DEBUG) {
      assertNull(view.getParent());
    }

    container.addView(view, index);

    onAttachView(view);

    lifecycleState.updateState(LifecycleState.STATE_VIEW_ATTACHED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onAttachView(this, view);
      }
    }
  }

  void start() {
    if (childDirector != null) {
      childDirector.start();
    }

    onStart();

    lifecycleState.updateState(LifecycleState.STATE_STARTED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onStart(this);
      }
    }
  }

  void resume() {
    if (childDirector != null) {
      childDirector.resume();
    }

    onResume();

    lifecycleState.updateState(LifecycleState.STATE_RESUMED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onResume(this);
      }
    }
  }

  void pause() {
    if (childDirector != null) {
      childDirector.pause();
    }

    onPause();

    lifecycleState.updateState(LifecycleState.STATE_PAUSED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onPause(this);
      }
    }
  }

  void stop() {
    if (childDirector != null) {
      childDirector.stop();
    }

    onStop();

    lifecycleState.updateState(LifecycleState.STATE_STOPPED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onStop(this);
      }
    }
  }

  private void destroyView() {
    if (childDirector != null) {
      childDirector.detach();
    }

    onDestroyView(view);

    lifecycleState.updateState(LifecycleState.STATE_VIEW_DESTROYED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onDestroyView(this, view);
      }
    }

    view = null;
    context = null;
  }

  private void destroy() {
    if (DEBUG) {
      if (!willRetainView) {
        assertNull(view);
      }
    }

    if (view != null) {
      destroyView();
    }

    if (childDirector != null) {
      childDirector.destroy();
      childDirector = null;
    }

    onDestroy();

    lifecycleState.updateState(LifecycleState.STATE_DESTROYED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onDestroy(this);
      }
    }

    stage = null;
  }

  void finish(boolean willRecreate) {
    this.willDestroy = true;
    this.willRecreate = willRecreate;

    if (childDirector != null) {
      childDirector.finish(willRecreate);
    }

    if (!lifecycleState.isViewAttached()) {
      // No need to wait view detach, call destroy() now
      destroy();
    }
  }

  void detachView(@NonNull ViewGroup container) {
    detachView(container, false);
  }

  void detachView(@NonNull ViewGroup container, boolean forceDestroyView) {
    if (DEBUG) {
      assertNotNull(view);
    }

    // If retaining view, no need to recreate view before saveViewState() called,
    // no need to restore view state, no need to save view state
    if (!willRetainView) {
      saveViewState(view);
    }

    if (view.getParent() != container) {
      throw new IllegalStateException("Don't detach view by yourself");
    }
    container.removeView(view);

    onDetachView(view);

    lifecycleState.updateState(LifecycleState.STATE_VIEW_DETACHED);

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onDetachView(this, view);
      }
    }

    if (!willRetainView || forceDestroyView) {
      destroyView();
    }

    if (willDestroy) {
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

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onSaveViewState(this, viewState);
      }
    }
  }

  private void restoreViewState(@NonNull View view) {
    if (viewState != null) {
      view.restoreHierarchyState(viewState.getSparseParcelableArray(KEY_VIEW_STATE_HIERARCHY));

      Bundle savedViewState = viewState.getBundle(KEY_VIEW_STATE_BUNDLE);
      if (savedViewState != null) {
        savedViewState.setClassLoader(getClass().getClassLoader());
        onRestoreViewState(view, savedViewState);
      }

      if (!lifecycleListeners.isEmpty()) {
        for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
          listener.onRestoreViewState(this, viewState);
        }
      }
    }
  }

  Bundle saveInstanceState() {
    Bundle outState = new Bundle();
    outState.putInt(KEY_ID, getId());
    outState.putString(KEY_TAG, getTag());
    outState.putBundle(KEY_ARGS, getArgs());
    outState.putBoolean(KEY_WILL_RETAIN_VIEW, willRetainView());
    outState.putInt(KEY_OPACITY, getOpacity());
    outState.putInt(KEY_THEME, getTheme());
    outState.putInt(KEY_TARGET, target);

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

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onSaveInstanceState(this, outState);
      }
    }

    return outState;
  }

  void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    setSavedId(savedInstanceState.getInt(KEY_ID, INVALID_ID));
    setTag(savedInstanceState.getString(KEY_TAG, null));
    setArgs(savedInstanceState.getBundle(KEY_ARGS));
    setWillRetainView(savedInstanceState.getBoolean(KEY_WILL_RETAIN_VIEW));
    //noinspection WrongConstant
    setOpacity(savedInstanceState.getInt(KEY_OPACITY));
    setTheme(savedInstanceState.getInt(KEY_THEME));
    target = savedInstanceState.getInt(KEY_TARGET, INVALID_ID);

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

    if (!lifecycleListeners.isEmpty()) {
      for (LifecycleListener listener : new ArrayList<>(lifecycleListeners)) {
        listener.onRestoreInstanceState(this, savedInstanceState);
      }
    }
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

  public static abstract class LifecycleListener {

    public void onCreate(@NonNull Scene scene) {}
    public void onCreateView(@NonNull Scene scene) {}
    public void onAttachView(@NonNull Scene scene, @NonNull View view) {}
    public void onStart(@NonNull Scene scene) {}
    public void onResume(@NonNull Scene scene) {}
    public void onPause(@NonNull Scene scene) {}
    public void onStop(@NonNull Scene scene) {}
    public void onDetachView(@NonNull Scene scene, @NonNull View view) {}
    public void onDestroyView(@NonNull Scene scene, @NonNull View view) {}
    public void onDestroy(@NonNull Scene scene) {}

    public void onSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState) { }
    public void onRestoreInstanceState(@NonNull Scene scene, @NonNull Bundle savedInstanceState) { }

    public void onSaveViewState(@NonNull Scene scene, @NonNull Bundle outState) { }
    public void onRestoreViewState(@NonNull Scene scene, @NonNull Bundle savedViewState) { }
  }

  public static class LifecycleState {

    @IntDef({
        STATE_NONE,
        STATE_CREATED,
        STATE_VIEW_CREATED,
        STATE_VIEW_ATTACHED,
        STATE_STARTED,
        STATE_RESUMED,
        STATE_PAUSED,
        STATE_STOPPED,
        STATE_VIEW_DETACHED,
        STATE_VIEW_DESTROYED,
        STATE_DESTROYED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    private static final int STATE_NONE = 0;
    private static final int STATE_CREATED = 1;
    private static final int STATE_VIEW_CREATED = 2;
    private static final int STATE_VIEW_ATTACHED = 3;
    private static final int STATE_STARTED = 4;
    private static final int STATE_RESUMED = 5;
    private static final int STATE_PAUSED = 6;
    private static final int STATE_STOPPED = 7;
    private static final int STATE_VIEW_DETACHED = 8;
    private static final int STATE_VIEW_DESTROYED = 9;
    private static final int STATE_DESTROYED = 10;

    @State
    private int state = STATE_NONE;

    private void assertState(@State int state) {
      if (this.state != state) {
        throw new IllegalStateException("State should be " + state + ", but it's " + this.state);
      }
    }

    private void assertState(@State int state1, @State int state2) {
      if (this.state != state1 && this.state != state2) {
        throw new IllegalStateException("State should be " + state1 + " or " + state2
            + ", but it's " + this.state);
      }
    }

    private void updateState(@State int state) {
      switch (state) {
        case STATE_CREATED:
          assertState(STATE_NONE);
          break;
        case STATE_VIEW_CREATED:
          assertState(STATE_CREATED, STATE_VIEW_DESTROYED);
          break;
        case STATE_VIEW_ATTACHED:
          assertState(STATE_VIEW_CREATED, STATE_VIEW_DETACHED);
          break;
        case STATE_STARTED:
          assertState(STATE_VIEW_ATTACHED, STATE_STOPPED);
          break;
        case STATE_RESUMED:
          assertState(STATE_STARTED, STATE_PAUSED);
          break;
        case STATE_PAUSED:
          assertState(STATE_RESUMED);
          break;
        case STATE_STOPPED:
          assertState(STATE_STARTED, STATE_PAUSED);
          break;
        case STATE_VIEW_DETACHED:
          assertState(STATE_VIEW_ATTACHED, STATE_STOPPED);
          break;
        case STATE_VIEW_DESTROYED:
          assertState(STATE_VIEW_CREATED, STATE_VIEW_DETACHED);
          break;
        case STATE_DESTROYED:
          assertState(STATE_CREATED, STATE_VIEW_DESTROYED);
          break;
        default:
          throw new IllegalStateException("Can't change state to " + state);
      }
      this.state = state;
    }

    /**
     * Returns {@code true} if the scene has been created.
     */
    public boolean hasCreated() {
      return state >= STATE_CREATED;
    }

    /**
     * Returns {@code true} if the scene has been destroyed.
     */
    public boolean hasDestroyed() {
      return state >= STATE_DESTROYED;
    }

    /**
     * Returns {@code true} if the scene is created.
     */
    public boolean isCreated() {
      return state >= STATE_CREATED && state < STATE_DESTROYED;
    }

    /**
     * Returns {@code true} if the view is created.
     */
    public boolean isViewCreated() {
      return state >= STATE_VIEW_CREATED && state < STATE_VIEW_DESTROYED;
    }

    /**
     * Returns {@code true} if the view is attached to parent.
     */
    public boolean isViewAttached() {
      return state >= STATE_VIEW_ATTACHED && state < STATE_VIEW_DETACHED;
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
      return state >= STATE_RESUMED && state < STATE_PAUSED;
    }
  }
}
