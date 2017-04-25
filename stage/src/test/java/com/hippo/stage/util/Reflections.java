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

package com.hippo.stage.util;

/*
 * Created by Hippo on 4/23/2017.
 */

import static org.junit.Assert.assertTrue;

import com.hippo.stage.Curtain;
import com.hippo.stage.Stage;
import java.lang.reflect.Field;

public class Reflections {

  private static Object getField(Object instance, Class<?> clazz, String name) {
    assertTrue(clazz.isAssignableFrom(instance.getClass()));
    try {
      Field field = clazz.getDeclaredField(name);
      field.setAccessible(true);
      return field.get(instance);
    } catch (Exception e) {
      throw new RuntimeException("Can't get field " + name + " of " + clazz.getName(), e);
    }
  }

  public static Curtain getRunningCurtain(Stage stage) {
    return (Curtain) getField(stage, Stage.class, "runningCurtain");
  }

  public static boolean isStarted(Stage stage) {
    return (boolean) getField(stage, Stage.class, "isStarted");
  }

  public static boolean isResumed(Stage stage) {
    return (boolean) getField(stage, Stage.class, "isResumed");
  }
}
