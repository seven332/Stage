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
 * Created by Hippo on 5/3/2017.
 */

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.hippo.stage.demo.R;
import com.hippo.stage.dialog.DialogScene;

public class DialogListScene extends DebugScene {

  private static final String[] ITEMS = {
      "Not Cancelled",
      "Cancelled",
      "Cancelled on Touch Outside",
      "No dim",
  };

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.scene_list, container, false);
    ListView listView = (ListView) view.findViewById(R.id.list);
    listView.setAdapter(new ArrayAdapter<>(inflater.getContext(), android.R.layout.simple_list_item_1, ITEMS));
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DialogScene scene = new SimpleDialogScene();
        switch (position) {
          case 0:
            scene.setCancellable(false);
            scene.setCancelledOnTouchOutside(false);
            scene.setShowBackgroundDim(true);
            break;
          case 1:
            scene.setCancellable(true);
            scene.setCancelledOnTouchOutside(false);
            scene.setShowBackgroundDim(true);
            break;
          case 2:
            scene.setCancellable(true);
            scene.setCancelledOnTouchOutside(true);
            scene.setShowBackgroundDim(true);
            break;
          case 3:
            scene.setCancellable(true);
            scene.setCancelledOnTouchOutside(true);
            scene.setShowBackgroundDim(false);
            break;
        }
        getStage().pushScene(scene);
      }
    });
    return view;
  }
}
