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

package com.hippo.stage.demo.scene;

/*
 * Created by Hippo on 5/10/2017.
 */

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import com.hippo.stage.demo.R;
import com.hippo.stage.fragment.FragmentScene;

public class PreferenceScene extends FragmentScene {

  private static int ID = 0x7fff0000;

  private int id;

  @Override
  protected int onCreateContainerId() {
    if (id == 0) {
      id = ID++;
    }
    return id;
  }

  @NonNull
  @Override
  protected Fragment onCreateFragment() {
    return new TestPreference();
  }

  public static class TestPreference extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.preferences_test);
    }
  }
}
