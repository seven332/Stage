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

import android.support.annotation.IdRes;
import android.view.ViewGroup;
import com.hippo.stage.util.TestContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DirectorTest {

  private Director director;

  @Before
  public void before() {
    director = new DumpDirector();
  }

  @Test
  public void testFindSceneById() {
    ViewGroup container1 = new TestContainer(RuntimeEnvironment.application);
    ViewGroup container2 = new TestContainer(RuntimeEnvironment.application);
    @IdRes int containerId1 = 1;
    @IdRes int containerId2 = 2;
    container1.setId(containerId1);
    container2.setId(containerId2);

    Stage stage1 = director.direct(container1);
    Stage stage2 = director.direct(container2);

    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();
    stage1.pushScene(scene1);
    stage1.pushScene(scene2);

    Scene scene3 = new TestScene();
    Scene scene4 = new TestScene();
    stage2.pushScene(scene3);
    stage2.pushScene(scene4);

    Director director2 = scene2.hireChildDirector();
    Stage stage3 = director2.direct(new TestContainer(RuntimeEnvironment.application));
    Scene scene5 = new TestScene();
    stage3.pushScene(scene5);

    Director director3 = scene3.hireChildDirector();
    Stage stage4 = director3.direct(new TestContainer(RuntimeEnvironment.application));
    Scene scene6 = new TestScene();
    stage4.pushScene(scene6);

    assertEquals(scene1, director.findSceneById(1));
    assertEquals(scene2, director.findSceneById(2));
    assertEquals(scene3, director.findSceneById(3));
    assertEquals(scene4, director.findSceneById(4));
    assertEquals(scene5, director.findSceneById(5));
    assertEquals(scene6, director.findSceneById(6));
    assertEquals(null, director.findSceneById(7));
  }

  @Test
  public void testCloseStage() {
    ViewGroup container = new TestContainer(RuntimeEnvironment.application);
    Stage stage = director.direct(container);

    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();

    stage.pushScene(scene1);
    stage.pushScene(scene2);

    assertEquals(false, scene1.isDestroyed());
    assertEquals(false, scene2.isDestroyed());
    assertEquals(1, container.getChildCount());

    stage.close();

    assertEquals(true, scene1.isDestroyed());
    assertEquals(true, scene2.isDestroyed());
    assertEquals(0, container.getChildCount());
  }

  @Test
  public void testHeadlessStage() {
    director.start();
    director.resume();

    Stage stage = director.direct(1);

    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();

    stage.pushScene(scene1);
    stage.pushScene(scene2);

    assertFalse(scene2.isViewAttached());

    stage.restore(new TestContainer(RuntimeEnvironment.application));

    assertTrue(scene2.isViewAttached());
  }
}
