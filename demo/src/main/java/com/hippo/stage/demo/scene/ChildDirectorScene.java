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

package com.hippo.stage.demo.scene;

/*
 * Created by Hippo on 5/2/2017.
 */

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.Director;
import com.hippo.stage.Stage;
import com.hippo.stage.demo.R;

public class ChildDirectorScene extends DebugScene {

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    final View view = inflater.inflate(R.layout.scene_child_director, container, false);

    ViewGroup container1 = (ViewGroup) view.findViewById(R.id.stage_layout1);
    ViewGroup container2 = (ViewGroup) view.findViewById(R.id.stage_layout2);

    Director director = hireChildDirector();

    Stage stage1 = director.direct(container1);
    if (stage1.getSceneCount() == 0) {
      stage1.pushScene(new HomeScene());
    }

    Stage stage2 = director.direct(container2);
    if (stage2.getSceneCount() == 0) {
      stage2.pushScene(new HomeScene());
    }

    return view;
  }
}
