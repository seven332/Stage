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
 * Created by Hippo on 5/2/2017.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

/**
 * A {@code CurtainSuppler} supplies {@link Curtain} for {@link Stage}.
 */
public interface CurtainSuppler {

  /**
   * Returns a {@link Curtain} for these scenes.
   * <p>
   * If no animation should be played, returns
   * {@link com.hippo.stage.curtain.NoOpCurtain#INSTANCE} instead of {@code null},
   * otherwise, another {@code CurtainSuppler} with lower priority might be
   * asked.
   */
  @Nullable
  Curtain getCurtain(@NonNull SceneInfo upper, @NonNull List<SceneInfo> lower);
}
