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

package com.hippo.stage.lint;

/*
 * Created by Hippo on 5/6/2017.
 */

import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import java.util.Collections;
import java.util.List;

public class SceneDetector extends Detector implements Detector.JavaPsiScanner {

  private static final String CLASS_SCENE = "com.hippo.stage.Scene";

  public static final Issue ISSUE = Issue.create(
      "ValidScene",
      "Scene not instantiatable",
      "Non-abstract Controller instances must be public, static "
          + "and have a no-parameters constructor in order for being recreated.",
      Category.CORRECTNESS,
      8,
      Severity.FATAL,
      new Implementation(SceneDetector.class, Scope.JAVA_FILE_SCOPE)
  );

  public SceneDetector() {}

  @Override
  public List<String> applicableSuperClasses() {
    return Collections.singletonList(CLASS_SCENE);
  }

  @Override
  public void checkClass(JavaContext context, PsiClass declaration) {
    final JavaEvaluator evaluator = context.getEvaluator();
    if (evaluator.isAbstract(declaration)) {
      return;
    }

    if (!evaluator.isPublic(declaration)) {
      context.report(ISSUE, declaration, context.getLocation(declaration),
          "This Scene class should be public");
      return;
    }

    if (declaration.getContainingClass() != null && !evaluator.isStatic(declaration)) {
      context.report(ISSUE, declaration, context.getLocation(declaration),
          "This Scene inner class should be static");
      return;
    }

    PsiMethod[] constructors = declaration.getConstructors();
    // No constructor means it has default constructor
    boolean hasNoParametersConstructor = constructors.length == 0;
    if (!hasNoParametersConstructor) {
      // Find no-parameter constructor
      for (PsiMethod constructor : constructors) {
        if (evaluator.isPublic(constructor)) {
          PsiParameter[] parameters = constructor.getParameterList().getParameters();
          if (parameters.length == 0) {
            hasNoParametersConstructor = true;
            break;
          }
        }
      }
    }

    if (!hasNoParametersConstructor) {
      context.report(ISSUE, declaration, context.getLocation(declaration),
          "This Scene needs to have a no-parameters constructor");
    }
  }
}
