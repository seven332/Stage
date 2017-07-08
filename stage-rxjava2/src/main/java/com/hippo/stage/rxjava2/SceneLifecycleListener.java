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

package com.hippo.stage.rxjava2;

/*
 * Created by Hippo on 6/13/2017.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import com.hippo.stage.Scene;
import io.reactivex.ObservableEmitter;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Cancellable;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.ArrayList;
import java.util.List;

class SceneLifecycleListener extends Scene.LifecycleListener {

  private List<ObservableEmitter<Integer>> emitterList = new ArrayList<>();

  private int lifecycle;

  public SceneLifecycleListener(@NonNull Scene scene) {
    scene.addLifecycleListener(this);

    Scene.LifecycleState state = scene.getLifecycleState();
    if (state.isResumed()) {
      lifecycle = SceneLifecycle.RESUME;
    } else if (state.isStarted()) {
      lifecycle = SceneLifecycle.START;
    } else if (state.isViewAttached()) {
      lifecycle = SceneLifecycle.ATTACH_VIEW;
    } else if (state.isViewCreated()) {
      lifecycle = SceneLifecycle.CREATE_VIEW;
    } else if (state.isCreated()) {
      lifecycle = SceneLifecycle.CREATE;
    } else {
      lifecycle = SceneLifecycle.INIT;
    }
  }

  public void addEmitter(@NonNull final ObservableEmitter<Integer> emitter) {
    emitterList.add(emitter);
    emitter.setCancellable(new Cancellable() {
      @Override
      public void cancel() throws Exception {
        emitterList.remove(emitter);
      }
    });
    emitMissingLifecycle(emitter);
  }

  private void emitMissingLifecycle(ObservableEmitter<Integer> emitter) {
    if (lifecycle >= SceneLifecycle.CREATE && lifecycle < SceneLifecycle.DESTROY) {
      emit(emitter, SceneLifecycle.CREATE);
    }
    if (lifecycle >= SceneLifecycle.CREATE_VIEW && lifecycle < SceneLifecycle.DESTROY_VIEW) {
      emit(emitter, SceneLifecycle.CREATE_VIEW);
    }
    if (lifecycle >= SceneLifecycle.ATTACH_VIEW && lifecycle < SceneLifecycle.DETACH_VIEW) {
      emit(emitter, SceneLifecycle.ATTACH_VIEW);
    }
    if (lifecycle >= SceneLifecycle.START && lifecycle < SceneLifecycle.STOP) {
      emit(emitter, SceneLifecycle.START);
    }
    if (lifecycle >= SceneLifecycle.RESUME && lifecycle < SceneLifecycle.PAUSE) {
      emit(emitter, SceneLifecycle.RESUME);
    }
  }

  private void emit(ObservableEmitter<Integer> emitter, int lifecycle) {
    if (!emitter.isDisposed()) {
      try {
        emitter.onNext(lifecycle);
      } catch (Throwable t) {
        Exceptions.throwIfFatal(t);
        RxJavaPlugins.onError(t);
      }
    }
  }

  private void emit(int lifecycle) {
    this.lifecycle = lifecycle;

    if (!emitterList.isEmpty()) {
      for (ObservableEmitter<Integer> emitter : new ArrayList<>(emitterList)) {
        emit(emitter, lifecycle);
      }
    }
  }

  @Override
  public void onCreate(@NonNull Scene scene, @NonNull Bundle args) {
    emit(SceneLifecycle.CREATE);
  }

  @Override
  public void onCreateView(@NonNull Scene scene) {
    emit(SceneLifecycle.CREATE_VIEW);
  }

  @Override
  public void onAttachView(@NonNull Scene scene, @NonNull View view) {
    emit(SceneLifecycle.ATTACH_VIEW);
  }

  @Override
  public void onStart(@NonNull Scene scene) {
    emit(SceneLifecycle.START);
  }

  @Override
  public void onResume(@NonNull Scene scene) {
    emit(SceneLifecycle.RESUME);
  }

  @Override
  public void onPause(@NonNull Scene scene) {
    emit(SceneLifecycle.PAUSE);
  }

  @Override
  public void onStop(@NonNull Scene scene) {
    emit(SceneLifecycle.STOP);
  }

  @Override
  public void onDetachView(@NonNull Scene scene, @NonNull View view) {
    emit(SceneLifecycle.DETACH_VIEW);
  }

  @Override
  public void onDestroyView(@NonNull Scene scene, @NonNull View view) {
    emit(SceneLifecycle.DESTROY_VIEW);
  }

  @Override
  public void onDestroy(@NonNull Scene scene) {
    emit(SceneLifecycle.DESTROY);
  }
}
