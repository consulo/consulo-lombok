/*
 * Copyright 2013 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.lombok.processors;

import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;

/**
 * @author VISTALL
 * @since 18:44/29.03.13
 */
public abstract class LombokAnnotationOwnerProcessor<E extends PsiModifierListOwner> implements LombokProcessor {
  protected final String myAnnotationClass;

  public LombokAnnotationOwnerProcessor(@Nonnull String annotationClass) {
    myAnnotationClass = annotationClass;
  }

  @Override
  public void process(@Nonnull PsiClass element, @Nonnull List<PsiElement> result) {
    final E[] elements = getElements(element);
    if(elements.length == 0) {
      return;
    }

    for(E e : elements) {
      if(AnnotationUtil.findAnnotation(e, myAnnotationClass) != null && canBeProcessed(e)) {
        processElement(element, e, result);
      }
    }
  }

  @Override
  public void collectInspections(@Nonnull PsiClass element, @Nonnull ProblemsHolder problemsHolder) {
    final E[] elements = getElements(element);
    if(elements.length == 0) {
      return;
    }

    for(E e : elements) {
      if(AnnotationUtil.findAnnotation(e, myAnnotationClass) != null && !canBeProcessed(e)) {
        collectInspectionsForElement(e, problemsHolder);
      }
    }
  }

  public abstract void processElement(@Nonnull PsiClass parent, @Nonnull E e, @Nonnull List<PsiElement> result);

  public boolean canBeProcessed(@Nonnull E e) {
    return true;
  }

  public void collectInspectionsForElement(@Nonnull E element, @Nonnull ProblemsHolder problemsHolder) {

  }

  @Nonnull
  protected abstract E[] getElements(@Nonnull PsiClass psiClass);

  @Nonnull
  public PsiAnnotation getAffectedAnnotation(PsiModifierListOwner owner) {
    return AnnotationUtil.findAnnotation(owner, myAnnotationClass);
  }
}
