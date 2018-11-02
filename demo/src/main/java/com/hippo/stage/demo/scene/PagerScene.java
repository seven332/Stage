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
 * Created by Hippo on 5/5/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.Stage;
import com.hippo.stage.demo.R;
import com.hippo.stage.pager.StagePagerAdapter;

public class PagerScene extends DebugScene {

  public static final String KEY_MODE = "com.hippo.stage.demo.scene.PagerScene.MODE";

  private int mode;

  @Override
  protected void onCreate(@NonNull Bundle args) {
    super.onCreate(args);
    mode = args.getInt(KEY_MODE);
  }

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.scene_pager, container, false);
    TabLayout tabLayout = view.findViewById(R.id.tab_layout);
    ViewPager viewPager = view.findViewById(R.id.view_pager);
    viewPager.setAdapter(new StagePagerAdapter(this, mode) {
      @Override
      public void bindStage(@NonNull Stage stage, int position) {
        stage.pushScene(new HomeScene());
      }

      @Override
      public int getCount() {
        return 5;
      }

      @Override
      public int getStageId(int position) {
        return position;
      }

      @Override
      public CharSequence getPageTitle(int position) {
        return "Page " + position;
      }
    });
    tabLayout.setupWithViewPager(viewPager);
    return view;
  }
}
