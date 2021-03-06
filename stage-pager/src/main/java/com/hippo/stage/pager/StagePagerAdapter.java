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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.Director;
import com.hippo.stage.Scene;
import com.hippo.stage.Stage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An adapter for {@link android.support.v4.view.ViewPager} that uses Routers as pages.
 * {@link StagePager} is strongly recommended.
 */
public abstract class StagePagerAdapter extends PagerAdapter {

  private static final String KEY_SAVED_STATE_MAP = "StagePagerAdapter:saved_state_map";

  /**
   * Nothing is kept.
   */
  public static final int MODE_NONE = 0;
  /**
   * Like {@code FragmentStatePagerSupport}, all stage states are kept.
   */
  public static final int MODE_SAVE = 1;
  /**
   * Like {@code FragmentPagerAdapter}, all stages are kept.
   */
  public static final int MODE_RETAIN = 2;

  @IntDef({MODE_NONE, MODE_SAVE, MODE_RETAIN})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Mode {}

  private Scene host;
  @Mode
  private int mode;
  @Nullable
  private SparseArray<Bundle> savedStateMap;

  /**
   * Creates a new StagePagerAdapter using the passed host.
   *
   * @param mode Describes how to handle stage lifecycle.
   */
  public StagePagerAdapter(@NonNull Scene host, @Mode int mode) {
    this.host = host;
    this.mode = mode;
    if (mode == MODE_SAVE) {
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

  /**
   * Restore the stage. Only called if the mode is {@link #MODE_SAVE}.
   * Sometimes it's not enough to restore stage with bundle only.
   * This method is always used to make up it.
   */
  public abstract void restoreStage(@NonNull Stage stage, int position);

  /**
   * Return the stable ID for the stage at position.
   * In {@link #MODE_NONE}, id must be unique for each attached stage.
   * In {@link #MODE_SAVE} and {@link #MODE_RETAIN}, id must be unique for any stage.
   */
  public abstract int getStageId(int position);

  @NonNull
  @Override
  public Object instantiateItem(@NonNull ViewGroup container, int position) {
    Director director = host.hireChildDirector();

    int id = getStageId(position);

    if (director.contains(id)) {
      // Contains the stage, just return it
      return director.direct(container, id);
    }

    // Try to restore saved state
    Stage stage = null;
    if (savedStateMap != null) {
      Bundle savedState = savedStateMap.get(id);
      if (savedState != null) {
        savedStateMap.remove(id);
        stage = director.direct(container, savedState);
        if (stage != null) {
          restoreStage(stage, position);
        }
      }
    }

    // Create a new Stage
    if (stage == null) {
      stage = director.direct(container, id);
      bindStage(stage, position);
    }

    return stage;
  }

  @Override
  public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
    Stage stage = (Stage) object;

    if (savedStateMap == null) {
      if (mode == MODE_RETAIN) {
        stage.suspend();
      } else {
        stage.close();
      }
    } else {
      // Save state
      Bundle savedState = new Bundle();
      stage.saveInstanceState(savedState);
      int id = getStageId(position);
      savedStateMap.put(id, savedState);
      // Close Stage
      stage.close();
    }
  }

  @Override
  public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
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
