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
 * Created by Hippo on 6/13/2017.
 */

import static org.junit.Assert.assertEquals;

import com.hippo.stage.rxjava2.SceneLifecycle;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class RecordConsumer implements Consumer<Integer> {

  private SceneCalling calling;

  public RecordConsumer() {
    calling = new SceneCalling();
  }

  public void assertSceneCalling(SceneCalling calling) {
    assertEquals(calling, this.calling);
  }

  @Override
  public void accept(@NonNull Integer integer) throws Exception {
    switch (integer) {
      case SceneLifecycle.CREATE:
        calling.onCreate++;
        break;
      case SceneLifecycle.CREATE_VIEW:
        calling.onCreateView++;
        break;
      case SceneLifecycle.ATTACH_VIEW:
        calling.onAttachView++;
        break;
      case SceneLifecycle.START:
        calling.onStart++;
        break;
      case SceneLifecycle.RESUME:
        calling.onResume++;
        break;
      case SceneLifecycle.PAUSE:
        calling.onPause++;
        break;
      case SceneLifecycle.STOP:
        calling.onStop++;
        break;
      case SceneLifecycle.DETACH_VIEW:
        calling.onDetachView++;
        break;
      case SceneLifecycle.DESTROY_VIEW:
        calling.onDestroyView++;
        break;
      case SceneLifecycle.DESTROY:
        calling.onDestroy++;
        break;
    }
  }
}
