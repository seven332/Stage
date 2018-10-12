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

package com.hippo.stage.util;

/*
 * Created by Hippo on 4/25/2017.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class TestView extends FrameLayout {

  private static int SAVED_KEY;

  private int savedKey;

  public TestView(Context context) {
    super(context);
  }

  public TestView(Context context,
      @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public int getSavedKey() {
    if (savedKey == 0) {
      savedKey = ++SAVED_KEY;
    }
    return savedKey;
  }

  public void setSavedKey(int savedKey) {
    this.savedKey = savedKey;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putParcelable("super", super.onSaveInstanceState());
    bundle.putInt("saved_key", getSavedKey());
    return bundle;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    Bundle bundle = (Bundle) state;
    super.onRestoreInstanceState(bundle.getParcelable("super"));
    setSavedKey(bundle.getInt("saved_key"));
  }
}
