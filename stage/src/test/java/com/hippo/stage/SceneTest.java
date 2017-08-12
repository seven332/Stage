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
 * Created by Hippo on 2017/8/12.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.hippo.stage.util.TestContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneTest {

  private Director director;
  private Stage stage;

  @Before
  public void before() {
    director = new DumpDirector();
    stage = new Stage(director);
    stage.setContainer(new TestContainer(RuntimeEnvironment.application));
  }

  @Test
  public void testRecreateView() {
    stage.start();
    stage.resume();

    Scene scene1 = new TestScene();
    stage.pushScene(scene1);
    Scene scene2 = new TestScene();
    stage.pushScene(scene2);

    assertEquals(true, scene2.getLifecycleState().isResumed());
    int hashCode1 = scene2.getView().hashCode();

    scene2.recreateView();

    assertEquals(true, scene2.getLifecycleState().isResumed());
    int hashCode2 = scene2.getView().hashCode();

    assertNotEquals(hashCode1, hashCode2);
  }
}
