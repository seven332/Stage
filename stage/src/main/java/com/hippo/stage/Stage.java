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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@code Stage} is where {@link Scene}s performed.
 * {@code Stage} objects handle attaching and detaching views of {@link Scene}s.
 */
public abstract class Stage {

  private static final String LOG_TAG = Scene.class.getSimpleName();

  private static final boolean DEBUG = BuildConfig.DEBUG;

  private static final String KEY_STACK = "Stage:stack";

  private ViewGroup container;
  private CurtainSuppler curtainSuppler;
  private Curtain runningCurtain;
  private SceneStack stack = new SceneStack(new SceneStack.Callback() {
    @Override
    public void onPush(@NonNull Scene scene) {
      onPushScene(scene);
    }
    @Override
    public void onPop(@NonNull Scene scene) {
      onPopScene(scene);
    }
  });

  private boolean isStarted;
  private boolean isResumed;

  /**
   * Register a {@link Activity} to make it available for installing {@code Stage}.
   * This is useful when no {@code Stage} need to be created at beginning.
   *
   * @see #install(Activity, ViewGroup, Bundle)
   */
  public static void register(@NonNull Activity activity) {
    LifecycleHandler.install(activity);
  }

  /**
   * Install a {@code Stage} to a {@link Activity}.
   * <p>
   * At least, this method or {@link #register(Activity)} must be called once
   * before {@link Activity#onResume()} called,
   * to ensure activity lifecycle record work fine.
   * <p>
   * Multiple {@code Stage}s can be installed to the same {@link Activity}.
   * Use different container view for each {@code Stage}.
   * Set different ID for each container view.
   *
   * @see #register(Activity)
   */
  @NonNull
  public static Stage install(
      @NonNull Activity activity, @NonNull ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    LifecycleHandler lifecycleHandler = LifecycleHandler.install(activity);
    return lifecycleHandler.getStage(container, savedInstanceState);
  }

  /**
   * Pops the top {@link Scene}.
   * It's a no-op if scene stack is empty.
   */
  public void popTopScene() {
    Scene scene = stack.peek();
    if (scene != null) {
      popScene(scene);
    } else {
      Log.w(LOG_TAG, "Can't pop an empty Stage");
    }
  }

  /**
   * Pop a {@link Scene}.
   * It's a no-op if scene isn't in the stack.
   */
  public void popScene(@NonNull Scene scene) {
    completeRunningCurtain();

    // Only need sync views if the scene is attached
    boolean needSyncViews = scene.isViewAttached();

    int index = stack.pop(scene);
    if (index == SceneStack.INVALID_INDEX) {
      Log.w(LOG_TAG, "Can't pop an Scene which isn't performed on this Stage");
      return;
    }

    if (needSyncViews) {
      // Calculate the visible scenes below popped scene
      List<Scene> newScenes = getVisibleScenes();

      if (index == 0 && isResumed) {
        scene.pause();
      }
      SceneInfo upperInfo = new SceneInfo.Builder()
          .scene(scene)
          .newlyAttached(false)
          .willBeDetached(true)
          .isStarted(isStarted)
          .build();
      List<SceneInfo> upper = Collections.singletonList(upperInfo);

      int opacity = scene.opacity();
      boolean newlyAttached = opacity == Scene.OPAQUE || (opacity == Scene.TRANSLUCENT && index != 0);
      boolean isTop = index == 0;
      List<Scene> lowerScenes = newScenes.subList(index, newScenes.size());
      List<SceneInfo> lower = new ArrayList<>(lowerScenes.size());
      for (Scene lowerScene : lowerScenes) {

        if (newlyAttached) {
          lowerScene.attachView(container, 0);
          if (isStarted) {
            lowerScene.start();
          }
        }
        // Only top scene can be resumed
        if (isResumed && isTop) {
          lowerScene.resume();
        }

        SceneInfo lowerInfo = new SceneInfo.Builder()
            .scene(lowerScene)
            .newlyAttached(newlyAttached)
            .willBeDetached(false)
            .isStarted(isStarted)
            .build();
        lower.add(lowerInfo);

        if (isTop && lowerScene.opacity() == Scene.TRANSLUCENT) {
          // An translucent scene become top now, the following scene must be newly attached
          newlyAttached = true;
        }

        // Not top anymore
        isTop = false;
      }

      changeScenes(upper, lower);
    }
  }

  /**
   * Push a {@link Scene} to the top of the stack.
   */
  public void pushScene(@NonNull Scene scene) {
    completeRunningCurtain();

    ArrayList<Scene> oldScenes = getVisibleScenes();
    stack.push(scene);
    ArrayList<Scene> newScenes = getVisibleScenes();

    scene.attachView(container);
    if (isStarted) {
      scene.start();
    }
    // It's top
    if (isResumed) {
      scene.resume();
    }
    SceneInfo upperInfo = new SceneInfo.Builder()
        .scene(scene)
        .newlyAttached(true)
        .willBeDetached(false)
        .isStarted(isStarted)
        .build();
    List<SceneInfo> upper = Collections.singletonList(upperInfo);

    List<SceneInfo> lower = new ArrayList<>(oldScenes.size());
    // Some Scenes in bottom might be not visible
    // start from (newScenes.size() - 1) in oldScenes
    int detachIndex = newScenes.size() - 1;
    for (int i = 0, n = oldScenes.size(); i < n; ++i) {
      Scene lowerScene = oldScenes.get(i);

      // Pause the original top Scene if activity is resumed
      if (i == 0 && isResumed) {
        lowerScene.pause();
      }

      SceneInfo lowerInfo = new SceneInfo.Builder()
          .scene(lowerScene)
          .newlyAttached(false)
          .willBeDetached(i >= detachIndex)
          .isStarted(isStarted)
          .build();
      lower.add(lowerInfo);
    }

    changeScenes(upper, lower);
  }

  /**
   * Replace the top {@link Scene} with a {@code Scene}.
   * If the stack is empty, just push the {@code Scene}.
   */
  public void replaceTopScene(@NonNull Scene scene) {
    Scene oldTopScene = stack.peek();
    if (oldTopScene == null) {
      // The stack is empty, just push the Scene
      pushScene(scene);
      return;
    }

    completeRunningCurtain();

    ArrayList<Scene> oldScenes = getVisibleScenes();
    stack.pop();
    stack.push(scene);
    ArrayList<Scene> newScenes = getVisibleScenes();

    scene.attachView(container);
    if (isStarted) {
      scene.start();
    }
    // It's top
    if (isResumed) {
      scene.resume();
    }
    SceneInfo upperInfo = new SceneInfo.Builder()
        .scene(scene)
        .newlyAttached(true)
        .willBeDetached(false)
        .isStarted(isStarted)
        .build();
    List<SceneInfo> upper = Collections.singletonList(upperInfo);

    int lowerSize = Math.max(oldScenes.size(), newScenes.size());
    List<SceneInfo> lower = new ArrayList<>(lowerSize);
    // Add old top scenes
    if (isResumed) {
      oldTopScene.pause();
    }
    SceneInfo lowerInfo = new SceneInfo.Builder()
        .scene(oldTopScene)
        .newlyAttached(false)
        .willBeDetached(true)
        .isStarted(isStarted)
        .build();
    lower.add(lowerInfo);
    // Add the same scenes which are in both old visible scenes and new visible scenes
    int sameSize = Math.min(oldScenes.size(), newScenes.size());
    for (int i = 1; i < sameSize; ++i) {
      Scene lowerScene = oldScenes.get(i);
      lowerInfo = new SceneInfo.Builder()
          .scene(lowerScene)
          .newlyAttached(false)
          .willBeDetached(false)
          .isStarted(isStarted)
          .build();
      lower.add(lowerInfo);
    }
    // Add scenes which will be detached
    if (oldScenes.size() > sameSize) {
      for (int i = sameSize; i < oldScenes.size(); ++i) {
        Scene lowerScene = oldScenes.get(i);
        lowerInfo = new SceneInfo.Builder()
            .scene(lowerScene)
            .newlyAttached(false)
            .willBeDetached(true)
            .isStarted(isStarted)
            .build();
        lower.add(lowerInfo);
      }
    }
    // Add scenes which should be attached
    if (newScenes.size() > sameSize) {
      for (int i = sameSize; i < newScenes.size(); ++i) {
        Scene lowerScene = newScenes.get(i);
        // Always attach view to tail
        lowerScene.attachView(container, 0);
        if (isStarted) {
          lowerScene.start();
        }
        lowerInfo = new SceneInfo.Builder()
            .scene(lowerScene)
            .newlyAttached(true)
            .willBeDetached(false)
            .isStarted(isStarted)
            .build();
        lower.add(lowerInfo);
      }
    }

    changeScenes(upper, lower);
  }

  public void setRootScene(@NonNull Scene scene) {
    completeRunningCurtain();

    ArrayList<Scene> oldScenes = getVisibleScenes();
    stack.popAll();
    stack.push(scene);

    scene.attachView(container);
    if (isStarted) {
      scene.start();
    }
    // It's top
    if (isResumed) {
      scene.resume();
    }
    SceneInfo upperInfo = new SceneInfo.Builder()
        .scene(scene)
        .newlyAttached(true)
        .willBeDetached(false)
        .isStarted(isStarted)
        .build();
    List<SceneInfo> upper = Collections.singletonList(upperInfo);

    List<SceneInfo> lower = new ArrayList<>(oldScenes.size());
    boolean isTop = true;
    for (Scene lowerScene : oldScenes) {
      if (isResumed && isTop) {
        lowerScene.pause();
      }

      SceneInfo lowerInfo = new SceneInfo.Builder()
          .scene(lowerScene)
          .newlyAttached(false)
          .willBeDetached(true)
          .isStarted(isStarted)
          .build();
      lower.add(lowerInfo);

      // Not top anymore
      isTop = false;
    }

    changeScenes(upper, lower);
  }

  private void completeRunningCurtain() {
    if (runningCurtain != null) {
      runningCurtain.completeImmediately();

      // Check whether Curtain.OnCompleteListener is called
      if (runningCurtain != null) {
        throw new IllegalStateException("Must call"
            + " OnCompleteListener.onChangeCompleted() in "
            + runningCurtain.getClass().getName() + ".completeImmediately()");
      }
    }
  }

  private void changeScenes(
      @NonNull final List<SceneInfo> upper, @NonNull final List<SceneInfo> lower) {
    Curtain curtain = null;
    if (curtainSuppler != null) {
      curtain = curtainSuppler.getCurtain(upper, lower);
    }
    if (curtain != null) {
      runningCurtain = curtain;
      curtain.execute(upper, lower, new Curtain.OnCompleteListener() {
        @Override
        public void OnComplete() {
          runningCurtain = null;
          detachViews(upper);
          detachViews(lower);
        }
      });
    } else {
      detachViews(upper);
      detachViews(lower);
    }
  }

  private void detachViews(
      @NonNull final List<SceneInfo> infos) {
    for (SceneInfo info : infos) {
      if (info.willBeDetached) {
        Scene scene = info.scene;
        if (info.isStarted) {
          scene.stop();
        }
        scene.detachView(container);
      }
    }
  }

  private void onPushScene(@NonNull Scene scene) {
    scene.create(this);
  }

  private void onPopScene(@NonNull Scene scene) {
    scene.finish();
  }

  @NonNull
  private ArrayList<Scene> getVisibleScenes() {
    ArrayList<Scene> scenes = new ArrayList<>();
    boolean isTop = true;
    for (Scene scene : stack) {
      scenes.add(scene);
      int opacity = scene.opacity();
      if (opacity == Scene.OPAQUE || (opacity == Scene.TRANSLUCENT && !isTop)) {
        // The scenes below can't be seen
        break;
      }
      // It's not top anymore
      isTop = false;
    }
    return scenes;
  }

  boolean hasContainer() {
    return container != null;
  }

  void setContainer(ViewGroup container) {
    this.container = container;
  }

  ViewGroup getContainer() {
    return container;
  }

  void onActivityStarted() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    isStarted = true;

    // All view-attached scene should start
    for (Scene scene : stack) {
      if (scene.isViewAttached()) {
        scene.start();
      }
    }
  }

  void onActivityResumed() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertFalse(isResumed);
    }

    isResumed = true;

    // Only top scene should resume
    Scene scene = getTopScene();
    if (scene != null) {
      scene.resume();
    }
  }

  void onActivityPaused() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertTrue(isResumed);
    }

    isResumed = false;

    // Only top scene should pause
    Scene scene = getTopScene();
    if (scene != null) {
      scene.pause();
    }
  }

  void onActivityStopped() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertFalse(isResumed);
    }

    isStarted = false;

    // All view-attached scene should stop
    for (Scene scene : stack) {
      if (scene.isViewAttached()) {
        scene.stop();
      }
    }
  }

  void onActivityDestroyed(boolean isFinishing) {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    completeRunningCurtain();

    for (Scene scene : getVisibleScenes()) {
      scene.detachView(container, true);
    }

    if (isFinishing) {
      stack.popAll();
    }
  }

  @CallSuper
  void saveInstanceState(@NonNull Bundle outState) {
    Bundle stackState = new Bundle();
    stack.saveInstanceState(stackState);
    outState.putBundle(KEY_STACK, stackState);
  }

  @CallSuper
  void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    Bundle bundle = savedInstanceState.getBundle(KEY_STACK);
    if (bundle != null) {
      // Only push Scenes to stack
      stack.restoreInstanceState(bundle);

      // Add views of visible scenes and handle visible scenes lifecycle
      ArrayList<Scene> visibleScenes = getVisibleScenes();
      int index = visibleScenes.size();
      // From bottom to top
      while (--index >= 0) {
        Scene scene = visibleScenes.get(index);

        // Always attach view to tail
        scene.attachView(container);

        // Handle lifecycle
        if (isStarted) {
          scene.start();
        }
        if (isResumed && index == 0) {
          scene.resume();
        }
      }
    }
  }

  /**
   * Returns top {@link Scene} in stack.
   */
  @Nullable
  public Scene getTopScene() {
    return stack.peek();
  }

  /**
   * Returns root {@link Scene} in stack.
   */
  @Nullable
  public Scene getRootScene() {
    return stack.tail();
  }

  /**
   * Returns the number of {@link Scene}s in {@code Stage}.
   */
  public int getSceneCount() {
    return stack.size();
  }

  /**
   * Sets the curtain suppler for this {@code Stage}.
   */
  public void setCurtainSuppler(CurtainSuppler suppler) {
    this.curtainSuppler = suppler;
  }

  /**
   * A {@code CurtainSuppler} supplies {@link Curtain} for {@link Stage}.
   */
  public interface CurtainSuppler {

    /**
     * Returns a {@link Curtain} for these scenes.
     */
    @Nullable
    Curtain getCurtain(List<SceneInfo> upper, List<SceneInfo> lower);
  }
}
