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
 * Created by Hippo on 4/25/2017.
 */

import static org.junit.Assert.assertEquals;

import com.hippo.stage.util.Reflections;
import com.hippo.stage.util.TestActivity;
import com.hippo.stage.util.TestScene;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StageActivityTest {

  private ActivityController<TestActivity> controller;

  @Before
  public void before() {
    controller = Robolectric.buildActivity(TestActivity.class);
  }

  @Test
  public void testActivityLifecycleCatching() {
    controller.create();

    Stage stage1 = controller.get().installStage(1);
    assertEquals(false, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));

    controller.start();
    assertEquals(true, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));

    controller.resume();
    assertEquals(true, Reflections.isStarted(stage1));
    assertEquals(true, Reflections.isResumed(stage1));

    Stage stage2 = controller.get().installStage(2);
    assertEquals(true, Reflections.isStarted(stage2));
    assertEquals(true, Reflections.isResumed(stage2));

    controller.pause();
    assertEquals(true, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));
    assertEquals(true, Reflections.isStarted(stage2));
    assertEquals(false, Reflections.isResumed(stage2));

    controller.stop();
    assertEquals(false, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));
    assertEquals(false, Reflections.isStarted(stage2));
    assertEquals(false, Reflections.isResumed(stage2));

    controller.destroy();
    assertEquals(false, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));
    assertEquals(false, Reflections.isStarted(stage2));
    assertEquals(false, Reflections.isResumed(stage2));
  }

  @Test
  public void testActivityFinish() {
    controller.create().start().resume();

    Stage stage = controller.get().installStage(0);

    TestScene scene1 = new TestScene();
    TestScene scene2 = new TestScene();
    TestScene scene3 = new TestScene();
    stage.pushScene(scene1);
    stage.pushScene(scene2);
    stage.pushScene(scene3);
    assertEquals(3, stage.getSceneCount());

    controller.get().finish();
    controller.pause().stop().destroy();

    assertEquals(0, stage.getSceneCount());
    assertEquals(true, scene1.isDestroyed());
    assertEquals(true, scene2.isDestroyed());
    assertEquals(true, scene3.isDestroyed());
  }
}
