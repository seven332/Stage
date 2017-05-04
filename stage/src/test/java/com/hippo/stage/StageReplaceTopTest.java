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
import com.hippo.stage.util.TimingCurtainSuppler;
import java.util.ArrayList;
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
public class StageReplaceTopTest {

  private static final int SCENE_COUNT = 3;

  @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-replaceTop-{5}")
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

  private Stage stage;
  private TestContainer container;

  private boolean started;
  private boolean resumed;
  private boolean retainView;
  private int[] opacities;
  private int opacity;

  public StageReplaceTopTest(
      boolean started, boolean resumed, boolean retainView, int[] opacities, int opacity, String name) {
    this.started = started;
    this.resumed = resumed;
    this.retainView = retainView;
    this.opacities = opacities;
    this.opacity = opacity;
  }

  @Before
  public void before() {
    stage = new Stage(new DumpDirector());
    container = new TestContainer(RuntimeEnvironment.application);
    stage.setContainer(container);
    stage.setCurtainSuppler(new TimingCurtainSuppler());
  }

  private void assertCalling(TestScene[] scenes, SceneCalling[] callings, int index) {
    int i = index + 1;
    while (--i >= 0) {
      scenes[i].assertSceneCalling("scenes[" + i + "]: " + scenes[i], callings[i]);
    }
  }

  private int getVisibleSceneCount(List<Integer> opacities, int index) {
    int size = opacities.size();
    int[] array = new int[size];
    for (int i = 0; i < size; ++i) {
      array[i] = opacities.get(i);
    }
    return getVisibleSceneCount(array, index);
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

  private static List<Integer> toList(int[] array) {
    List<Integer> list = new ArrayList<>();
    for (int i : array) {
      list.add(i);
    }
    return list;
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

  @Test
  public void testReplaceTop() {
    if (resumed) {
      stage.start();
      stage.resume();
    } else if (started) {
      stage.start();
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
    stage.replaceTopScene(newScene);
    List<Integer> opacityList = toList(opacities);
    if (opacityList.size() > 0) {
      opacityList.remove(opacityList.size() - 1);
    }
    opacityList.add(opacity);
    int newVisibleCount = getVisibleSceneCount(opacityList, opacities.length - 1);

    /////////////////////////////
    // After replacing top scene
    /////////////////////////////
    newCalling.onCreate++;
    newCalling.onCreateView++;
    newCalling.onAttachView++;
    if (started) {
      newCalling.onStart++;
    }
    if (resumed) {
      newCalling.onResume++;
    }

    if (scenes.length != 0 ) {
      SceneCalling oldTopCalling = callings[scenes.length - 1];
      if (resumed) {
        oldTopCalling.onPause++;
      }

      for (int i = scenes.length - oldVisibleCount- 1; i >= scenes.length - newVisibleCount; --i) {
        Scene scene = scenes[i];
        SceneCalling calling = callings[i];
        if (!scene.willRetainView() || calling.onCreateView == 0) {
          calling.onCreateView++;
        }
        calling.onAttachView++;
        if (started) {
          calling.onStart++;
        }
      }
    }

    assertCalling(scenes, callings, scenes.length - 1);
    newScene.assertSceneCalling(newCalling);

    int[] ids = newIntArray(scenes.length - Math.max(oldVisibleCount, newVisibleCount), scenes.length + 1);
    container.assertChildren(ids);

    ////////////////////////////
    // After curtain completing
    ////////////////////////////

    Reflections.getRunningCurtain(stage).completeImmediately();

    if (scenes.length != 0 ) {
      SceneCalling oldTopCalling = callings[scenes.length - 1];
      if (started) {
        oldTopCalling.onStop++;
      }
      oldTopCalling.onDetachView++;
      oldTopCalling.onDestroyView++;
      oldTopCalling.onDestroy++;
      scenes[scenes.length - 1].assertPair();

      for (int i = scenes.length - newVisibleCount - 1; i >= scenes.length - oldVisibleCount; --i) {
        Scene scene = scenes[i];
        SceneCalling calling = callings[i];
        if (started) {
          calling.onStop++;
        }
        calling.onDetachView++;
        if (!scene.willRetainView()) {
          calling.onDestroyView++;
        }
      }
    }

    assertCalling(scenes, callings, scenes.length - 1);
    newScene.assertSceneCalling(newCalling);

    List<Integer> viewIds = new ArrayList<>();
    for (int i = scenes.length - newVisibleCount; i < scenes.length - 1; ++i) {
      viewIds.add(i);
    }
    viewIds.add(scenes.length);
    container.assertChildren(toArray(viewIds));

    //////////////////
    // Pop all scenes
    //////////////////
    for (int i = 0, n = opacities.length != 0 ? opacities.length : 1; i < n; ++i) {
      stage.popTopScene();
      Reflections.getRunningCurtain(stage).completeImmediately();
    }
    assertEquals(0, stage.getSceneCount());
    for (TestScene scene : scenes) {
      scene.assertPair();
    }
    newScene.assertPair();
    assertEquals(0, container.getChildCount());
  }
}
