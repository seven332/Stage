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

import android.support.annotation.NonNull;
import android.view.View;
import com.hippo.stage.Scene;
import io.reactivex.ObservableEmitter;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Cancellable;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.ArrayList;
import java.util.List;

class StageLifecycleListener extends Scene.LifecycleListener {

  private List<ObservableEmitter<Integer>> emitterList = new ArrayList<>();

  private int lifecycle;

  public StageLifecycleListener(@NonNull Scene scene) {
    scene.addLifecycleListener(this);

    if (scene.isResumed()) {
      lifecycle = StageLifecycle.RESUME;
    } else if (scene.isStarted()) {
      lifecycle = StageLifecycle.START;
    } else if (scene.isViewAttached()) {
      lifecycle = StageLifecycle.ATTACH_VIEW;
    } else if (scene.getView() != null) {
      lifecycle = StageLifecycle.CREATE_VIEW;
    } else if (!scene.isDestroyed()) {
      lifecycle = StageLifecycle.CREATE;
    } else {
      lifecycle = StageLifecycle.INIT;
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
    if (lifecycle >= StageLifecycle.CREATE && lifecycle < StageLifecycle.DESTROY) {
      emit(emitter, StageLifecycle.CREATE);
    }
    if (lifecycle >= StageLifecycle.CREATE_VIEW && lifecycle < StageLifecycle.DESTROY_VIEW) {
      emit(emitter, StageLifecycle.CREATE_VIEW);
    }
    if (lifecycle >= StageLifecycle.ATTACH_VIEW && lifecycle < StageLifecycle.DETACH_VIEW) {
      emit(emitter, StageLifecycle.ATTACH_VIEW);
    }
    if (lifecycle >= StageLifecycle.START && lifecycle < StageLifecycle.STOP) {
      emit(emitter, StageLifecycle.START);
    }
    if (lifecycle >= StageLifecycle.RESUME && lifecycle < StageLifecycle.PAUSE) {
      emit(emitter, StageLifecycle.RESUME);
    }
  }

  private void emit(ObservableEmitter<Integer> emitter, int lifecycle) {
    if (!emitter.isDisposed()) {
      emitter.onNext(lifecycle);
    }
  }

  private void emit(int lifecycle) {
    this.lifecycle = lifecycle;

    if (!emitterList.isEmpty()) {
      for (ObservableEmitter<Integer> emitter : new ArrayList<>(emitterList)) {
        try {
          emit(emitter, lifecycle);
        } catch (Throwable t) {
          Exceptions.throwIfFatal(t);
          RxJavaPlugins.onError(t);
        }
      }
    }
  }

  @Override
  public void onCreate(@NonNull Scene scene) {
    emit(StageLifecycle.CREATE);
  }

  @Override
  public void onCreateView(@NonNull Scene scene) {
    emit(StageLifecycle.CREATE_VIEW);
  }

  @Override
  public void onAttachView(@NonNull Scene scene, @NonNull View view) {
    emit(StageLifecycle.ATTACH_VIEW);
  }

  @Override
  public void onStart(@NonNull Scene scene) {
    emit(StageLifecycle.START);
  }

  @Override
  public void onResume(@NonNull Scene scene) {
    emit(StageLifecycle.RESUME);
  }

  @Override
  public void onPause(@NonNull Scene scene) {
    emit(StageLifecycle.PAUSE);
  }

  @Override
  public void onStop(@NonNull Scene scene) {
    emit(StageLifecycle.STOP);
  }

  @Override
  public void onDetachView(@NonNull Scene scene, @NonNull View view) {
    emit(StageLifecycle.DETACH_VIEW);
  }

  @Override
  public void onDestroyView(@NonNull Scene scene, @NonNull View view) {
    emit(StageLifecycle.DESTROY_VIEW);
  }

  @Override
  public void onDestroy(@NonNull Scene scene) {
    emit(StageLifecycle.DESTROY);
  }
}
