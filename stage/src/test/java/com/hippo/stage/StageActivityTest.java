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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.hippo.stage.util.ActivityProxy;
import com.hippo.stage.util.Reflections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StageActivityTest {

  private ActivityProxy proxy;

  @Before
  public void before() {
    proxy = new ActivityProxy();
  }

  @Test
  public void testActivityLifecycleCatching() {
    proxy.create();

    Stage stage1 = proxy.get().installStage(1);
    assertEquals(false, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));

    proxy.start();
    assertEquals(true, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));

    proxy.resume();
    assertEquals(true, Reflections.isStarted(stage1));
    assertEquals(true, Reflections.isResumed(stage1));

    Stage stage2 = proxy.get().installStage(2);
    assertEquals(true, Reflections.isStarted(stage2));
    assertEquals(true, Reflections.isResumed(stage2));

    proxy.pause();
    assertEquals(true, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));
    assertEquals(true, Reflections.isStarted(stage2));
    assertEquals(false, Reflections.isResumed(stage2));

    proxy.stop();
    assertEquals(false, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));
    assertEquals(false, Reflections.isStarted(stage2));
    assertEquals(false, Reflections.isResumed(stage2));

    proxy.destroy();
    assertEquals(false, Reflections.isStarted(stage1));
    assertEquals(false, Reflections.isResumed(stage1));
    assertEquals(false, Reflections.isStarted(stage2));
    assertEquals(false, Reflections.isResumed(stage2));
  }

  @Test
  public void testActivityFinish() {
    proxy.create().start().resume();

    Stage stage = proxy.get().installStage(0);

    TestScene scene1 = new TestScene();
    TestScene scene2 = new TestScene();
    TestScene scene3 = new TestScene();
    stage.pushScene(scene1);
    stage.pushScene(scene2);
    stage.pushScene(scene3);
    assertEquals(3, stage.getSceneCount());

    proxy.finish();

    assertEquals(0, stage.getSceneCount());
    assertEquals(true, scene1.getLifecycleState().hasDestroyed());
    assertEquals(true, scene2.getLifecycleState().hasDestroyed());
    assertEquals(true, scene3.getLifecycleState().hasDestroyed());
  }

  @Test
  public void testActivityRestoreFromSavedState() {
    proxy.create().start().resume();

    Stage stage = proxy.get().installStage(0);
    TestScene scene = TestScene.create(3, Scene.OPAQUE, true);
    stage.pushScene(scene);
    int sceneSavedKey = scene.getSavedKey();
    assertNotEquals(0, sceneSavedKey);
    int viewSavedKey = scene.getView().getSavedKey();
    assertNotEquals(0, viewSavedKey);

    proxy.restoreFromSavedState().start().resume();

    stage = proxy.get().installStage(0);
    TestScene newScene = (TestScene) stage.getTopScene();
    assertNotNull(newScene);
    assertEquals(true, newScene.getLifecycleState().isViewAttached());
    assertEquals(scene.getArgs(), newScene.getArgs());
    assertEquals(scene.willRetainView(), newScene.willRetainView());
    assertEquals(scene.getTag(), newScene.getTag());
    assertEquals(sceneSavedKey, newScene.getSavedKey());
    assertEquals(viewSavedKey, newScene.getView().getSavedKey());
  }

  @Test
  public void testSceneGetId() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();

    proxy.create();
    Stage stage = proxy.get().installStage(0);

    stage.pushScene(scene1);
    stage.pushScene(scene2);
    int id1 = scene1.getId();
    int id2 = scene2.getId();
    assertNotEquals(Scene.INVALID_ID, id1);
    assertNotEquals(Scene.INVALID_ID, id2);
    assertNotEquals(id1, id2);

    proxy.restoreFromSavedState();
    stage = proxy.get().installStage(0);

    assertEquals(id2, stage.getTopScene().getId());
    stage.popTopScene();
    assertEquals(id1, stage.getTopScene().getId());

    Scene scene3 = new TestScene();
    stage.pushScene(scene3);
    assertNotEquals(id1, scene3.getId());
    assertNotEquals(id2, scene3.getId());
    assertNotEquals(Scene.INVALID_ID, scene3.getId());
  }
}
