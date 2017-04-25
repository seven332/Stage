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
import android.app.Application;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.view.ViewGroup;

public class LifecycleHandler extends Fragment implements Application.ActivityLifecycleCallbacks {

  private static final boolean DEBUG = BuildConfig.DEBUG;

  private static final String FRAGMENT_TAG = "LifecycleHandler";

  private static final String KEY_STAGE_STATE_PREFIX = "LifecycleHandler:stage_state:";

  private Activity activity;
  private boolean hasRegisteredCallbacks;

  private boolean isStarted;
  private boolean isResumed;

  private final SparseArray<ActivityStage> stageMap = new SparseArray<>();

  @NonNull
  static LifecycleHandler install(@NonNull Activity activity) {
    LifecycleHandler lifecycleHandler =
        (LifecycleHandler) activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);

    if (lifecycleHandler == null) {
      lifecycleHandler = new LifecycleHandler();
      activity.getFragmentManager().beginTransaction().add(lifecycleHandler, FRAGMENT_TAG).commit();
    }

    lifecycleHandler.registerActivityListener(activity);
    return lifecycleHandler;
  }

  public LifecycleHandler() {
    setRetainInstance(true);
    setHasOptionsMenu(true);
  }

  private void registerActivityListener(@NonNull Activity activity) {
    if (this.activity != activity) {
      this.activity = activity;

      if (!hasRegisteredCallbacks) {
        hasRegisteredCallbacks = true;
        activity.getApplication().registerActivityLifecycleCallbacks(this);
      }
    }
  }

  @NonNull
  public Stage getStage(@NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        stage.onActivityStarted();
      }
      if (isResumed) {
        stage.onActivityResumed();
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
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    if (activity != null && this.activity == activity) {
      // TODO
    }
  }

  @Override
  public void onActivityStarted(Activity activity) {
    if (activity != null && this.activity == activity) {
      if (DEBUG) {
        assertFalse(isStarted);
        assertFalse(isResumed);
      }

      isStarted = true;

      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.onActivityStarted();
      }
    }
  }

  @Override
  public void onActivityResumed(Activity activity) {
    if (activity != null && this.activity == activity) {
      if (DEBUG) {
        assertTrue(isStarted);
        assertFalse(isResumed);
      }

      isResumed = true;

      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.onActivityResumed();
      }
    }
  }

  @Override
  public void onActivityPaused(Activity activity) {
    if (activity != null && this.activity == activity) {
      if (DEBUG) {
        assertTrue(isStarted);
        assertTrue(isResumed);
      }

      isResumed = false;

      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.onActivityPaused();
      }
    }
  }

  @Override
  public void onActivityStopped(Activity activity) {
    if (activity != null && this.activity == activity) {
      if (DEBUG) {
        assertTrue(isStarted);
        assertFalse(isResumed);
      }

      isStarted = false;

      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.onActivityStopped();
      }
    }
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    if (activity != null && this.activity == activity) {
      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        Bundle bundle = new Bundle();
        stage.saveInstanceState(bundle);
        outState.putBundle(getStageStateKey(stage.hashKey), bundle);
      }
    }
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
    if (activity != null && this.activity == activity) {
      if (DEBUG) {
        assertFalse(isStarted);
        assertFalse(isResumed);
      }

      boolean isFinishing = activity.isFinishing();
      for (int i = 0, n = stageMap.size(); i < n; ++i) {
        ActivityStage stage = stageMap.valueAt(i);
        stage.onActivityDestroyed(isFinishing);
      }

      // TODO unregister activity listener if isFinishing == true
    }
  }
}
