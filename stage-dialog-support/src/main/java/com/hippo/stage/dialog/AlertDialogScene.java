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

package com.hippo.stage.dialog;

/*
 * Created by Hippo on 5/3/2017.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * {@link DialogScene} with {@link android.support.v7.appcompat.R.attr#alertDialogTheme}.
 */
public class AlertDialogScene extends DialogScene {

  @Override
  protected void onCreate(@Nullable Bundle args) {
    super.onCreate(args);
    setThemeAttrId(android.support.v7.appcompat.R.attr.alertDialogTheme);
  }
}
