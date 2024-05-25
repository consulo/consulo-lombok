package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiTypesUtil;
import com.intellij.java.language.psi.util.PsiUtil;
import com.intellij.java.language.psi.util.TypeConversionUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiNamedElement;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiElementUtil;
import jakarta.annotation.Nonnull;

import java.util.*;

/**
 * Handler for Delegate annotation processing, for fields and for methods
 */
public final class DelegateHandler {

  public static boolean validate(@Nonnull PsiModifierListOwner psiModifierListOwner,
                                 @Nonnull PsiType psiType,
                                 @Nonnull PsiAnnotation psiAnnotation,
                                 @Nonnull ProblemSink problemSink) {
    boolean result = true;

    if (psiModifierListOwner.hasModifierProperty(PsiModifier.STATIC)) {
      problemSink.addErrorMessage("inspection.message.delegate.legal.only.on.instance.fields");
      result = false;
    }

    final Collection<PsiType> types = collectDelegateTypes(psiAnnotation, psiType);
    result &= validateTypes(types, problemSink);

    final Collection<PsiType> excludes = collectExcludeTypes(psiAnnotation);
    result &= validateTypes(excludes, problemSink);

    return result;
  }

  private static Collection<PsiType> collectDelegateTypes(PsiAnnotation psiAnnotation, PsiType psiType) {
    Collection<PsiType> types = PsiAnnotationUtil.getAnnotationValues(psiAnnotation, "types", PsiType.class);
    if (types.isEmpty()) {
      types = Collections.singletonList(psiType);
    }
    return types;
  }

  private static boolean validateTypes(Collection<PsiType> psiTypes, ProblemSink problemSink) {
    boolean result = true;
    for (PsiType psiType : psiTypes) {
      if (!checkConcreteClass(psiType)) {
        problemSink.addErrorMessage("inspection.message.delegate.can.only.use.concrete.class.types", psiType.getCanonicalText());
        result = false;
      }
      else {
        result &= validateRecursion(psiType, problemSink);
      }
    }
    return result;
  }

  private static boolean validateRecursion(PsiType psiType, ProblemSink problemSink) {
    final PsiClass psiClass = PsiTypesUtil.getPsiClass(psiType);
    if (null != psiClass) {
      final DelegateAnnotationElementVisitor delegateAnnotationElementVisitor = new DelegateAnnotationElementVisitor(psiType, problemSink);
      psiClass.acceptChildren(delegateAnnotationElementVisitor);
      return delegateAnnotationElementVisitor.isValid();
    }
    return true;
  }


  private static boolean checkConcreteClass(@Nonnull PsiType psiType) {
    if (psiType instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)psiType).resolve();
      return !(psiClass instanceof PsiTypeParameter);
    }
    return false;
  }

  private static Collection<PsiType> collectExcludeTypes(PsiAnnotation psiAnnotation) {
    return PsiAnnotationUtil.getAnnotationValues(psiAnnotation, "excludes", PsiType.class);
  }

  public static <T extends PsiMember & PsiNamedElement> void generateElements(@Nonnull T psiElement,
                                                                              @Nonnull PsiType psiElementType,
                                                                              @Nonnull PsiAnnotation psiAnnotation,
                                                                              @Nonnull List<? super PsiElement> target) {
    final PsiManager manager = psiElement.getContainingFile().getManager();

    final Collection<Pair<PsiMethod, PsiSubstitutor>> includesMethods = new LinkedHashSet<>();

    final Collection<PsiType> types = collectDelegateTypes(psiAnnotation, psiElementType);
    addMethodsOfTypes(types, includesMethods);

    final Collection<Pair<PsiMethod, PsiSubstitutor>> excludeMethods = new LinkedHashSet<>();
    PsiClassType javaLangObjectType = PsiType.getJavaLangObject(manager, psiElement.getResolveScope());
    addMethodsOfType(javaLangObjectType, excludeMethods);

    final Collection<PsiType> excludes = collectExcludeTypes(psiAnnotation);
    addMethodsOfTypes(excludes, excludeMethods);

    final Collection<Pair<PsiMethod, PsiSubstitutor>> methodsToDelegate = findMethodsToDelegate(includesMethods, excludeMethods);
    if (!methodsToDelegate.isEmpty()) {
      final PsiClass psiClass = psiElement.getContainingClass();
      if (null != psiClass) {
        for (Pair<PsiMethod, PsiSubstitutor> pair : methodsToDelegate) {
          target.add(generateDelegateMethod(psiClass, psiElement, psiAnnotation, pair.getFirst(), pair.getSecond()));
        }
      }
    }
  }

  private static void addMethodsOfTypes(Collection<PsiType> types, Collection<Pair<PsiMethod, PsiSubstitutor>> includesMethods) {
    for (PsiType type : types) {
      addMethodsOfType(type, includesMethods);
    }
  }

  private static void addMethodsOfType(PsiType psiType, Collection<Pair<PsiMethod, PsiSubstitutor>> allMethods) {
    final PsiClassType.ClassResolveResult resolveResult = PsiUtil.resolveGenericsClassInType(psiType);

    final PsiClass psiClass = resolveResult.getElement();
    if (null != psiClass) {
      collectAllMethods(allMethods, psiClass, resolveResult.getSubstitutor());
    }
  }

  private static void collectAllMethods(Collection<Pair<PsiMethod, PsiSubstitutor>> allMethods,
                                        @Nonnull PsiClass psiStartClass,
                                        @Nonnull PsiSubstitutor classSubstitutor) {
    PsiClass psiClass = psiStartClass;
    while (null != psiClass) {
      PsiMethod[] psiMethods = psiClass.getMethods();
      for (PsiMethod psiMethod : psiMethods) {
        if (!psiMethod.isConstructor() && psiMethod.hasModifierProperty(PsiModifier.PUBLIC) && !psiMethod.hasModifierProperty(PsiModifier.STATIC)) {

          Pair<PsiMethod, PsiSubstitutor> newMethodSubstitutorPair = new Pair<>(psiMethod, classSubstitutor);

          boolean acceptMethod = true;
          for (Pair<PsiMethod, PsiSubstitutor> uniquePair : allMethods) {
            if (PsiElementUtil.methodMatches(newMethodSubstitutorPair, uniquePair)) {
              acceptMethod = false;
              break;
            }
          }
          if (acceptMethod) {
            allMethods.add(newMethodSubstitutorPair);
          }
        }
      }

      for (PsiClass interfaceClass : psiClass.getInterfaces()) {
        classSubstitutor = TypeConversionUtil.getSuperClassSubstitutor(interfaceClass, psiClass, classSubstitutor);

        collectAllMethods(allMethods, interfaceClass, classSubstitutor);
      }

      psiClass = psiClass.getSuperClass();
    }
  }

  private static Collection<Pair<PsiMethod, PsiSubstitutor>> findMethodsToDelegate(Collection<Pair<PsiMethod, PsiSubstitutor>> includesMethods,
                                                                                   Collection<Pair<PsiMethod, PsiSubstitutor>> excludeMethods) {
    final Collection<Pair<PsiMethod, PsiSubstitutor>> result = new ArrayList<>();
    for (Pair<PsiMethod, PsiSubstitutor> includesMethodPair : includesMethods) {
      boolean acceptMethod = true;
      for (Pair<PsiMethod, PsiSubstitutor> excludeMethodPair : excludeMethods) {
        if (PsiElementUtil.methodMatches(includesMethodPair, excludeMethodPair)) {
          acceptMethod = false;
          break;
        }
      }
      if (acceptMethod) {
        result.add(includesMethodPair);
      }
    }
    return result;
  }

  @Nonnull
  private static <T extends PsiModifierListOwner & PsiNamedElement> PsiMethod generateDelegateMethod(@Nonnull PsiClass psiClass,
                                                                                                     @Nonnull T psiElement,
                                                                                                     @Nonnull PsiAnnotation psiAnnotation,
                                                                                                     @Nonnull PsiMethod psiMethod,
                                                                                                     @Nonnull PsiSubstitutor psiSubstitutor) {
    final PsiType returnType = psiSubstitutor.substitute(psiMethod.getReturnType());

    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiClass.getManager(), psiMethod.getName())
      .withModifier(PsiModifier.PUBLIC)
      .withMethodReturnType(returnType)
      .withContainingClass(psiClass)
      //Have to go to original method, or some refactoring action will not work (like extract parameter oder change signature)
      .withNavigationElement(psiMethod);

    for (PsiTypeParameter typeParameter : psiMethod.getTypeParameters()) {
      methodBuilder.withTypeParameter(typeParameter);
    }

    final PsiReferenceList throwsList = psiMethod.getThrowsList();
    for (PsiClassType psiClassType : throwsList.getReferencedTypes()) {
      methodBuilder.withException(psiClassType);
    }

    final PsiParameterList parameterList = psiMethod.getParameterList();

    final PsiParameter[] psiParameters = parameterList.getParameters();
    for (int parameterIndex = 0; parameterIndex < psiParameters.length; parameterIndex++) {
      final PsiParameter psiParameter = psiParameters[parameterIndex];
      final PsiType psiParameterType = psiSubstitutor.substitute(psiParameter.getType());
      final String generatedParameterName = StringUtil.defaultIfEmpty(psiParameter.getName(), "p" + parameterIndex);
      methodBuilder.withParameter(generatedParameterName, psiParameterType);
    }

    final String codeBlockText = createCodeBlockText(psiElement, psiMethod, returnType, psiParameters);
    methodBuilder.withBodyText(codeBlockText);

    return methodBuilder;
  }

  @Nonnull
  private static <T extends PsiModifierListOwner & PsiNamedElement> String createCodeBlockText(@Nonnull T psiElement,
                                                                                               @Nonnull PsiMethod psiMethod,
                                                                                               @Nonnull PsiType returnType,
                                                                                               @Nonnull PsiParameter[] psiParameters) {
    final String blockText;
    final StringBuilder paramString = new StringBuilder();

    for (int parameterIndex = 0; parameterIndex < psiParameters.length; parameterIndex++) {
      final PsiParameter psiParameter = psiParameters[parameterIndex];
      final String generatedParameterName = StringUtil.defaultIfEmpty(psiParameter.getName(), "p" + parameterIndex);
      paramString.append(generatedParameterName).append(',');
    }

    if (paramString.length() > 0) {
      paramString.deleteCharAt(paramString.length() - 1);
    }

    final boolean isMethodCall = psiElement instanceof PsiMethod;
    blockText = String.format("%sthis.%s%s.%s(%s);",
                              PsiTypes.voidType().equals(returnType) ? "" : "return ",
                              psiElement.getName(),
                              isMethodCall ? "()" : "",
                              psiMethod.getName(),
                              paramString);
    return blockText;
  }

  private static class DelegateAnnotationElementVisitor extends JavaElementVisitor {
    private final PsiType psiType;
    private final ProblemSink builder;
    private boolean valid;

    DelegateAnnotationElementVisitor(PsiType psiType, ProblemSink builder) {
      this.psiType = psiType;
      this.builder = builder;
      this.valid = true;
    }

    @Override
    public void visitField(@Nonnull PsiField psiField) {
      checkModifierListOwner(psiField);
    }

    @Override
    public void visitMethod(@Nonnull PsiMethod psiMethod) {
      checkModifierListOwner(psiMethod);
    }

    private void checkModifierListOwner(PsiModifierListOwner modifierListOwner) {
      if (PsiAnnotationSearchUtil.isAnnotatedWith(modifierListOwner, LombokClassNames.DELEGATE, LombokClassNames.EXPERIMENTAL_DELEGATE)) {
        builder.addErrorMessage("inspection.message.delegate.does.not.support.recursion.delegating",
                                ((PsiMember)modifierListOwner).getName(),
                                psiType.getPresentableText());
        valid = false;
      }
    }

    public boolean isValid() {
      return valid;
    }
  }
}
