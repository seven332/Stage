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
 * Created by Hippo on 5/4/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * An {@code Announcer} passes metadata to a {@link Scene}.
 * The metadata is saved for Scene recreating.
 */
public final class Announcer {

  private Scene scene;
  private String tag;
  private Bundle args;

  private Announcer(Scene scene) {
    this.scene = scene;
  }

  /**
   * Sets tag for the scene.
   */
  public Announcer tag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * Sets args for the scene.
   */
  public Announcer args(Bundle args) {
    this.args = args;
    return this;
  }

  Scene build() {
    scene.setTag(tag);
    scene.setArgs(args);
    return scene;
  }

  /**
   * Create an Announcer of the scene.
   */
  public static Announcer of(@NonNull Scene scene) {
    return new Announcer(scene);
  }
}
