// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.java.language.psi.javadoc.PsiDocComment;
import com.intellij.java.language.psi.javadoc.PsiDocTag;
import com.intellij.java.language.psi.javadoc.PsiDocToken;
import com.siyeh.ig.psiutils.CommentTracker;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.project.Project;
import consulo.util.lang.Pair;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class LombokGetterOrSetterMayBeUsedInspection extends LombokJavaInspectionBase {

  @Nonnull
  @Override
  protected PsiElementVisitor createVisitor(@Nonnull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new LombokGetterOrSetterMayBeUsedVisitor(holder, null);
  }

  private class LombokGetterOrSetterMayBeUsedVisitor extends JavaElementVisitor {
    private final @Nullable ProblemsHolder myHolder;

    private final @Nullable LombokGetterOrSetterMayBeUsedInspection.LombokGetterOrSetterMayBeUsedFix myLombokGetterOrSetterMayBeUsedFix;

    private LombokGetterOrSetterMayBeUsedVisitor(
      @Nullable ProblemsHolder holder,
      @Nullable LombokGetterOrSetterMayBeUsedInspection.LombokGetterOrSetterMayBeUsedFix lombokGetterOrSetterMayBeUsedFix
    ) {
      this.myHolder = holder;
      this.myLombokGetterOrSetterMayBeUsedFix = lombokGetterOrSetterMayBeUsedFix;
    }

    @Override
    public void visitJavaFile(@Nonnull PsiJavaFile psiJavaFile) {
    }

    @Override
    public void visitClass(@Nonnull PsiClass psiClass) {
      List<PsiField> annotatedFields = new ArrayList<>();
      List<Pair<PsiField, PsiMethod>> instanceCandidates = new ArrayList<>();
      List<Pair<PsiField, PsiMethod>> staticCandidates = new ArrayList<>();
      for (PsiMethod method : psiClass.getMethods()) {
        processMethod(method, instanceCandidates, staticCandidates);
      }
      boolean isLombokAnnotationAtClassLevel = true;
      for (PsiField field : psiClass.getFields()) {
        PsiAnnotation annotation = field.getAnnotation(getAnnotationName());
        if (annotation != null) {
          if (!annotation.getAttributes().isEmpty() || field.hasModifierProperty(PsiModifier.STATIC)) {
            isLombokAnnotationAtClassLevel = false;
          }
          else {
            annotatedFields.add(field);
          }
        }
        else if (!field.hasModifierProperty(PsiModifier.STATIC)) {
          boolean found = false;
          for (Pair<PsiField, PsiMethod> instanceCandidate : instanceCandidates) {
            if (field.equals(instanceCandidate.getFirst())) {
              found = true;
              break;
            }
          }
          isLombokAnnotationAtClassLevel = found;
        }

        if (!isLombokAnnotationAtClassLevel) {
          break;
        }
      }
      List<Pair<PsiField, PsiMethod>> allCandidates = new ArrayList<>(staticCandidates);
      if (isLombokAnnotationAtClassLevel && (!instanceCandidates.isEmpty() || !annotatedFields.isEmpty())) {
        warnOrFix(psiClass, instanceCandidates, annotatedFields);
      }
      else {
        allCandidates.addAll(instanceCandidates);
      }
      for (Pair<PsiField, PsiMethod> candidate : allCandidates) {
        warnOrFix(candidate.getFirst(), candidate.getSecond());
      }
    }

    public void visitMethodForFix(@Nonnull PsiMethod psiMethod) {
      List<Pair<PsiField, PsiMethod>> fieldsAndMethods = new ArrayList<>();
      if (!processMethod(psiMethod, fieldsAndMethods, fieldsAndMethods)) return;
      if (!fieldsAndMethods.isEmpty()) {
        final Pair<PsiField, PsiMethod> psiFieldPsiMethodPair = fieldsAndMethods.get(0);
        warnOrFix(psiFieldPsiMethodPair.getFirst(), psiFieldPsiMethodPair.getSecond());
      }
    }

    private void warnOrFix(
      @Nonnull PsiClass psiClass,
      @Nonnull List<Pair<PsiField, PsiMethod>> fieldsAndMethods,
      @Nonnull List<PsiField> annotatedFields
    ) {
      if (myHolder != null) {
        String className = psiClass.getName();
        final PsiIdentifier psiClassNameIdentifier = psiClass.getNameIdentifier();
        final LocalQuickFix fix = new LombokGetterOrSetterMayBeUsedFix(Objects.requireNonNull(className));
        myHolder.registerProblem(psiClass,
                                 getClassErrorMessage(className),
                                 ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                 psiClassNameIdentifier != null ? psiClassNameIdentifier.getTextRangeInParent() : psiClass.getTextRange(),
                                 fix);
      }
      else if (myLombokGetterOrSetterMayBeUsedFix != null) {
        myLombokGetterOrSetterMayBeUsedFix.effectivelyDoFix(psiClass, fieldsAndMethods, annotatedFields);
      }
    }

    private void warnOrFix(@Nonnull PsiField field, @Nonnull PsiMethod method) {
      if (myHolder != null) {
        String fieldName = field.getName();
        final LocalQuickFix fix = new LombokGetterOrSetterMayBeUsedFix(fieldName);
        myHolder.registerProblem(method,
                                 getFieldErrorMessage(fieldName), fix);
      }
      else if (myLombokGetterOrSetterMayBeUsedFix != null) {
        myLombokGetterOrSetterMayBeUsedFix.effectivelyDoFix(field, method);
      }
    }
  }

  private class LombokGetterOrSetterMayBeUsedFix implements LocalQuickFix {
    private final @Nonnull String myText;

    private LombokGetterOrSetterMayBeUsedFix(@Nonnull String text) {
      myText = text;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Nonnull
    @Override
    public String getName() {
      return getFixName(myText);
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @Nonnull
    @Override
    public String getFamilyName() {
      return getFixFamilyName();
    }

    @Override
    public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
      final PsiElement element = descriptor.getPsiElement();
      if (element instanceof PsiMethod) {
        new LombokGetterOrSetterMayBeUsedVisitor(null, this).visitMethodForFix((PsiMethod)element);
      }
      else if (element instanceof PsiClass) {
        new LombokGetterOrSetterMayBeUsedVisitor(null, this).visitClass((PsiClass)element);
      }
    }

    private void effectivelyDoFix(@Nonnull PsiField field, @Nonnull PsiMethod method) {
      if (!addLombokAnnotation(field)) return;
      removeMethodAndMoveJavaDoc(field, method);
    }

    public void effectivelyDoFix(@Nonnull PsiClass aClass, @Nonnull List<Pair<PsiField, PsiMethod>> fieldsAndMethods,
                                 @Nonnull List<PsiField> annotatedFields) {
      if (!addLombokAnnotation(aClass)) return;
      for (Pair<PsiField, PsiMethod> fieldAndMethod : fieldsAndMethods) {
        PsiField field = fieldAndMethod.getFirst();
        PsiMethod method = fieldAndMethod.getSecond();
        removeMethodAndMoveJavaDoc(field, method);
      }
      for (PsiField annotatedField : annotatedFields) {
        PsiAnnotation oldAnnotation = annotatedField.getAnnotation(getAnnotationName());
        if (oldAnnotation != null) {
          new CommentTracker().deleteAndRestoreComments(oldAnnotation);
        }
      }
    }

    private boolean addLombokAnnotation(@Nonnull PsiModifierListOwner fieldOrClass) {
      final PsiModifierList modifierList = fieldOrClass.getModifierList();
      if (modifierList == null) {
        return false;
      }
      Project project = fieldOrClass.getProject();
      final PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
      final PsiAnnotation annotation = factory.createAnnotationFromText("@" + getAnnotationName(), fieldOrClass);
      JavaCodeStyleManager.getInstance(project).shortenClassReferences(annotation);
      modifierList.addAfter(annotation, null);
      return true;
    }

    private void removeMethodAndMoveJavaDoc(@Nonnull PsiField field, @Nonnull PsiMethod method) {
      final PsiElementFactory factory = JavaPsiFacade.getElementFactory(field.getProject());
      CommentTracker tracker = new CommentTracker();
      PsiDocComment methodJavaDoc = method.getDocComment();
      if (methodJavaDoc != null) {
        tracker.text(methodJavaDoc);
        PsiDocComment fieldJavaDoc = field.getDocComment();
        List<String> methodJavaDocTokens = Arrays.stream(methodJavaDoc.getChildren())
                                                 .filter(e -> e instanceof PsiDocToken)
                                                 .map(PsiElement::getText)
                                                 .filter(text -> !text.matches("\\s*\\*\\s*"))
                                                 .toList();
        methodJavaDocTokens = methodJavaDocTokens.subList(1, methodJavaDocTokens.size() - 1);
        String javaDocMethodText = String.join("\n* ", methodJavaDocTokens);
        PsiDocTag[] methodTags = methodJavaDoc.findTagsByName(getTagName());
        if (fieldJavaDoc == null) {
          if (javaDocMethodText.isEmpty()) {
            fieldJavaDoc = factory.createDocCommentFromText("/**\n*/");
          }
          else {
            fieldJavaDoc =
              factory.createDocCommentFromText("/**\n* -- " + getJavaDocMethodMarkup() + " --\n* " + javaDocMethodText + "\n*/");
          }
          for (PsiDocTag methodTag : methodTags) {
            fieldJavaDoc.add(methodTag);
          }
          field.getParent().addBefore(fieldJavaDoc, field);
        }
        else {
          @Nonnull PsiElement[] fieldJavaDocChildren = Arrays.stream(fieldJavaDoc.getChildren())
                                                             .filter(e -> e instanceof PsiDocToken)
                                                             .toArray(PsiElement[]::new);
          @Nonnull PsiElement fieldJavaDocChild = fieldJavaDocChildren[fieldJavaDocChildren.length - 2];
          PsiDocComment newMethodJavaDoc =
            factory.createDocCommentFromText("/**\n* -- " + getJavaDocMethodMarkup() + " --\n* " + javaDocMethodText + "\n*/");
          PsiElement[] tokens = newMethodJavaDoc.getChildren();
          for (int i = tokens.length - 2; 0 < i; i--) {
            fieldJavaDoc.addAfter(tokens[i], fieldJavaDocChild);
          }
          for (PsiDocTag methodTag : methodTags) {
            fieldJavaDoc.add(methodTag);
          }
        }
        methodJavaDoc.delete();
      }
      tracker.delete(method);
      tracker.insertCommentsBefore(field);
    }
  }

  @Nonnull
  protected abstract String getTagName();

  @Nonnull
  protected abstract String getJavaDocMethodMarkup();

  @Nonnull
  protected abstract @NonNls String getAnnotationName();

  @Nonnull
  protected abstract @Nls String getFieldErrorMessage(String fieldName);

  @Nonnull
  protected abstract @Nls String getClassErrorMessage(String className);

  protected abstract boolean processMethod(
    @Nonnull PsiMethod method,
    @Nonnull List<Pair<PsiField, PsiMethod>> instanceCandidates,
    @Nonnull List<Pair<PsiField, PsiMethod>> staticCandidates
  );

  @Nonnull
  protected abstract @Nls String getFixName(String text);

  @Nonnull
  protected abstract @Nls String getFixFamilyName();
}
