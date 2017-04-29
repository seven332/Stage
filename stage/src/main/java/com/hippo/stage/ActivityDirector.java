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
 * Created by Hippo on 4/28/2017.
 */

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.ViewGroup;
import java.util.ArrayList;

class ActivityDirector implements InternalDirector {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  private static final String FRAGMENT_TAG = "ActivityDirector";

  private static final String KEY_CURRENT_SCENE_ID = "ActivityDirector:current_scene_id";
  private static final String KEY_STAGE_STATES = "ActivityDirector:stage_states";

  private boolean isStarted;
  private boolean isResumed;
  private boolean isDestroy;

  private int currentSceneId = Scene.INVALID_ID;

  private Activity activity;

  private final SparseArray<ActivityStage> stageMap = new SparseArray<>();

  static ActivityDirector getInstance(
      @NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    // Get DataFragment
    DataFragment fragment =
        (DataFragment) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    if (fragment == null) {
      fragment = new DataFragment();
      activity.getFragmentManager().beginTransaction().add(fragment, FRAGMENT_TAG).commit();
    }

    // Get director
    ActivityDirector director = fragment.getDirector();
    if (director == null) {
      director = new ActivityDirector();
      director.setActivity(activity);
      if (savedInstanceState != null) {
        director.restoreInstanceState(savedInstanceState);
      }
      fragment.setDirector(director);
    } else {
      director.setActivity(activity);
    }

    return director;
  }

  private void setActivity(@Nullable Activity activity) {
    if (this.activity == null) {
      this.activity = activity;
    } else if (this.activity != activity) {
      throw new IllegalStateException("Two different activity for one ActivityDirector. "
          + "Maybe the library developer forgot to release old activity reference.");
    }
  }

  @Override
  public Activity getActivity() {
    return activity;
  }

  @NonNull
  @Override
  public Stage direct(@NonNull ViewGroup container) {
    if (isDestroy) {
      throw new IllegalStateException("Can't call direct() on a destroyed Director");
    }

    int id = container.getId();
    ActivityStage stage = stageMap.get(id);
    if (stage == null) {
      stage = new ActivityStage(this);
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

  @Override
  public int requireSceneId() {
    int id;
    do {
      id = ++currentSceneId;
    } while (id == Scene.INVALID_ID);
    return id;
  }

  private void start() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    isStarted = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      stage.start();
    }
  }

  private void resume() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertFalse(isResumed);
    }

    isResumed = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      stage.resume();
    }
  }

  private void pause() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertTrue(isResumed);
    }

    isResumed = false;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      stage.pause();
    }
  }

  private void stop() {
    if (DEBUG) {
      assertTrue(isStarted);
      assertFalse(isResumed);
    }

    isStarted = false;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      stage.stop();
    }
  }

  public void destroy() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
      assertFalse(isDestroy);
    }

    isDestroy = true;

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      stage.detach();
      stage.destroy();
    }
    stageMap.clear();

    // The activity will be destroyed soon
    activity = null;
  }

  private void detach() {
    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    // destroy() is called before detach()
    // Check it to avoid detach stage twice
    if (!isDestroy) {
      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.detach();
      }

      // The activity will be destroyed soon
      activity = null;
    }
  }

  private void saveInstanceState(Bundle outState) {
    outState.putInt(KEY_CURRENT_SCENE_ID, currentSceneId);

    ArrayList<Bundle> stageStates = new ArrayList<>(stageMap.size());
    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      Bundle bundle = new Bundle();
      stage.saveInstanceState(bundle);
      stageStates.add(bundle);
    }
    outState.putParcelableArrayList(KEY_STAGE_STATES, stageStates);
  }

  private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
    currentSceneId = savedInstanceState.getInt(KEY_CURRENT_SCENE_ID, Scene.INVALID_ID);

    ArrayList<Bundle> stageStates = savedInstanceState.getParcelableArrayList(KEY_STAGE_STATES);
    if (stageStates != null) {
      for (Bundle stageState : stageStates) {
        ActivityStage stage = new ActivityStage(this);
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

  public static class DataFragment extends Fragment {

    private boolean isStarted;
    private boolean isResumed;
    private boolean isDestroy;

    @Nullable
    private ActivityDirector director;

    private final ActivityCallbacks activityCallbacks = new ActivityCallbacks() {
      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (activity != null && activity == getActivity()) {
          saveInstanceState(outState);
        }
      }
    };

    public DataFragment() {
      setRetainInstance(true);
      setHasOptionsMenu(true);
    }

    private void setDirector(@NonNull ActivityDirector director) {
      if (this.director == null) {
        this.director = director;

        if (isStarted) {
          director.start();
        }
        if (isResumed) {
          director.resume();
        }
      } else {
        throw new IllegalStateException("Don't hire two Director for one Activity");
      }
    }

    @Nullable
    private ActivityDirector getDirector() {
      return director;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getActivity().getApplication().registerActivityLifecycleCallbacks(activityCallbacks);
    }

    @Override
    public void onStart() {
      super.onStart();
      isStarted = true;

      if (director != null) {
        director.start();
      }
    }

    @Override
    public void onResume() {
      super.onResume();
      isResumed = true;

      if (director != null) {
        director.resume();
      }
    }

    @Override
    public void onPause() {
      super.onPause();
      isResumed = false;

      if (director != null) {
        director.pause();
      }
    }

    @Override
    public void onStop() {
      super.onStop();
      isStarted = false;

      if (director != null) {
        director.stop();
      }
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      isDestroy = true;

      if (director != null) {
        director.destroy();
      }
    }

    @Override
    public void onDetach() {
      super.onDetach();

      if (director != null) {
        director.detach();
      }

      if (isDestroy) {
        // onDestroy() is called before onDetach(), clear data here
        getActivity().getApplication().unregisterActivityLifecycleCallbacks(activityCallbacks);
        director = null;
      }
    }

    private void saveInstanceState(Bundle outState) {
      if (director != null) {
        director.saveInstanceState(outState);
      }
    }
  }
}
