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
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.ViewGroup;

/**
 * Don't use it! It's a {@code Fragment}, so it's public.
 * <p>
 * A {@code Director} stores data cross {@link Activity} during recreating
 * and handle lifecycle of {@link Stage}.
 */
public class Director extends Fragment {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  private static final String FRAGMENT_TAG = "Director";

  private static final String KEY_STAGE_STATE_PREFIX = "Director:stage_state:";

  private boolean isStarted;
  private boolean isResumed;
  private boolean isDestroy;

  private final SparseArray<ActivityStage> stageMap = new SparseArray<>();

  private final ActivityCallbacks activityCallbacks =
      new ActivityCallbacks() {
        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
          if (activity != null && activity == getActivity()) {
            saveStageState(outState);
          }
        }
      };

  @NonNull
  static Director install(@NonNull Activity activity) {
    Director director = (Director) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

    if (director == null) {
      director = new Director();
      activity.getFragmentManager().beginTransaction().add(director, FRAGMENT_TAG).commit();
    }

    return director;
  }

  public Director() {
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  @NonNull
  Stage getStage(@NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
    if (isDestroy) {
      throw new IllegalStateException("Can't call getStage() on a destroyed Director");
    }

    int stageHashKey = getStageHashKey(container);
    ActivityStage stage = stageMap.get(stageHashKey);
    if (stage == null) {
      stage = new ActivityStage(stageHashKey);

      // Restore
      if (savedInstanceState != null) {
        Bundle stageState = savedInstanceState.getBundle(getStageStateKey(stageHashKey));
        if (stageState != null) {
          stage.restoreInstanceState(stageState);
        }
      }

      // Restore activity lifecycle
      if (isStarted) {
        stage.start();
      }
      if (isResumed) {
        stage.resume();
      }

      // setContainer() handles view re-attaching, so call it after restoring state
      stage.setContainer(container);

      stageMap.put(stageHashKey, stage);
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

  private static int getStageHashKey(@NonNull ViewGroup viewGroup) {
    return viewGroup.getId();
  }

  private static String getStageStateKey(int hashKey) {
    return KEY_STAGE_STATE_PREFIX + hashKey;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getActivity().getApplication().registerActivityLifecycleCallbacks(activityCallbacks);
  }

  @Override
  public void onStart() {
    super.onStart();

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

  @Override
  public void onResume() {
    super.onResume();

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

  @Override
  public void onPause() {
    super.onPause();

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

  @Override
  public void onStop() {
    super.onStop();

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

  @Override
  public void onDetach() {
    super.onDetach();

    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
    }

    // onDestroy() is called before onDetach()
    // Check it to avoid detach stage twice
    if (!isDestroy) {
      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.detach();
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if (DEBUG) {
      assertFalse(isStarted);
      assertFalse(isResumed);
      assertFalse(isDestroy);
    }

    isDestroy = true;

    getActivity().getApplication().unregisterActivityLifecycleCallbacks(activityCallbacks);

    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      stage.detach();
      stage.destroy();
    }
    stageMap.clear();
  }

  private void saveStageState(Bundle outState) {
    for (int i = 0, n = stageMap.size(); i < n; ++i) {
      ActivityStage stage = stageMap.valueAt(i);
      Bundle bundle = new Bundle();
      stage.saveInstanceState(bundle);
      outState.putBundle(getStageStateKey(stage.hashKey), bundle);
    }
  }
}
