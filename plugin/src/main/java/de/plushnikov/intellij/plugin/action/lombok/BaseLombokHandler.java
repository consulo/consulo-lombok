package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.java.impl.codeInsight.generation.OverrideImplementUtil;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.codeEditor.Editor;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.editor.util.LanguageUndoUtil;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BaseLombokHandler implements CodeInsightActionHandler {

  @Override
  public void invoke(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
    if (file.isWritable()) {
      PsiClass psiClass = OverrideImplementUtil.getContextClass(project, editor, file, false);
      if (null != psiClass) {
        processClass(psiClass);

        LanguageUndoUtil.markPsiFileForUndo(file);
      }
    }
  }

  protected abstract void processClass(@Nonnull PsiClass psiClass);

  protected static void processIntern(@Nonnull Map<PsiField, PsiMethod> fieldMethodMap,
                                      @Nonnull PsiClass psiClass, String annotationClassName) {
    if (fieldMethodMap.isEmpty()) {
      return;
    }

    final PsiMethod firstPropertyMethod = fieldMethodMap.values().iterator().next();

    final boolean useAnnotationOnClass = haveAllMethodsSameAccessLevel(fieldMethodMap.values()) &&
      isNotAnnotatedWithOrSameAccessLevelAs(psiClass, firstPropertyMethod, annotationClassName);

    if (useAnnotationOnClass) {
      addAnnotation(psiClass, firstPropertyMethod, annotationClassName);
    }

    for (Map.Entry<PsiField, PsiMethod> fieldMethodEntry : fieldMethodMap.entrySet()) {
      final PsiField propertyField = fieldMethodEntry.getKey();
      final PsiMethod propertyMethod = fieldMethodEntry.getValue();

      if (null != propertyField) {
        boolean isStatic = propertyField.hasModifierProperty(PsiModifier.STATIC);
        if (isStatic || !useAnnotationOnClass) {
          addAnnotation(propertyField, propertyMethod, annotationClassName);
        }

        // Move all annotations to field declaration
        for (PsiAnnotation psiMethodAnnotation : propertyMethod.getModifierList().getAnnotations()) {
          psiClass.addBefore(psiMethodAnnotation, propertyField);
        }

        propertyMethod.delete();
      }
    }
  }

  private static boolean isNotAnnotatedWithOrSameAccessLevelAs(PsiClass psiClass,
                                                               PsiMethod firstPropertyMethod,
                                                               String annotationClassName) {
    final PsiAnnotation presentAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, annotationClassName);
    if (null != presentAnnotation) {

      final String presentAccessModifier = LombokProcessorUtil.getMethodModifier(presentAnnotation);
      final String currentAccessModifier = PsiUtil.getAccessModifier(PsiUtil.getAccessLevel(firstPropertyMethod.getModifierList()));

      return presentAccessModifier != null && presentAccessModifier.equals(currentAccessModifier);
    }
    return true;
  }

  private static boolean haveAllMethodsSameAccessLevel(Collection<PsiMethod> psiMethods) {
    final Set<Integer> accessLevelSet = new HashSet<>();
    for (PsiMethod psiMethod : psiMethods) {
      accessLevelSet.add(PsiUtil.getAccessLevel(psiMethod.getModifierList()));
    }
    return accessLevelSet.size() <= 1;
  }

  private static void addAnnotation(@Nonnull PsiModifierListOwner targetElement, @Nonnull PsiModifierListOwner sourceElement,
                                    String annotationClassName) {
    final PsiAnnotation newPsiAnnotation = LombokProcessorUtil.createAnnotationWithAccessLevel(sourceElement, annotationClassName);

    addAnnotation(targetElement, newPsiAnnotation, annotationClassName);
  }

  protected static void addAnnotation(@Nonnull PsiClass targetElement,
                                      String annotationClassName) {
    final PsiAnnotation newPsiAnnotation = PsiAnnotationUtil.createPsiAnnotation(targetElement, annotationClassName);

    addAnnotation(targetElement, newPsiAnnotation, annotationClassName);
  }

  private static void addAnnotation(@Nonnull PsiModifierListOwner targetElement,
                                    @Nonnull PsiAnnotation newPsiAnnotation,
                                    String annotationClassName) {
    final PsiAnnotation presentAnnotation = PsiAnnotationSearchUtil.findAnnotation(targetElement, annotationClassName);

    final Project project = targetElement.getProject();
    final JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
    javaCodeStyleManager.shortenClassReferences(newPsiAnnotation);

    if (null == presentAnnotation) {
      PsiModifierList modifierList = targetElement.getModifierList();
      if (null != modifierList) {
        modifierList.addAfter(newPsiAnnotation, null);
      }
    } else {
      presentAnnotation.setDeclaredAttributeValue(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME,
        newPsiAnnotation.findDeclaredAttributeValue(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME));
    }
  }

  protected void removeDefaultAnnotation(@Nonnull PsiModifierListOwner targetElement, String annotationClassName) {
    final PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(targetElement, annotationClassName);
    if (null != psiAnnotation) {
      boolean hasOnlyDefaultValues = true;

      final PsiAnnotationParameterList psiAnnotationParameterList = psiAnnotation.getParameterList();
      for (PsiNameValuePair nameValuePair : psiAnnotationParameterList.getAttributes()) {
        if (null != psiAnnotation.findDeclaredAttributeValue(nameValuePair.getName())) {
          hasOnlyDefaultValues = false;
          break;
        }
      }

      if (hasOnlyDefaultValues) {
        psiAnnotation.delete();
      }
    }
  }

  @Nullable
  protected PsiMethod findPublicNonStaticMethod(@Nonnull PsiClass psiClass, @Nonnull String methodName, @Nonnull PsiType returnType, PsiType... params) {
    final PsiMethod[] toStringMethods = psiClass.findMethodsByName(methodName, false);
    for (PsiMethod method : toStringMethods) {
      if (method.hasModifierProperty(PsiModifier.PUBLIC) &&
        !method.hasModifierProperty(PsiModifier.STATIC) &&
        returnType.equals(method.getReturnType())) {

        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] psiParameters = parameterList.getParameters();
        final int paramsCount = params.length;

        if (psiParameters.length == paramsCount) {
          boolean allParametersFound = true;
          for (int i = 0; i < paramsCount; i++) {

            if (!psiParameters[i].getType().equals(params[i])) {
              allParametersFound = false;
              break;
            }
          }
          if (allParametersFound) {
            return method;
          }
        }
      }
    }
    return null;
  }
}
