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
 * Created by Hippo on 5/3/2017.
 */

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.hippo.android.dialog.base.DialogView;
import com.hippo.android.dialog.base.DialogViewBuilder;

public class SimpleDialogScene extends DebugDialogScene {

  private static final String TITLE = "李凭箜篌引";
  private static final String MESSAGE = "吴丝蜀桐张高秋，空白凝云颓不流。\n"
      + "江娥啼竹素女愁，李凭中国弹箜篌。\n"
      + "昆山玉碎凤凰叫，芙蓉泣露香兰笑。\n"
      + "十二门前融冷光，二十三丝动紫皇。\n"
      + "女娲炼石补天处，石破天惊逗秋雨。\n"
      + "梦入坤山教神妪，老鱼跳波瘦蛟舞。\n"
      + "吴质不眠倚桂树，露脚斜飞湿寒兔。";
  private static final String OK = "好的";

  @NonNull
  @Override
  protected DialogView onCreateDialogView(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container) {
    return new DialogViewBuilder()
        .title(TITLE)
        .message(MESSAGE)
        .positiveButton(OK, null)
        .build(inflater, container);
  }
}
