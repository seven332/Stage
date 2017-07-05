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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.hippo.stage.rxjava2.SceneLifecycle;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SceneLifecycleTest {

  private Stage stage;

  @Before
  public void before() {
    stage = new Stage(new DumpDirector());
    stage.setContainer(new FrameLayout(RuntimeEnvironment.application));
  }

  @Test
  public void testSceneLifecycle() throws Exception {
    stage.start();

    Scene scene = new DumpScene();
    stage.pushScene(scene);
    RecordConsumer consumer = new RecordConsumer();
    Disposable disposable = SceneLifecycle.create(scene).subscribe(consumer);
    SceneCalling calling = new SceneCalling();

    calling.onCreate++;
    calling.onCreateView++;
    calling.onAttachView++;
    calling.onStart++;
    consumer.assertSceneCalling(calling);

    stage.resume();
    calling.onResume++;
    consumer.assertSceneCalling(calling);

    assertFalse(disposable.isDisposed());
    disposable.dispose();
    assertTrue(disposable.isDisposed());
    stage.pause();
    consumer.assertSceneCalling(calling);
  }

  @Test
  public void testSceneLifecycleMultiObserver() throws Exception {
    stage.start();

    Scene scene = new DumpScene();
    stage.pushScene(scene);
    Observable<Integer> lifecycle = SceneLifecycle.create(scene);

    RecordConsumer consumer1 = new RecordConsumer();
    Disposable disposable1 = lifecycle.subscribe(consumer1);
    SceneCalling calling1 = new SceneCalling();

    calling1.onCreate++;
    calling1.onCreateView++;
    calling1.onAttachView++;
    calling1.onStart++;
    consumer1.assertSceneCalling(calling1);

    stage.resume();
    calling1.onResume++;
    consumer1.assertSceneCalling(calling1);

    RecordConsumer consumer2 = new RecordConsumer();
    Disposable disposable2 = lifecycle.subscribe(consumer2);
    SceneCalling calling2 = new SceneCalling();

    calling2.onCreate++;
    calling2.onCreateView++;
    calling2.onAttachView++;
    calling2.onStart++;
    calling2.onResume++;
    consumer2.assertSceneCalling(calling2);

    stage.pause();
    calling1.onPause++;
    consumer1.assertSceneCalling(calling1);
    calling2.onPause++;
    consumer2.assertSceneCalling(calling2);

    assertFalse(disposable1.isDisposed());
    disposable1.dispose();
    assertTrue(disposable1.isDisposed());
    stage.resume();
    consumer1.assertSceneCalling(calling1);
    calling2.onResume++;
    consumer2.assertSceneCalling(calling2);

    assertFalse(disposable2.isDisposed());
    disposable2.dispose();
    assertTrue(disposable2.isDisposed());
    stage.pause();
    consumer1.assertSceneCalling(calling1);
    consumer2.assertSceneCalling(calling2);
  }

  static class DumpDirector extends Director {
    @Override
    public void requestFocus() {}
    @Override
    int requireSceneId() { return 0; }
    @Override
    boolean willDestroyActivity() { return false; }
    @Nullable
    @Override
    Activity getActivity() { return null; }
    @Override
    void startActivity(@NonNull Intent intent) {}
    @Override
    void startActivity(@android.support.annotation.NonNull Intent intent, @Nullable Bundle options) {}
    @Override
    void startActivityForResult(Intent intent, int requestCode) {}
    @Override
    void startActivityForResult(Intent intent, int requestCode, Bundle options) {}
    @Override
    void requestPermissions(@android.support.annotation.NonNull String[] permissions, int requestCode) {}
  }

  static class DumpScene extends Scene {
    @NonNull
    @Override
    protected View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
      return new View(inflater.getContext());
    }
  }
}


