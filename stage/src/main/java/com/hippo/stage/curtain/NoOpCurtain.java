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

package com.hippo.stage.curtain;

/*
 * Created by Hippo on 5/4/2017.
 */

import android.support.annotation.NonNull;
import android.view.ViewGroup;
import com.hippo.stage.Curtain;
import com.hippo.stage.SceneInfo;
import java.util.List;

/**
 * No animation, just call {@link OnCompleteListener#onComplete()}.
 */
public final class NoOpCurtain extends Curtain {

  /**
   * Instance of {@code NoOpCurtain}.
   */
  @NonNull
  public static final NoOpCurtain INSTANCE = new NoOpCurtain();

  // No need to new a instance
  private NoOpCurtain() {}

  @Override
  protected void completeImmediately() {
    // It's already completed
  }

  @Override
  protected void execute(@NonNull ViewGroup container, @NonNull SceneInfo upper,
      @NonNull List<SceneInfo> lower, @NonNull OnCompleteListener listener) {
    // Just call it
    listener.onComplete();
  }
}
