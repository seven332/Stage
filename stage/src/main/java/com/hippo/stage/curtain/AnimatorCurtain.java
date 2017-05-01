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
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import com.hippo.stage.Curtain;
import com.hippo.stage.SceneInfo;
import java.util.List;

/**
 * An {@code AnimatorCurtain} uses an {@link android.animation.Animator Animator}
 * to show transition between {@link com.hippo.stage.Scene Scene}s.
 */
public abstract class AnimatorCurtain extends Curtain {

  private View view;
  private ViewTreeObserver.OnPreDrawListener onPreDrawListener;
  private Animator animator;
  private OnCompleteListener listener;

  private long duration = -1;
  private TimeInterpolator interpolator;

  /**
   * Sets duration for the {@link Animator} returned by {@link #getAnimator(SceneInfo, List)}.
   * Negative value will be ignored.
   */
  public void setDuration(long duration) {
    this.duration = duration;
  }

  /**
   * Sets interpolator for the {@link Animator} returned by {@link #getAnimator(SceneInfo, List)}.
   */
  public void setInterpolator(TimeInterpolator interpolator) {
    this.interpolator = interpolator;
  }

  @Override
  protected void completeImmediately() {
    if (view != null && onPreDrawListener != null) {
      view.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
    }
    view = null;
    onPreDrawListener = null;

    if (animator != null) {
      animator.end();
      animator = null;
    }

    if (listener != null) {
      listener.onComplete();
      listener = null;
    }
  }

  @Nullable
  private View getFirstNonLaidOutView(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    if (!isLaidOut(upper.view)) {
      return upper.view;
    }
    for (SceneInfo info : lower) {
      if (!isLaidOut(info.view)) {
        return info.view;
      }
    }
    return null;
  }

  @Override
  protected void execute(@NonNull final SceneInfo upper, @NonNull final List<SceneInfo> lower,
      @NonNull final OnCompleteListener listener) {
    this.listener = listener;
    view = getFirstNonLaidOutView(upper, lower);
    if (view != null) {
      onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          if (view != null && onPreDrawListener != null) {
            view.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
          }
          view = null;
          onPreDrawListener = null;
          animate(upper, lower);
          return true;
        }
      };
      view.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener);
    } else {
      animate(upper, lower);
    }
  }

  private void animate(@NonNull final SceneInfo upper, @NonNull final List<SceneInfo> lower) {
    Animator animate = getAnimator(upper, lower);
    if (animate != null) {
      this.animator = animate;
      if (duration >= 0) {
        animate.setDuration(duration);
      }
      if (interpolator != null) {
        animate.setInterpolator(interpolator);
      }
      animate.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          animator = null;
          restore(upper, lower);
          if (listener != null) {
            listener.onComplete();
            listener = null;
          }
        }
      });
      animate.start();
    } else {
      if (listener != null) {
        listener.onComplete();
        listener = null;
      }
    }
  }

  /**
   * Returns an {@link Animator} to show transition between {@link com.hippo.stage.Scene Scene}s.
   */
  @Nullable
  protected abstract Animator getAnimator(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower);

  /**
   * Will be called after the animator ends to reset the detached view to its pre-animation state.
   */
  protected abstract void restore(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower);

  private static boolean isLaidOut(View view) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      return view.isLaidOut();
    } else {
      return view.getWidth() > 0 && view.getHeight() > 0;
    }
  }
}
