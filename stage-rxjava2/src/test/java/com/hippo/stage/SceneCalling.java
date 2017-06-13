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
 * Created by Hippo on 4/23/2017.
 */

import com.hippo.yorozuya.HashCodeUtils;

public class SceneCalling {

  public int onCreate;
  public int onCreateView;
  public int onAttachView;
  public int onStart;
  public int onResume;
  public int onPause;
  public int onStop;
  public int onDetachView;
  public int onDestroyView;
  public int onDestroy;

  public boolean isPair() {
    return onCreate == onDestroy &&
        onCreateView == onDestroyView &&
        onAttachView == onDetachView &&
        onStart == onStop &&
        onResume == onPause;
  }

  public SceneCalling copy() {
    SceneCalling calling = new SceneCalling();
    calling.onCreate = onCreate;
    calling.onCreateView = onCreateView;
    calling.onAttachView = onAttachView;
    calling.onStart = onStart;
    calling.onResume = onResume;
    calling.onPause = onPause;
    calling.onStop = onStop;
    calling.onDetachView = onDetachView;
    calling.onDestroyView = onDestroyView;
    calling.onDestroy = onDestroy;
    return calling;
  }

  @Override
  public int hashCode() {
    return HashCodeUtils.hashCode(
        onCreate,
        onCreateView,
        onAttachView,
        onStart,
        onResume,
        onPause,
        onStop,
        onDetachView,
        onDestroyView,
        onDestroy
    );
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof SceneCalling) {
      SceneCalling calling = (SceneCalling) obj;
      return calling.onCreate == onCreate &&
          calling.onCreateView == onCreateView &&
          calling.onAttachView == onAttachView &&
          calling.onStart == onStart &&
          calling.onResume == onResume &&
          calling.onPause == onPause &&
          calling.onStop == onStop &&
          calling.onDetachView == onDetachView &&
          calling.onDestroyView == onDestroyView &&
          calling.onDestroy == onDestroy;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "SceneCalling: {\n"
        + "onCreate: " + onCreate + ", \n"
        + "onCreateView: " + onCreateView + ", \n"
        + "onAttachView: " + onAttachView + ", \n"
        + "onStart: " + onStart + ", \n"
        + "onResume: " + onResume + ", \n"
        + "onPause: " + onPause + ", \n"
        + "onStop: " + onStop + ", \n"
        + "onDetachView: " + onDetachView + ", \n"
        + "onDestroyView: " + onDestroyView + ", \n"
        + "onDestroy: " + onDestroy + ", \n"
        + "}";
  }
}
