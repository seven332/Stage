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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.hippo.stage.Director;
import com.hippo.stage.Stage;

public class TestActivity extends Activity {

  private Director director;
  private FrameLayout root;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    director = Director.hire(this, savedInstanceState);

    root = new FrameLayout(this);
    setContentView(root);
  }

  public Stage installStage(int id) {
    ViewGroup container = null;
    for (int i = 0; i < root.getChildCount(); ++i) {
      View child = root.getChildAt(i);
      if (child.getId() == id) {
        container = (ViewGroup) child;
        break;
      }
    }
    if (container == null) {
      container = new TestContainer(this);
      container.setId(id);
      root.addView(container);
    }

    return director.direct(container);
  }
}
