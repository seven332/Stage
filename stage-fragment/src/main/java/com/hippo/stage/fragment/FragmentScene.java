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

package com.hippo.stage.fragment;

/*
 * Created by Hippo on 5/10/2017.
 */

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.hippo.stage.Scene;

/**
 * {@code FragmentScene} shows a {@link Fragment} as its view.
 * The fragment is added or attached in {@link #onAttachView(View)},
 * is detached in {@link #onDetachView(View)}, is removed in {@link #onDestroy()}.
 * Except for that, there is no relationship between scene lifecycle and fragment lifecycle.
 */
public abstract class FragmentScene extends Scene {

  private int containerId = View.NO_ID;
  private String fragmentTag;

  @NonNull
  @Override
  protected final View onCreateView(@NonNull LayoutInflater inflater,
      @NonNull ViewGroup container) {
    View view = new FrameLayout(inflater.getContext());
    view.setId(getContainerId());
    return view;
  }

  /**
   * Creates the view id for the container of the fragment.
   * This id must be unique, otherwise the fragment might be
   * attached to another container,
   */
  @IdRes
  protected abstract int onCreateContainerId();

  /**
   * Creates the fragment.
   */
  @NonNull
  protected abstract Fragment onCreateFragment();

  private int getContainerId() {
    if (containerId == View.NO_ID) {
      containerId = onCreateContainerId();
    }
    return containerId;
  }

  private String getFragmentTag() {
    if (fragmentTag == null) {
      fragmentTag = makeFragmentTag(getContainerId());
    }
    return fragmentTag;
  }

  @Override
  protected void onAttachView(@NonNull View view) {
    super.onAttachView(view);

    //noinspection ConstantConditions
    FragmentManager fm = getActivity().getFragmentManager();
    Fragment fragment = fm.findFragmentByTag(getFragmentTag());

    if (fragment == null) {
      fragment = onCreateFragment();
      fm.beginTransaction().add(getContainerId(), fragment, getFragmentTag()).commit();
    } else {
      FragmentTransaction transaction = fm.beginTransaction();
      // transaction.attach() may not work if the the fragment isn't detached
      if (!fragment.isDetached()) {
        transaction.detach(fragment);
      }
      transaction.attach(fragment).commit();
    }
  }

  @Override
  protected void onDetachView(@NonNull View view) {
    super.onDetachView(view);

    // FragmentTransaction is not allowed if Activity is destroyed
    if (!willDestroyActivity()) {
      //noinspection ConstantConditions
      FragmentManager fm = getActivity().getFragmentManager();
      Fragment fragment = fm.findFragmentByTag(getFragmentTag());

      if (fragment != null) {
        fm.beginTransaction().detach(fragment).commit();
      }
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    // FragmentTransaction is not allowed if Activity is destroyed
    if (!willDestroyActivity() && (!willRecreate())) {
      //noinspection ConstantConditions
      FragmentManager fm = getActivity().getFragmentManager();
      Fragment fragment = fm.findFragmentByTag(getFragmentTag());

      if (fragment != null) {
        fm.beginTransaction().remove(fragment).commit();
      }
    }
  }

  private static String makeFragmentTag(int id) {
    return "FragmentScene:" + id;
  }
}
