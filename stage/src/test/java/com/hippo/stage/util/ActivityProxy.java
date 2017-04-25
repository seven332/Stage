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

import android.os.Bundle;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

public class ActivityProxy {

  private Bundle savedInstanceState;
  private boolean isCreated;
  private boolean isStarted;
  private boolean isResumed;

  private ActivityController<TestActivity> controller;

  public ActivityProxy() {
    controller = Robolectric.buildActivity(TestActivity.class);
  }

  public TestActivity get() {
    return controller.get();
  }

  public ActivityProxy create() {
    if (isCreated) {
      throw new IllegalStateException("The activity is already created");
    }
    isCreated = true;
    controller.create(savedInstanceState);
    return this;
  }

  public ActivityProxy start() {
    if (isStarted) {
      throw new IllegalStateException("The activity is already started");
    }
    isStarted = true;
    controller.start();
    return this;
  }

  public ActivityProxy resume() {
    if (isResumed) {
      throw new IllegalStateException("The activity is already resumed");
    }
    isResumed = true;
    controller.resume();
    return this;
  }

  public ActivityProxy pause() {
    if (!isResumed) {
      throw new IllegalStateException("The activity isn't resumed");
    }
    isResumed = false;
    controller.pause();
    return this;
  }

  public ActivityProxy stop() {
    if (!isStarted) {
      throw new IllegalStateException("The activity isn't started");
    }
    isStarted = false;
    controller.stop();
    return this;
  }

  public ActivityProxy destroy() {
    if (!isCreated) {
      throw new IllegalStateException("The activity isn't created");
    }
    isCreated = false;
    controller.destroy();
    return this;
  }

  public void finish() {
    if (!isCreated) {
      throw new IllegalStateException("The activity isn't created");
    }
    get().finish();
    if (isResumed) {
      pause();
    }
    if (isStarted) {
      stop();
    }
    destroy();
  }

  public ActivityProxy restoreFromSavedState() {
    if (!isCreated) {
      throw new IllegalStateException("The activity isn't created");
    }

    savedInstanceState = new Bundle();
    controller.saveInstanceState(savedInstanceState);

    if (isResumed) {
      pause();
    }
    if (isStarted) {
      stop();
    }
    destroy();

    controller = Robolectric.buildActivity(TestActivity.class);

    create();

    return this;
  }
}
