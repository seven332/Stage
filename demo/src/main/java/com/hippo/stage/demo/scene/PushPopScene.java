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
 * Created by Hippo on 4/26/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hippo.stage.Scene;
import com.hippo.stage.demo.R;

public class PushPopScene extends DebugScene {

  private static final String KEY_INDEX = "PushPopScene:index";

  private int index;

  @Override
  protected void onCreate(@Nullable Bundle args) {
    super.onCreate(args);

    if (args != null) {
      index = args.getInt(KEY_INDEX);
    }
  }

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.scene_push_pop, container, false);
    TextView text = (TextView) view.findViewById(R.id.text);
    text.setText("Scene " + index);
    view.findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Scene scene = new PushPopScene();
        Bundle args = new Bundle();
        args.putInt(KEY_INDEX, index + 1);
        scene.setArgs(args);
        getStage().pushScene(scene);
      }
    });
    view.findViewById(R.id.pop).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pop();
      }
    });
    return view;
  }
}
