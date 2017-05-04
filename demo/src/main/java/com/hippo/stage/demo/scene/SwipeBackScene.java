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

package com.hippo.stage.demo.scene;

/*
 * Created by Hippo on 5/3/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.Curtain;
import com.hippo.stage.SceneInfo;
import com.hippo.stage.curtain.NoOpCurtain;
import com.hippo.stage.curtain.ShiftCurtain;
import com.hippo.stage.demo.R;
import com.hippo.swipeback.SwipeBackLayout;
import java.util.List;

public abstract class SwipeBackScene extends RefWatcherScene {

  private SwipeBackLayout swipeBackLayout;

  @Override
  protected void onCreate(@Nullable Bundle args) {
    super.onCreate(args);
    setOpacity(TRANSLUCENT);
  }

  @NonNull
  @Override
  protected final View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.scene_swipe_back, container, false);
    swipeBackLayout = (SwipeBackLayout) view.findViewById(R.id.swipe_back);
    swipeBackLayout.setSwipeEdge(SwipeBackLayout.EDGE_LEFT);
    swipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
      @Override
      public void onSwipe(float percent) {}
      @Override
      public void onStateChange(int edge, int state) {}
      @Override
      public void onSwipeOverThreshold() {}
      @Override
      public void onFinish() {
        pop();
      }
    });

    View content = onCreateContent(inflater, swipeBackLayout);
    swipeBackLayout.addView(content);

    return view;
  }

  @NonNull
  protected abstract View onCreateContent(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container);

  @Override
  protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    swipeBackLayout = null;
  }

  @Nullable
  @Override
  protected Curtain onCreateCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    if (isFinished()) {
      return NoOpCurtain.INSTANCE;
    } else {
      ShiftCurtain curtain = new ShiftCurtain();
      curtain.setDuration(150L);
      curtain.setInterpolator(new FastOutSlowInInterpolator());
      return curtain;
    }
  }

  private boolean isFinished() {
    View view = getView();
    if (view != null) {
      return ((SwipeBackLayout) view.findViewById(R.id.swipe_back)).isFinished();
    } else {
      return false;
    }
  }
}
