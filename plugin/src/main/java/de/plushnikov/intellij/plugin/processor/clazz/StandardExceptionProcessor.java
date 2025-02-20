package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.InheritanceUtil;
import com.intellij.java.language.psi.util.PsiTypesUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ExtensionImpl(id = "StandardExceptionProcessor", order = "after UtilityClassProcessor")
public class StandardExceptionProcessor extends AbstractClassProcessor {

  public StandardExceptionProcessor() {
    super(PsiMethod.class, LombokClassNames.STANDARD_EXCEPTION);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return Collections.singleton(getConstructorName(psiClass));
  }

  private static String getConstructorName(@Nonnull PsiClass psiClass) {
    return StringUtil.notNullize(psiClass.getName());
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation,
                             @Nonnull PsiClass psiClass,
                             @Nonnull ProblemSink builder) {
    if (checkWrongType(psiClass)) {
      builder.addErrorMessage("inspection.message.standardexception.class.only.supported.on.class");
      return false;
    }
    if (checkWrongInheritorOfThrowable(psiClass)) {
      builder.addErrorMessage("inspection.message.standardexception.should.extend.throwable");
      return false;
    }
    if (checkWrongAccessVisibility(psiAnnotation)) {
      builder.addErrorMessage("inspection.message.standardexception.accesslevel.none.not.valid");
      //log error but continue
    }
    return true;
  }

  private static boolean checkWrongType(@Nonnull PsiClass psiClass) {
    return psiClass.isInterface() || psiClass.isAnnotationType() || psiClass.isEnum() || psiClass.isRecord();
  }

  private static boolean checkWrongInheritorOfThrowable(@Nonnull PsiClass psiClass) {
    return !InheritanceUtil.isInheritor(psiClass, CommonClassNames.JAVA_LANG_THROWABLE);
  }

  private static boolean checkWrongAccessVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    return null == LombokProcessorUtil.getAccessVisibility(psiAnnotation);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    final PsiManager psiManager = psiClass.getManager();
    final Collection<PsiMethod> existedConstructors = PsiClassUtil.collectClassConstructorIntern(psiClass);
    final String accessVisibility = getAccessVisibility(psiAnnotation);

    // default constructor
    if (noConstructorWithParamsOfTypesDefined(existedConstructors)) {
      target.add(createConstructor(psiClass, psiAnnotation, psiManager, accessVisibility).withBodyText("this(null, null);"));
    }

    final GlobalSearchScope psiClassResolveScope = psiClass.getResolveScope();
    final PsiClassType javaLangStringType = PsiType.getJavaLangString(psiManager, psiClassResolveScope);
    final PsiClassType javaLangThrowableType = PsiType.getJavaLangThrowable(psiManager, psiClassResolveScope);
    final boolean addConstructorProperties =
      ConfigDiscovery.getInstance().getBooleanLombokConfigProperty(ConfigKey.ANYCONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES, psiClass);

    // message constructor
    if (noConstructorWithParamsOfTypesDefined(existedConstructors, javaLangStringType)) {
      final LombokLightMethodBuilder messageConstructor = createConstructor(psiClass, psiAnnotation, psiManager, accessVisibility)
        .withFinalParameter("message", javaLangStringType)
        .withBodyText("this(message, null);");
      if (addConstructorProperties) {
        messageConstructor.withAnnotation("java.beans.ConstructorProperties(\"message\")");
      }
      target.add(messageConstructor);
    }

    // cause constructor
    if (noConstructorWithParamsOfTypesDefined(existedConstructors, javaLangThrowableType)) {
      final LombokLightMethodBuilder causeConstructor = createConstructor(psiClass, psiAnnotation, psiManager, accessVisibility)
        .withFinalParameter("cause", javaLangThrowableType)
        .withBodyText("this(cause != null ? cause.getMessage() : null, cause);");
      if (addConstructorProperties) {
        causeConstructor.withAnnotation("java.beans.ConstructorProperties(\"cause\")");
      }
      target.add(causeConstructor);
    }

    // message and cause constructor
    if (noConstructorWithParamsOfTypesDefined(existedConstructors, javaLangStringType, javaLangThrowableType)) {
      final LombokLightMethodBuilder messageCauseConstructor = createConstructor(psiClass, psiAnnotation, psiManager, accessVisibility)
        .withFinalParameter("message", javaLangStringType)
        .withFinalParameter("cause", javaLangThrowableType)
        .withBodyText("super(message);\n" +
                      "if (cause != null) super.initCause(cause);");
      if (addConstructorProperties) {
        messageCauseConstructor.withAnnotation("java.beans.ConstructorProperties({\"message\",\"cause\"})");
      }
      target.add(messageCauseConstructor);
    }
  }

  @PsiModifier.ModifierConstant
  private static String getAccessVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    String accessVisibility = LombokProcessorUtil.getAccessVisibility(psiAnnotation);
    if (null == accessVisibility) {
      accessVisibility = PsiModifier.PUBLIC;
    }
    return accessVisibility;
  }

  private static boolean noConstructorWithParamsOfTypesDefined(Collection<PsiMethod> existedConstructors, PsiClassType... classTypes) {
    return !ContainerUtil.exists(existedConstructors, method -> {
      final PsiParameterList parameterList = method.getParameterList();
      if (parameterList.getParametersCount() != classTypes.length) {
        return false;
      }

      int paramIndex = 0;
      for (PsiClassType classType : classTypes) {
        if (!PsiTypesUtil.compareTypes(parameterList.getParameter(paramIndex).getType(), classType, true)) {
          return false;
        }
        paramIndex++;
      }

      return true;
    });
  }

  private static LombokLightMethodBuilder createConstructor(@Nonnull PsiClass psiClass,
                                                            @Nonnull PsiAnnotation psiAnnotation,
                                                            @Nonnull PsiManager psiManager,
                                                            @PsiModifier.ModifierConstant String accessVisibility) {
    return new LombokLightMethodBuilder(psiManager, getConstructorName(psiClass))
      .withConstructor(true)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(accessVisibility);
  }
}
