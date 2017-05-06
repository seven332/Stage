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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.stage.util.SceneCalling;
import com.hippo.stage.util.TestView;

public class TestScene extends Scene {

  private static final String KEY_ID = "TestScene:id";
  private static final String KEY_OPACITY = "TestScene:opacity";
  private static final String KEY_RETAIN_VIEW = "TestScene:retain_view";

  private int id;
  @Opacity
  private int opacity;

  private SceneCalling calling = new SceneCalling();

  private static int SAVED_KEY;
  private int savedKey;

  public SceneCalling copyCalling() {
    return calling.copy();
  }

  public void assertSceneCalling(SceneCalling calling) {
    assertEquals(calling, this.calling);
  }

  public void assertSceneCalling(String message, SceneCalling calling) {
    assertEquals(message, calling, this.calling);
  }

  public void assertPair() {
    assertTrue("calling = " + calling, calling.isPair());
  }

  @Override
  public TestView getView() {
    return (TestView) super.getView();
  }

  @Override
  protected void onCreate(@Nullable Bundle args) {
    super.onCreate(args);
    id = args != null ? args.getInt(KEY_ID) : 0;
    setTag(Integer.toString(id));
    //noinspection SimplifiableConditionalExpression
    setWillRetainView(args != null ? args.getBoolean(KEY_RETAIN_VIEW) : false);
    //noinspection WrongConstant
    setOpacity(args != null ? args.getInt(KEY_OPACITY) : OPAQUE);
    calling.onCreate++;
  }

  @NonNull
  @Override
  protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    View view = new TestView(inflater.getContext());
    view.setId(id);
    calling.onCreateView++;
    return view;
  }

  @Override
  protected void onAttachView(@NonNull View view) {
    super.onAttachView(view);
    calling.onAttachView++;
  }

  @Override
  protected void onStart() {
    super.onStart();
    calling.onStart++;
  }

  @Override
  protected void onResume() {
    super.onResume();
    calling.onResume++;
  }

  @Override
  protected void onPause() {
    super.onPause();
    calling.onPause++;
  }

  @Override
  protected void onStop() {
    super.onStop();
    calling.onStop++;
  }

  @Override
  protected void onDetachView(@NonNull View view) {
    super.onDetachView(view);
    calling.onDetachView++;
  }

  @Override
  protected void onDestroyView(@NonNull View view) {
    super.onDestroyView(view);
    calling.onDestroyView++;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    calling.onDestroy++;
  }

  public int getSavedKey() {
    if (savedKey == 0) {
      savedKey = ++SAVED_KEY;
    }
    return savedKey;
  }

  public void setSavedKey(int savedKey) {
    this.savedKey = savedKey;
  }

  @Override
  protected void onSaveViewState(@NonNull View view, @NonNull Bundle outState) {
    super.onSaveViewState(view, outState);
    outState.putInt("saved_key", getSavedKey());
  }

  @Override
  protected void onRestoreViewState(@NonNull View view, @NonNull Bundle savedViewState) {
    super.onRestoreViewState(view, savedViewState);
    setSavedKey(savedViewState.getInt("saved_key"));
  }

  public static TestScene create(int id, @Opacity int opacity, boolean retainView) {
    TestScene scene = new TestScene();
    Bundle args = new Bundle();
    args.putInt(KEY_ID, id);
    args.putInt(KEY_OPACITY, opacity);
    args.putBoolean(KEY_RETAIN_VIEW, retainView);
    scene.setArgs(args);
    return scene;
  }
}
