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

package com.hippo.stage;

/*
 * Created by Hippo on 5/1/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import java.util.List;

class SceneHostedDirector extends Director {

  private Scene scene;

  void setScene(Scene scene) {
    this.scene = scene;
  }

  @Override
  public void requestFocus() {
    if (scene != null) {
      scene.requestFocus();
    }
  }

  @Nullable
  @Override
  Curtain requestCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower) {
    Curtain curtain = super.requestCurtain(upper, lower);
    if (curtain == null && scene != null) {
      curtain = scene.requestCurtain(upper, lower);
    }
    return curtain;
  }

  @Override
  int requireSceneId() {
    return scene.requireSceneId();
  }

  @Override
  boolean isActivityDestroyed() {
    return scene == null || scene.isActivityDestroyed();
  }

  @Nullable
  @Override
  Activity getActivity() {
    return scene != null ? scene.getActivity() : null;
  }

  @Override
  void startActivity(@NonNull Intent intent) {
    if (scene != null) {
      scene.startActivity(intent);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  @Override
  void startActivity(@NonNull Intent intent, @Nullable Bundle options) {
    if (scene != null) {
      scene.startActivity(intent, options);
    }
  }

  @Override
  void startActivityForResult(Intent intent, int requestCode) {
    if (scene != null) {
      scene.startActivityForResult(intent, requestCode);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
  @Override
  void startActivityForResult(Intent intent, int requestCode, Bundle options) {
    if (scene != null) {
      scene.startActivityForResult(intent, requestCode, options);
    }
  }

  @Override
  void requestPermissions(@NonNull String[] permissions, int requestCode) {
    if (scene != null) {
      scene.requestPermissions(permissions, requestCode);
    }
  }

  @Override
  void destroy() {
    super.destroy();
    scene = null;
  }
}
