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
 * Created by Hippo on 5/5/2017.
 */

import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collections;
import java.util.List;

public class BackHandlerDetector extends Detector implements Detector.UastScanner {

  private static final String CLASS_DIRECTOR = "com.hippo.stage.Director";
  private static final String CLASS_DIRECTOR_BACK_HANDLER = CLASS_DIRECTOR + ".BackHandler";
  private static final String CLASS_STAGE = "com.hippo.stage.Stage";
  private static final String CLASS_STAGE_BACK_HANDLER = CLASS_STAGE + ".BackHandler";

  public static final Issue ISSUE = Issue.create(
      "CallOnHandleBack",
      "Call Stage/Scene.onHandleBack() instead of Stage/Scene.handleBack()",
      "Stage/Scene.handleBack() checks its BackHandler and calls BackHandler.handleBack(), "
          + "that causes a loop.",
      Category.CORRECTNESS,
      8,
      Severity.FATAL,
      new Implementation(BackHandlerDetector.class, Scope.JAVA_FILE_SCOPE)
  );

  public BackHandlerDetector() {}

  @Override
  public List<String> getApplicableMethodNames() {
    return Collections.singletonList("handleBack");
  }

  @Override
  public void visitMethod(JavaContext context, JavaElementVisitor visitor,
      PsiMethodCallExpression call, PsiMethod method) {
    JavaEvaluator evaluator = context.getEvaluator();
    if (evaluator.getParameterCount(method) == 0) {
      PsiMethod parent = PsiTreeUtil.getParentOfType(call, PsiMethod.class);
      if (parent != null) {
        if (evaluator.isMemberInClass(method, CLASS_DIRECTOR) &&
            evaluator.isMemberInSubClassOf(parent, CLASS_DIRECTOR_BACK_HANDLER, false)) {
          context.report(ISSUE, call, context.getLocation(call),
              "Can't call Stage.handleBack() in Stage.BackHandler.handleBack()");
        } else if (evaluator.isMemberInClass(method, CLASS_STAGE) &&
            evaluator.isMemberInSubClassOf(parent, CLASS_STAGE_BACK_HANDLER, false)) {
          context.report(ISSUE, call, context.getLocation(call),
              "Can't call Director.handleBack() in Director.BackHandler.handleBack()");
        }
      }
    }
  }
}
