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

import com.github.dakusui.combinatoradix.Permutator;
import com.hippo.stage.util.DumpDirector;
import com.hippo.stage.util.HomogeniousPermutator;
import com.hippo.stage.util.Reflections;
import com.hippo.stage.util.SceneCalling;
import com.hippo.stage.util.TestContainer;
import com.hippo.stage.util.TestScene;
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
public class StagePopTest {

  private static final int SCENE_COUNT = 3;

  @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-pop-{5}")
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
          for (List<Integer> popOrder : new Permutator<>(toList(newIntArray(0, SCENE_COUNT)), SCENE_COUNT)) {
            parameters.add(new Object[] {
                started,
                resumed,
                retainView,
                toArray(opacities),
                toArray(popOrder),
                getName(started, resumed, retainView, opacities, popOrder)
            });
          }
        }
      }
    }

    return parameters;
  }

  private static String getName(boolean started, boolean resumed, boolean retainView, List<Integer> opacities, List<Integer> popOrder) {
    StringBuilder sb = new StringBuilder();
    sb.append(started ? 'T' : 'F');
    sb.append(resumed ? 'T' : 'F');
    sb.append(retainView ? 'T' : 'F');
    sb.append('-');
    for (int opacity : opacities) {
      sb.append(opacity);
    }
    sb.append('-');
    for (int order : popOrder) {
      sb.append(order);
    }
    return sb.toString();
  }

  private static List<Integer> toList(int[] array) {
    List<Integer> list = new ArrayList<>();
    for (int i : array) {
      list.add(i);
    }
    return list;
  }

  private static int[] toArray(List<Integer> list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < list.size(); ++i) {
      array[i] = list.get(i);
    }
    return array;
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

  private TestStage stage;
  private TestContainer container;

  private boolean started;
  private boolean resumed;
  private boolean retainView;
  private int[] opacities;
  private int[] popOrder;

  public StagePopTest(boolean started, boolean resumed, boolean retainView, int[] opacities, int[] popOrder, String name) {
    this.started = started;
    this.resumed = resumed;
    this.retainView = retainView;
    this.opacities = opacities;
    this.popOrder = popOrder;
  }

  @Before
  public void before() {
    stage = new TestStage(new DumpDirector());
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

  @Test
  public void testPopEntry() {
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

    List<TestScene> sceneList = new ArrayList<>(Arrays.asList(scenes));
    List<SceneCalling> callingList = new ArrayList<>(Arrays.asList(callings));
    List<Integer> opacityList = toList(opacities);
    for (int i = 0; i < size; ++i) {
      TestScene sceneToPop = scenes[popOrder[i]];
      int oldVisibleCount = getVisibleSceneCount(opacityList, SCENE_COUNT - i - 1);
      stage.popScene(sceneToPop);
      int poppedIndex = sceneList.indexOf(sceneToPop);
      TestScene poppedScene = sceneList.remove(poppedIndex);
      SceneCalling poppedCalling = callingList.remove(poppedIndex);
      int poppedOpacity = opacityList.remove(poppedIndex);
      int newVisibleCount = getVisibleSceneCount(opacityList, SCENE_COUNT - i - 2);

      ///////////////////////
      // After popping scene
      ///////////////////////

      // If the popped scene is the top, pause it
      boolean popTop = poppedIndex == sceneList.size();
      if (popTop && resumed) {
        poppedCalling.onPause++;
      }

      boolean isPoppedViewAttached = poppedScene.isViewAttached();

      if (isPoppedViewAttached) {
        // If the popped scene can't be seen through, the visible scenes under it is newly attached
        boolean willAttachNewScene = poppedOpacity == Scene.OPAQUE || (poppedOpacity == Scene.TRANSLUCENT && !popTop);
        if (willAttachNewScene) {
          // popped scene should be the last visible scene
          assertEquals(sceneList.size() + 1 - oldVisibleCount, poppedIndex);
        }

        // Attache new view
        for (int index = poppedIndex - 1; index >= sceneList.size() - newVisibleCount; --index) {
          Scene scene = sceneList.get(index);
          SceneCalling calling = callingList.get(index);

          if (willAttachNewScene) {
            // Retain-view scene only call onCreateView() once without Activity recreating
            if (!scene.willRetainView() || calling.onCreateView == 0) {
              calling.onCreateView++;
            }
            calling.onAttachView++;
            if (started) {
              calling.onStart++;
            }
          }

          if (resumed && index == sceneList.size() - 1) {
            calling.onResume++;
          }

          if (index == sceneList.size() - 1 && opacityList.get(index) == Scene.TRANSLUCENT) {
            // An translucent scene become top now, the following scene must be newly attached
            willAttachNewScene = true;
          }
        }
      } else {
        if (retainView) {
          poppedCalling.onDestroyView++;
        }
        poppedCalling.onDestroy++;
      }

      assertCalling(scenes, callings, SCENE_COUNT - 1);

      List<TestScene> oldSceneList = new ArrayList<>(sceneList);
      oldSceneList.add(poppedIndex, poppedScene);
      int viewCount = Math.max(oldVisibleCount, isPoppedViewAttached ? newVisibleCount + 1 : newVisibleCount);
      List<Integer> viewIds = new ArrayList<>(viewCount);
      for (int index = oldSceneList.size() - viewCount; index < oldSceneList.size(); ++index) {
        viewIds.add(oldSceneList.get(index).getView().getId());
      }
      container.assertChildren(toArray(viewIds));

      ////////////////////////////
      // After curtain completing
      ////////////////////////////

      if (isPoppedViewAttached) {
        Reflections.getRunningCurtain(stage).completeImmediately();

        if (started) {
          poppedCalling.onStop++;
        }
        poppedCalling.onDetachView++;
        poppedCalling.onDestroyView++;
        poppedCalling.onDestroy++;

        assertCalling(scenes, callings, SCENE_COUNT - 1);
      }

      viewIds = new ArrayList<>(viewCount);
      viewCount = newVisibleCount;
      for (int index = sceneList.size() - viewCount; index < sceneList.size(); ++index) {
        viewIds.add(Integer.parseInt(sceneList.get(index).getTag()));
      }
      container.assertChildren(toArray(viewIds));
    }

    for (TestScene scene : scenes) {
      scene.assertPair();
    }
    assertEquals(0, container.getChildCount());
  }

  private static class TestStage extends Stage {
    public TestStage(Director director) {
      super(director);
    }
  }
}
