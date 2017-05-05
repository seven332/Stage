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

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
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

  private Scene host;
  private boolean retainStage;

  /**
   * Creates a new StagePagerAdapter using the passed host.
   *
   * @param retainStage whether retain {@link Stage} when page detached
   */
  public StagePagerAdapter(@NonNull Scene host, boolean retainStage) {
    this.host = host;
    this.retainStage = retainStage;
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

    boolean needBinding = !director.contains(position);
    Stage stage = director.direct(container, position);
    if (needBinding) {
      bindStage(stage, position);
    }

    return stage;
  }

  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    Stage stage = (Stage) object;

    if (retainStage) {
      // Stage should be retained
      stage.suspend();
    } else {
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
}
