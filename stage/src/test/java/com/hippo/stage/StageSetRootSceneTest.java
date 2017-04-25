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
 * Created by Hippo on 4/24/2017.
 */

import static org.junit.Assert.assertEquals;

import com.hippo.stage.util.HomogeniousPermutator;
import com.hippo.stage.util.Reflections;
import com.hippo.stage.util.SceneCalling;
import com.hippo.stage.util.TestContainer;
import com.hippo.stage.util.TestScene;
import com.hippo.stage.util.TimingCurtainSuppler;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class StageSetRootSceneTest {

  private static final int SCENE_COUNT = 3;

  @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-setRoot-{5}")
  public static List<Object[]> data() {
    List<Object[]> parameters = new LinkedList<>();

    boolean[] starteds = new boolean[] {false, true, true};
    boolean[] resumeds = new boolean[] {false, false, true};
    boolean[] retainViews = new boolean[] {false, true};
    for (int i = 0; i < 3; ++i) {
      boolean started = starteds[i];
      boolean resumed = resumeds[i];
      for (boolean retainView : retainViews) {
        for (int j = 0; j <= SCENE_COUNT; ++j) {
          for (List<Integer> opacities : new HomogeniousPermutator<>(
              Arrays.asList(Scene.TRANSPARENT, Scene.TRANSLUCENT, Scene.OPAQUE), j)) {
            for (int opacity : Arrays.asList(Scene.TRANSPARENT, Scene.TRANSLUCENT, Scene.OPAQUE)) {
              parameters.add(new Object[] {
                  started,
                  resumed,
                  retainView,
                  toArray(opacities),
                  opacity,
                  getName(started, resumed, retainView, opacities, opacity)
              });
            }
          }
        }
      }
    }
    return parameters;
  }

  private static String getName(boolean started, boolean resumed, boolean retainView, List<Integer> opacities, int opacity) {
    StringBuilder sb = new StringBuilder();
    sb.append(started ? 'T' : 'F');
    sb.append(resumed ? 'T' : 'F');
    sb.append(retainView ? 'T' : 'F');
    sb.append('-');
    for (int o : opacities) {
      sb.append(o);
    }
    sb.append('-');
    sb.append(opacity);
    return sb.toString();
  }

  private static int[] toArray(List<Integer> list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < list.size(); ++i) {
      array[i] = list.get(i);
    }
    return array;
  }

  private TestStage stage;
  private TestContainer container;

  private boolean started;
  private boolean resumed;
  private boolean retainView;
  private int[] opacities;
  private int opacity;

  public StageSetRootSceneTest(
      boolean started, boolean resumed, boolean retainView, int[] opacities, int opacity, String name) {
    this.started = started;
    this.resumed = resumed;
    this.retainView = retainView;
    this.opacities = opacities;
    this.opacity = opacity;
  }

  @Before
  public void before() {
    stage = new TestStage();
    container = new TestContainer(RuntimeEnvironment.application);
    stage.setContainer(container);
    stage.setCurtainSuppler(new TimingCurtainSuppler());
  }

  private static int[] newIntArray(int from, int to) {
    int size = Math.abs(from - to);
    int[] array = new int[size];
    int step = from < to ? 1 : -1;
    for (int i = 0, value = from; i < size; ++i, value += step) {
      array[i] = value;
    }
    return array;
  }

  private void assertCalling(TestScene[] scenes, SceneCalling[] callings, int index) {
    int i = index + 1;
    while (--i >= 0) {
      scenes[i].assertSceneCalling("scenes[" + i + "]: " + scenes[i], callings[i]);
    }
  }

  private int getVisibleSceneCount(int[] opacities, int index) {
    int count = 0;
    int i = index + 1;
    while (--i >= 0) {
      count++;
      int opacity = opacities[i];
      if (opacity == Scene.OPAQUE || (opacity == Scene.TRANSLUCENT && i != index)) {
        break;
      }
    }
    return count;
  }

  @Test
  public void testSetRoot() {
    if (resumed) {
      stage.onActivityStarted();
      stage.onActivityResumed();
    } else if (started) {
      stage.onActivityStarted();
    }

    int size = opacities.length;
    TestScene[] scenes = new TestScene[size];
    SceneCalling[] callings = new SceneCalling[size];

    for (int i = 0; i < size; ++i) {
      TestScene scene = TestScene.create(i, opacities[i], retainView);
      scenes[i] = scene;
      stage.pushScene(scene);
      Reflections.getRunningCurtain(stage).completeImmediately();
    }
    for (int i = 0; i < size; ++i) {
      callings[i] = scenes[i].copyCalling();
    }

    TestScene newScene = TestScene.create(size, opacity, retainView);
    SceneCalling newCalling = new SceneCalling();
    int oldVisibleCount = getVisibleSceneCount(opacities, opacities.length - 1);
    stage.setRootScene(newScene);

    //////////////////////
    // After setting root
    //////////////////////
    newCalling.onCreate++;
    newCalling.onCreateView++;
    newCalling.onAttachView++;
    if (started) {
      newCalling.onStart++;
    }
    if (resumed) {
      newCalling.onResume++;
    }

    if (scenes.length > 0) {
      SceneCalling oldTopCalling = callings[scenes.length - 1];
      if (resumed) {
        oldTopCalling.onPause++;
      }

      for (int i = scenes.length - oldVisibleCount - 1; i >= 0; --i) {
        Scene scene = scenes[i];
        SceneCalling calling = callings[i];
        if (scene.willRetainView()) {
          calling.onDestroyView++;
        }
        calling.onDestroy++;
      }
    }

    assertCalling(scenes, callings, scenes.length - 1);
    newScene.assertSceneCalling(newCalling);

    int[] ids = newIntArray(scenes.length - oldVisibleCount, scenes.length + 1);
    container.assertChildren(ids);

    ////////////////////////////
    // After curtain completing
    ////////////////////////////

    Reflections.getRunningCurtain(stage).completeImmediately();

    for (int i = scenes.length - 1; i >= scenes.length - oldVisibleCount; --i) {
      SceneCalling calling = callings[i];
      if (started) {
        calling.onStop++;
      }
      calling.onDetachView++;
      calling.onDestroyView++;
      calling.onDestroy++;
    }

    assertCalling(scenes, callings, scenes.length - 1);
    newScene.assertSceneCalling(newCalling);
    for (TestScene scene : scenes) {
      scene.assertPair();
    }

    container.assertChildren(scenes.length);

    //////////////////
    // Pop all scenes
    //////////////////
    stage.popTopScene();
    Reflections.getRunningCurtain(stage).completeImmediately();
    newScene.assertPair();
    assertEquals(0, container.getChildCount());
  }

  private static class TestStage extends Stage {}
}
