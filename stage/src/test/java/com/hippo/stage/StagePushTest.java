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

import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.support.annotation.Nullable;
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
public class StagePushTest {

  private static final int SCENE_COUNT = 3;

  @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-push-{4}")
  public static List<Object[]> data() {
    List<Object[]> parameters = new LinkedList<>();

    boolean[] starteds = new boolean[] {false, true, true};
    boolean[] resumeds = new boolean[] {false, false, true};
    boolean[] retainViews = new boolean[] {false, true};
    for (int i = 0; i < 3; ++i) {
      boolean started = starteds[i];
      boolean resumed = resumeds[i];
      for (boolean retainView : retainViews) {
        for (List<Integer> opacities : new HomogeniousPermutator<>(Arrays.asList(Scene.TRANSPARENT, Scene.TRANSLUCENT, Scene.OPAQUE), SCENE_COUNT)) {
          parameters.add(new Object[] {
              started,
              resumed,
              retainView,
              toArray(opacities),
              getName(started, resumed, retainView, opacities)
          });
        }
      }
    }

    return parameters;
  }

  private static String getName(boolean started, boolean resumed, boolean retainView, List<Integer> opacities) {
    StringBuilder sb = new StringBuilder();
    sb.append(started ? 'T' : 'F');
    sb.append(resumed ? 'T' : 'F');
    sb.append(retainView ? 'T' : 'F');
    sb.append('-');
    for (int opacity : opacities) {
      sb.append(opacity);
    }
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

  public StagePushTest(
      boolean started, boolean resumed, boolean retainView, int[] opacities, String name) {
    this.started = started;
    this.resumed = resumed;
    this.retainView = retainView;
    this.opacities = opacities;
  }

  @Before
  public void before() {
    stage = new TestStage();
    container = new TestContainer(RuntimeEnvironment.application);
    stage.setContainer(container);
    stage.setCurtainSuppler(new TimingCurtainSuppler());
  }

  private static void assertCalling(TestScene[] scenes, SceneCalling[] callings, int index) {
    int i = index + 1;
    while (--i >= 0) {
      scenes[i].assertSceneCalling("scenes[" + i + "]: " + scenes[i], callings[i]);
    }
  }

  private static int getVisibleSceneCount(int[] opacities, int index) {
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
  public void testPush() {
    if (resumed) {
      stage.start();
      stage.resume();
    } else if (started) {
      stage.start();
    }

    TestScene[] scenes = new TestScene[opacities.length];
    SceneCalling[] callings = new SceneCalling[opacities.length];

    for (int i = 0, n = opacities.length; i < n; ++i) {
      TestScene scene = TestScene.create(i, opacities[i], retainView);
      scenes[i] = scene;
      SceneCalling calling = new SceneCalling();
      callings[i] = calling;
      scene.assertSceneCalling(calling);

      int oldVisibleCount = getVisibleSceneCount(opacities, i - 1);
      stage.pushScene(scene);
      int newVisibleCount = getVisibleSceneCount(opacities, i);

      ///////////////////////
      // After pushing scene
      ///////////////////////

      calling.onCreate++;
      calling.onCreateView++;
      calling.onAttachView++;
      if (started) {
        calling.onStart++;
      }
      if (resumed) {
        calling.onResume++;
      }

      // Pause origin top scene
      if (i != 0 && resumed) {
        callings[i - 1].onPause++;
      }

      // Only popped view can be attached
      assertTrue(oldVisibleCount + 1 >= newVisibleCount);

      assertCalling(scenes, callings, i);

      container.assertChildren(newIntArray(i - oldVisibleCount, i + 1));

      ////////////////////////////
      // After curtain completing
      ////////////////////////////

      Reflections.getRunningCurtain(stage).completeImmediately();

      // Detach invisible views
      int index = i - newVisibleCount + 1;
      while (--index >= i - oldVisibleCount) {
        if (started) {
          callings[index].onStop++;
        }
        callings[index].onDetachView++;
        if (!retainView) {
          callings[index].onDestroyView++;
        }
      }

      assertCalling(scenes, callings, i);

      container.assertChildren(newIntArray(i - newVisibleCount + 1, i + 1));
    }
  }

  private static class TestStage extends Stage {
    @Nullable
    @Override
    Activity getActivity() {
      return null;
    }
  }
}
