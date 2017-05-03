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

package com.hippo.stage.demo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import com.hippo.stage.Curtain;
import com.hippo.stage.CurtainSuppler;
import com.hippo.stage.Director;
import com.hippo.stage.SceneInfo;
import com.hippo.stage.Stage;
import com.hippo.stage.curtain.FadeCurtain;
import com.hippo.stage.curtain.ShiftCurtain;
import com.hippo.stage.demo.scene.HomeScene;
import com.hippo.stage.dialog.DialogScene;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CurtainSuppler {

  private Director director;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    director = Director.hire(this, savedInstanceState);
    director.setCurtainSuppler(this);

    ViewGroup container = (ViewGroup) findViewById(R.id.stage_layout);

    Stage stage = director.direct(container);

    if (stage.getSceneCount() == 0) {
      stage.pushScene(new HomeScene());
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // Avoid memory leak
    director.setCurtainSuppler(null);
  }

  @Override
  public void onBackPressed() {
    if (!director.handleBack()) {
      super.onBackPressed();
    }
  }

  @Nullable
  @Override
  public Curtain getCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    if (lower.isEmpty()) {
      return null;
    } else if (upper.scene instanceof DialogScene) {
      FadeCurtain curtain = new FadeCurtain();
      curtain.setDuration(150L);
      return curtain;
    } else {
      ShiftCurtain curtain = new ShiftCurtain();
      curtain.setDuration(150L);
      curtain.setInterpolator(new FastOutSlowInInterpolator());
      return curtain;
    }
  }
}
