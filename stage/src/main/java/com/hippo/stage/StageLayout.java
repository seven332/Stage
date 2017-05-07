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
 * Created by Hippo on 5/2/2017.
 */

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import java.util.HashSet;
import java.util.Set;

/**
 * The standard container for a {@link Stage}.
 * It blocks touch even when curtain running,
 * requests focus when getting touch event,
 * disables children view states saving.
 */
public class StageLayout extends FrameLayout {

  private Set<Stage> stageSet = new HashSet<>();

  public StageLayout(@NonNull Context context) {
    super(context);
  }

  public StageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public StageLayout(@NonNull Context context, @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  void addStage(Stage stage) {
    stageSet.add(stage);
  }

  void removeStage(Stage stage) {
    stageSet.remove(stage);
  }

  private void requestStageFocus() {
    if (stageSet.size() > 0) {
      stageSet.iterator().next().requestFocus();
    }
  }

  private boolean hasCurtainRunning() {
    if (stageSet.size() > 0) {
      for (Stage stage : stageSet) {
        if (stage.hasCurtainRunning()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    requestStageFocus();
    return hasCurtainRunning() || super.onInterceptTouchEvent(ev);
  }

  @Override
  protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    dispatchFreezeSelfOnly(container);
  }

  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    dispatchThawSelfOnly(container);
  }
}
