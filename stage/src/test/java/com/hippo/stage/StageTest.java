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
 * Created by Hippo on 4/22/2017.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
public class StageTest {

  private Stage stage;

  @Before
  public void before() {
    stage = new TestStage();
    stage.setContainer(new TestContainer(RuntimeEnvironment.application));
  }

  @Test
  public void testGetTopScene() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();

    assertNull(stage.getTopScene());

    stage.pushScene(scene1);
    assertEquals(scene1, stage.getTopScene());

    stage.pushScene(scene2);
    assertEquals(scene2, stage.getTopScene());
  }

  @Test
  public void testGetRootScene() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();

    assertNull(stage.getRootScene());

    stage.pushScene(scene1);
    assertEquals(scene1, stage.getRootScene());

    stage.pushScene(scene2);
    assertEquals(scene1, stage.getRootScene());
  }

  private static class TestStage extends Stage {}
}
