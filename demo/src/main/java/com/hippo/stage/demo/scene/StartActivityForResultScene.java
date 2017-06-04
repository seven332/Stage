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
 * Created by Hippo on 4/30/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hippo.stage.demo.R;

public class StartActivityForResultScene extends DebugScene {

  private static final int REQUEST_CODE = 123;

  private Uri uri;
  private ImageView image;

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = inflater.inflate(R.layout.scene_for_result, container, false);
    image = (ImageView) view.findViewById(R.id.image);
    if (uri != null) {
      image.setImageURI(uri);
    }
    view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE);
      }
    });
    return view;
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    image = null;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        Uri uri = data.getData();
        if (uri != null) {
          this.uri = uri;
          if (image != null) {
            image.setImageURI(uri);
          }
        }
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
