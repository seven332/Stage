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
 * Created by Hippo on 4/20/2017.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import com.hippo.stage.util.TestScene;
import org.junit.Before;
import org.junit.Test;

public class SceneStackTest {

  private SceneStack stack;
  private int pushed;
  private int popped;
  private Scene pushedScene;
  private Scene poppedScene;

  @Before
  public void before() {
    stack = new SceneStack(new SceneStack.Callback() {
      @Override
      public void onPush(@NonNull Scene scene) {
        pushed++;
        pushedScene = scene;
        poppedScene = null;
      }
      @Override
      public void onPop(@NonNull Scene scene) {
        popped++;
        pushedScene = null;
        poppedScene = scene;
      }
    });
  }

  @Test
  public void testIsEmpty() {
    assertTrue(stack.isEmpty());

    stack.push(new TestScene());
    assertFalse(stack.isEmpty());

    stack.push(new TestScene());
    assertFalse(stack.isEmpty());

    stack.pop();
    assertFalse(stack.isEmpty());

    stack.pop();
    assertTrue(stack.isEmpty());
  }

  @Test
  public void testSize() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();
    Scene scene3 = new TestScene();

    assertEquals(0, stack.size());

    stack.push(scene1);
    assertEquals(1, stack.size());

    stack.pop(scene1);
    assertEquals(0, stack.size());

    stack.push(scene2);
    stack.push(scene3);
    assertEquals(2, stack.size());

    stack.pop(scene2);
    assertEquals(1, stack.size());

    stack.pop(scene3);
    assertEquals(0, stack.size());

    stack.pop(scene1);
    assertEquals(0, stack.size());
  }

  @Test
  public void testPeek() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();
    Scene scene3 = new TestScene();

    assertEquals(null, stack.peek());

    stack.push(scene1);
    assertEquals(scene1, stack.peek());

    stack.push(scene2);
    assertEquals(scene2, stack.peek());

    stack.push(scene3);
    assertEquals(scene3, stack.peek());

    stack.pop(scene2);
    assertEquals(scene3, stack.peek());

    stack.pop(scene3);
    assertEquals(scene1, stack.peek());

    stack.pop(scene1);
    assertEquals(null, stack.peek());

    stack.pop(scene3);
    assertEquals(null, stack.peek());
  }

  @Test
  public void testTail() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();
    Scene scene3 = new TestScene();

    assertEquals(null, stack.tail());

    stack.push(scene1);
    assertEquals(scene1, stack.tail());

    stack.push(scene2);
    assertEquals(scene1, stack.tail());

    stack.push(scene3);
    assertEquals(scene1, stack.tail());

    stack.pop(scene2);
    assertEquals(scene1, stack.tail());

    stack.pop(scene1);
    assertEquals(scene3, stack.tail());

    stack.pop(scene3);
    assertEquals(null, stack.tail());

    stack.pop(scene2);
    assertEquals(null, stack.tail());
  }

  @Test
  public void testPushPop() {
    assertNull(stack.pop());

    Scene scene1 = new TestScene();
    stack.push(scene1);
    Scene scene2 = new TestScene();
    stack.push(scene2);

    assertEquals(scene2, stack.pop());
    assertEquals(scene1, stack.pop());
    assertNull(stack.pop());
  }

  @Test
  public void testPopScene() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();
    Scene scene3 = new TestScene();

    assertEquals(-1, stack.pop(scene1));

    stack.push(scene1);
    stack.push(scene2);

    assertEquals(-1, stack.pop(scene3));
    assertEquals(1, stack.pop(scene1));
    assertEquals(0, stack.pop(scene2));
    assertEquals(-1, stack.pop(scene1));
  }

  @Test
  public void testCallback() {
    Scene scene1 = new TestScene();
    Scene scene2 = new TestScene();
    Scene scene3 = new TestScene();

    stack.pop();
    assertEquals(0, pushed);
    assertEquals(0, popped);
    assertEquals(null, pushedScene);
    assertEquals(null, poppedScene);

    stack.push(scene1);
    assertEquals(1, pushed);
    assertEquals(0, popped);
    assertEquals(scene1, pushedScene);
    assertEquals(null, poppedScene);

    stack.push(scene2);
    assertEquals(2, pushed);
    assertEquals(0, popped);
    assertEquals(scene2, pushedScene);
    assertEquals(null, poppedScene);

    stack.pop(scene3);
    assertEquals(2, pushed);
    assertEquals(0, popped);
    assertEquals(scene2, pushedScene);
    assertEquals(null, poppedScene);

    stack.pop(scene1);
    assertEquals(2, pushed);
    assertEquals(1, popped);
    assertEquals(null, pushedScene);
    assertEquals(scene1, poppedScene);

    stack.pop(scene2);
    assertEquals(2, pushed);
    assertEquals(2, popped);
    assertEquals(null, pushedScene);
    assertEquals(scene2, poppedScene);

    stack.pop(scene1);
    assertEquals(2, pushed);
    assertEquals(2, popped);
    assertEquals(null, pushedScene);
    assertEquals(scene2, poppedScene);
  }
}
