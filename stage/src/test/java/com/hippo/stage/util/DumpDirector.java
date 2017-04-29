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
 * Created by Hippo on 4/29/2017.
 */

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import com.hippo.stage.Director;
import com.hippo.stage.Stage;

public class DumpDirector implements Director {

  @NonNull
  @Override
  public Stage direct(@NonNull ViewGroup container) {
    return null;
  }

  @Override
  public int requireSceneId() {
    return 0;
  }

  @Nullable
  @Override
  public Activity getActivity() {
    return null;
  }
}
