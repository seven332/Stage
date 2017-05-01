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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import com.hippo.stage.util.Reflections;
import com.hippo.stage.util.TestContainer;
import com.hippo.stage.util.TestScene;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ChildDirectorTest {

  private Stage stage;
  private Scene scene;

  @Before
  public void before() {
    stage = new DumpStage(new DumpDirector());
    stage.setContainer(new TestContainer(RuntimeEnvironment.application));
    scene = new TestScene();
    stage.pushScene(scene);
  }

  @Test
  public void testLifecycle1() {
    Director director = scene.hireChildDirector();
    assertFalse(Reflections.isStarted(director));
    assertFalse(Reflections.isResumed(director));

    stage.start();
    assertTrue(Reflections.isStarted(director));
    assertFalse(Reflections.isResumed(director));

    stage.resume();
    assertTrue(Reflections.isStarted(director));
    assertTrue(Reflections.isResumed(director));

    stage.pause();
    assertTrue(Reflections.isStarted(director));
    assertFalse(Reflections.isResumed(director));

    stage.stop();
    assertFalse(Reflections.isStarted(director));
    assertFalse(Reflections.isResumed(director));
  }

  @Test
  public void testLifecycle2() {
    stage.start();

    Director director = scene.hireChildDirector();
    assertTrue(Reflections.isStarted(director));
    assertFalse(Reflections.isResumed(director));
  }

  @Test
  public void testLifecycle3() {
    stage.start();
    stage.resume();

    Director director = scene.hireChildDirector();
    assertTrue(Reflections.isStarted(director));
    assertTrue(Reflections.isResumed(director));
  }

  @Test
  public void testSaveRestoreState() {
    Director childDirector = scene.hireChildDirector();
    Stage childStage = childDirector.direct(new TestContainer(RuntimeEnvironment.application));
    Scene childScene = TestScene.create(100, Scene.TRANSLUCENT, false);
    childStage.pushScene(childScene);

    Bundle state = new Bundle();
    stage.saveInstanceState(state);

    Stage newStage = new DumpStage(new DumpDirector());
    newStage.setContainer(new TestContainer(RuntimeEnvironment.application));
    newStage.restoreInstanceState(state);
    Scene newScene = newStage.getTopScene();

    Director newChildDirector = newScene.hireChildDirector();
    Stage newChildStage = newChildDirector.direct(new TestContainer(RuntimeEnvironment.application));
    assertEquals(1, childStage.getSceneCount());

    Scene newChildScene = newChildStage.getTopScene();
    assertEquals("100", newChildScene.getTag());
  }
}
