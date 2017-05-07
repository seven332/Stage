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

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import com.hippo.stage.Director;
import com.hippo.stage.Stage;

/**
 * The best choice for {@link ViewPager} of {@link StagePagerAdapter}.
 * It blocks touch even when curtain running,
 * requests focus for {@link Stage} when a page selected,
 * only handles back action for selected page,
 * disables children view states saving.
 */
public class StagePager extends ViewPager {

  private Director director;

  public StagePager(Context context) {
    super(context);
    init();
  }

  public StagePager(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  private void init() {
    addOnPageChangeListener(new SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        if (director != null) {
          // Request focus for the current Stage
          Stage stage = director.get(position);
          if (stage != null) {
            stage.requestFocus();
          }
        }
      }
    });
  }

  @Override
  public void setAdapter(PagerAdapter adapter) {
    super.setAdapter(adapter);

    if (adapter instanceof StagePagerAdapter) {
      director = ((StagePagerAdapter) adapter).getHost().hireChildDirector();
      director.setBackHandler(new PagerBackHandler());
    } else if (director != null) {
      director.setBackHandler(null);
      director = null;
    }
  }

  private boolean hasCurtainRunning() {
    if (director != null) {
      for (Stage stage : director) {
        if (stage.hasCurtainRunning()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
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

  private static class PagerBackHandler implements Director.BackHandler {
    @Override
    public boolean handleBack(Director director) {
      // Only care the focused Stage, which is the visible Stage for user
      Stage stage = director.getFocusedStage();
      if (stage != null) {
        return stage.handleBack();
      } else {
        return director.onHandleBack();
      }
    }
  }
}
