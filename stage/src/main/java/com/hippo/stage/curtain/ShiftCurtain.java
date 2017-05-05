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
 * Created by Hippo on 5/1/2017.
 */

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.SceneInfo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class ShiftCurtain extends AnimatorCurtain {

  @IntDef({LEFT, TOP, RIGHT, BOTTOM})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Direction {}

  public static final int LEFT = 0;
  public static final int TOP = 1;
  public static final int RIGHT = 2;
  public static final int BOTTOM = 3;

  @Direction
  private int direction = RIGHT;

  /**
   * Creates a {@code ShiftCurtain} with right {@code direction}.
   */
  public ShiftCurtain() {
    this(RIGHT);
  }

  /**
   * Creates a {@code ShiftCurtain} with a {@code direction}.
   */
  public ShiftCurtain(@Direction int direction) {
    setDirection(direction);
  }

  /**
   * Sets direction for new pushed scene.
   */
  public void setDirection(@Direction int direction) {
    this.direction = direction;
  }

  @Nullable
  @Override
  protected Animator getAnimator(@NonNull ViewGroup container, @NonNull SceneInfo upper,
      @NonNull List<SceneInfo> lower) {
    AnimatorSet set = new AnimatorSet();

    Animator animator = getAnimator(container, upper, true);
    if (animator != null) {
      set.play(animator);
    }

    for (SceneInfo info : lower) {
      animator = getAnimator(container, info, false);
      if (animator != null) {
        set.play(animator);
      }
    }

    return set;
  }

  @Direction
  private int reverseDirection(@Direction int direction) {
    switch (direction) {
      case LEFT:
        return RIGHT;
      case TOP:
        return BOTTOM;
      default:
      case RIGHT:
        return LEFT;
      case BOTTOM:
        return TOP;
    }
  }

  @Nullable
  private Animator getAnimator(ViewGroup container, SceneInfo info, boolean isUpper) {
    if (info.viewState == SceneInfo.NONE) {
      return null;
    } else {
      boolean isFrom = info.viewState == SceneInfo.NEWLY_ATTACHED;
      int direction = isUpper ? this.direction : reverseDirection(this.direction);

      switch (direction) {
        case LEFT:
          if (isFrom) {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_X, -container.getWidth(), 0);
          } else {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_X, 0, -container.getWidth());
          }
        case TOP:
          if (isFrom) {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_Y, -container.getHeight(), 0);
          } else {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_Y, 0, -container.getHeight());
          }
        default:
        case RIGHT:
          if (isFrom) {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_X, container.getWidth(), 0);
          } else {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_X, 0, container.getWidth());
          }
        case BOTTOM:
          if (isFrom) {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_Y, container.getHeight(), 0);
          } else {
            return ObjectAnimator.ofFloat(info.view, View.TRANSLATION_Y, 0, container.getHeight());
          }
      }
    }
  }

  @Override
  protected void restore(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    upper.view.setTranslationX(0.0f);
    upper.view.setTranslationY(0.0f);
    for (SceneInfo info : lower) {
      info.view.setTranslationX(0.0f);
      info.view.setTranslationY(0.0f);
    }
  }
}
