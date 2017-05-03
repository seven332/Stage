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

package com.hippo.stage.dialog;

/*
 * Created by Hippo on 5/3/2017.
 */

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.FrameLayout;

// The actual content of dialog, direct child of DialogRoot.
class DialogContentView extends FrameLayout {

  private int minWidthSize;
  private float miniWidthPercent;

  public DialogContentView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public DialogContentView(@NonNull Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public DialogContentView(@NonNull Context context,
      @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  @SuppressWarnings("deprecation")
  private void init(Context context) {
    // Set min width
    Resources.Theme theme = context.getTheme();
    int resId = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        ? android.R.attr.windowMinWidthMinor
        : android.R.attr.windowMinWidthMajor;
    TypedValue tv = new TypedValue();
    theme.resolveAttribute(resId, tv, true);
    if (tv.type == TypedValue.TYPE_DIMENSION) {
      DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
      setMinWidthSize((int) tv.getDimension(metrics));
    } else if (tv.type == TypedValue.TYPE_FRACTION) {
      setMiniWidthPercent(tv.getFraction(1, 1));
    }

    // Set background
    Drawable drawable = ResourcesUtils.getAttrDrawable(context, android.R.attr.windowBackground);
    setBackgroundDrawable(drawable);

    // Set elevation
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      float elevation = ResourcesUtils.getAttrDimension(context, android.R.attr.windowElevation);
      setElevation(elevation);
    }
  }

  void setMinWidthSize(int minWidthSize) {
    this.minWidthSize = minWidthSize;
    this.miniWidthPercent = 0.0f;
    requestLayout();
  }

  void setMiniWidthPercent(float miniWidthPercent) {
    this.minWidthSize = 0;
    this.miniWidthPercent = miniWidthPercent;
    requestLayout();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    boolean measure = false;
    final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    if (widthMode == MeasureSpec.AT_MOST && (minWidthSize != 0 || miniWidthPercent != 0.0f)) {
      final int min;
      if (minWidthSize != 0) {
        min = minWidthSize;
      } else {
        min = (int) (MeasureSpec.getSize(widthMeasureSpec) * miniWidthPercent);
      }

      if (getMeasuredWidth() < min) {
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(min, MeasureSpec.EXACTLY);
        measure = true;
      }
    }

    if (measure) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }
}
