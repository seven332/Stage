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

package com.hippo.stage.pager;

/*
 * Created by Hippo on 5/4/2017.
 */

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.Director;
import com.hippo.stage.Scene;
import com.hippo.stage.Stage;

/**
 * An adapter for {@link android.support.v4.view.ViewPager} that uses Routers as pages.
 * {@link StagePager} is strongly recommended.
 */
public abstract class StagePagerAdapter extends PagerAdapter {

  private static final String KEY_SAVED_STATE_MAP = "StagePagerAdapter:saved_state_map";

  private Scene host;
  @Nullable
  private SparseArray<Bundle> savedStateMap;

  /**
   * Creates a new StagePagerAdapter using the passed host.
   *
   * @param retainStage whether retain {@link Stage} when page detached, or save their states.
   *                    If it is {@code true}, this class works like {@code FragmentPagerAdapter}.
   *                    If it is {@code false}, this class works like
   *                    {@code FragmentStatePagerAdapter}.
   */
  public StagePagerAdapter(@NonNull Scene host, boolean retainStage) {
    this.host = host;
    if (!retainStage) {
      savedStateMap = new SparseArray<>();
    }
  }

  Scene getHost() {
    return host;
  }

  /**
   * Binds a {@link Stage}.
   */
  public abstract void bindStage(@NonNull Stage stage, int position);

  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    Director director = host.hireChildDirector();

    if (director.contains(position)) {
      // Contains the stage, just return it
      return director.direct(container, position);
    }

    // Try to restore saved state
    Stage stage = null;
    if (savedStateMap != null) {
      Bundle savedState = savedStateMap.get(position);
      if (savedState != null) {
        savedStateMap.remove(position);
        stage = director.direct(container, savedState);
      }
    }

    // Create a new Stage
    if (stage == null) {
      stage = director.direct(container, position);
      bindStage(stage, position);
    }

    return stage;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    Stage stage = (Stage) object;

    if (savedStateMap == null) {
      // Stage should be retained
      stage.suspend();
    } else {
      // Save state
      Bundle savedState = new Bundle();
      stage.saveInstanceState(savedState);
      savedStateMap.put(position, savedState);
      // Close Stage
      stage.close();
    }
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    Stage stage = (Stage) object;

    for (Scene scene : stage) {
      if (scene.getView() == view) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Parcelable saveState() {
    if (savedStateMap != null) {
      Bundle state = new Bundle();
      state.putSparseParcelableArray(KEY_SAVED_STATE_MAP, savedStateMap);
      return state;
    } else {
      return null;
    }
  }

  @Override
  public void restoreState(Parcelable state, ClassLoader loader) {
    if (this.savedStateMap != null) {
      Bundle bundle = (Bundle) state;
      SparseArray<Bundle> savedStateMap = bundle.getSparseParcelableArray(KEY_SAVED_STATE_MAP);
      if (savedStateMap != null) {
        this.savedStateMap = savedStateMap;
      }
    }
  }
}
