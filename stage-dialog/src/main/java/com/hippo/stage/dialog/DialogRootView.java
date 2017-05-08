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
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.android.resource.AttrResources;

// The root of dialog, shows dim effect, handles cancelled-on-touch-outside,
// resize dialog width.
class DialogRootView extends ViewGroup implements DialogRoot {

  private int dialogWidth;
  private View content;
  private DialogScene dialog;
  private boolean cancelledOnTouchOutside;

  public DialogRootView(Context context) {
    super(context);
    init(context);
  }

  public DialogRootView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public DialogRootView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    dialogWidth = context.getResources().getDimensionPixelSize(R.dimen.sd_dialog_width);

    float dimAmount = AttrResources.getAttrFloat(context, android.R.attr.backgroundDimAmount);
    // Ensure backgroundDimAmount is in range
    dimAmount = clamp(dimAmount, 0.0f, 1.0f);
    final int alpha = (int) (255 * dimAmount);
    setBackgroundColor(Color.argb(alpha, 0, 0, 0));
  }

  public static float clamp(float x, float bound1, float bound2) {
    if (bound2 >= bound1) {
      if (x > bound2) return bound2;
      if (x < bound1) return bound1;
    } else {
      if (x > bound1) return bound1;
      if (x < bound2) return bound2;
    }
    return x;
  }

  public void setCancelledOnTouchOutside(boolean cancel) {
    cancelledOnTouchOutside = cancel;
  }

  void setDialog(DialogScene dialog) {
    this.dialog = dialog;
  }

  private void ensureContent() {
    if (content == null) {
      if (getChildCount() == 0) {
        throw new IllegalStateException("DialogRoot should contain a DialogContent");
      }
      content = getChildAt(0);
    }
  }

  private boolean isUnderView(View view, MotionEvent event) {
    float x = event.getX();
    float y = event.getY();
    return x >= view.getLeft() && x < view.getRight()
        && y >= view.getTop() && y < view.getBottom();
  }

  private void cancel() {
    if (dialog != null) {
      dialog.cancel();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (cancelledOnTouchOutside && event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      ensureContent();
      if (!isUnderView(content, event)) {
        cancel();
      }
    }
    // Always return true to avoid touch through
    return true;
  }

  private static int getMeasuredDimension(int spec, int childDimension) {
    int size = MeasureSpec.getSize(spec);
    int mode = MeasureSpec.getMode(spec);
    if (mode == MeasureSpec.EXACTLY || mode == MeasureSpec.AT_MOST) {
      return size;
    } else {
      return childDimension;
    }
  }

  // android.view.ViewRootImpl
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    ensureContent();
    View content = this.content;
    int baseWidth = dialogWidth;
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    ViewGroup.LayoutParams lp = content.getLayoutParams();

    boolean resizeWidth;
    int childWidthMeasureSpec;
    if (lp.width == LayoutParams.WRAP_CONTENT &&
        (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.EXACTLY) &&
        widthSize > baseWidth) {
      resizeWidth = true;
      childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(baseWidth, MeasureSpec.AT_MOST);
    } else {
      resizeWidth = false;
      childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
    }
    int childHeightMeasureSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, lp.height);

    content.measure(childWidthMeasureSpec, childHeightMeasureSpec);

    if (resizeWidth && (content.getMeasuredWidthAndState() & View.MEASURED_STATE_TOO_SMALL) != 0) {
      // Didn't fit in that width... try expanding a bit.
      baseWidth = (baseWidth + widthSize) / 2;
      childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(baseWidth, MeasureSpec.AT_MOST);
      content.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    if (resizeWidth && (content.getMeasuredWidthAndState() & View.MEASURED_STATE_TOO_SMALL) != 0) {
      // Still didn't fit in that width... restore.
      childWidthMeasureSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
      content.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    int width = getMeasuredDimension(widthMeasureSpec, content.getMeasuredWidth());
    int height = getMeasuredDimension(heightMeasureSpec, content.getMeasuredHeight());
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    ensureContent();
    View content = this.content;
    int contentWidth = content.getMeasuredWidth();
    int contentHeight = content.getMeasuredHeight();
    int left = (getWidth() - contentWidth) / 2;
    int top = (getHeight() - contentHeight) / 2;
    content.layout(left, top, left + contentWidth, top + contentHeight);
  }

  @NonNull
  @Override
  public View getDialogContent() {
    ensureContent();
    return content;
  }
}
