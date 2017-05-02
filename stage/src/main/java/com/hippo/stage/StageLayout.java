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
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * The standard container for a {@link Stage}.
 */
public class StageLayout extends FrameLayout {

  private Stage stage;

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

  void setStage(Stage stage) {
    this.stage = stage;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (stage != null) {
      stage.requestFocus();
    }
    return (stage != null && stage.hasCurtainRunning()) || super.onInterceptTouchEvent(ev);
  }
}
