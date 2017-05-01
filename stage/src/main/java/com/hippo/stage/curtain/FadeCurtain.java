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

package com.hippo.stage.curtain;

/*
 * Created by Hippo on 4/27/2017.
 */

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import com.hippo.stage.SceneInfo;
import java.util.List;

public class FadeCurtain extends AnimatorCurtain {

  @Nullable
  @Override
  protected Animator getAnimator(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    AnimatorSet set = new AnimatorSet();

    Animator animator = getAnimator(upper);
    if (animator != null) {
      set.play(animator);
    }

    for (SceneInfo info : lower) {
      animator = getAnimator(info);
      if (animator != null) {
        set.play(animator);
      }
    }

    return set;
  }

  @Nullable
  private Animator getAnimator(SceneInfo info) {
    if (info.viewState == SceneInfo.NONE) {
      return null;
    } else {
      float start, end;
      if (info.viewState == SceneInfo.NEWLY_ATTACHED) {
        start = 0.0f;
        end = 1.0f;
      } else {
        start = 1.0f;
        end = 0.0f;
      }
      return ObjectAnimator.ofFloat(info.view, View.ALPHA, start, end);
    }
  }

  @Override
  protected void restore(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    upper.view.setAlpha(1.0f);
    for (SceneInfo info : lower) {
      info.view.setAlpha(1.0f);
    }
  }
}
