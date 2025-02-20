package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiJavaCodeReferenceElement;
import consulo.application.Application;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorCacheData;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class LombokProcessorManager {
  private static Set<String> getSupportedShortNames(Application application) {
    return ProcessorCacheData.getSupportedClassNames(application);
  }

  @Nonnull
  public static Collection<Processor> getProcessors(@Nonnull Class<? extends PsiElement> supportedClass) {
    Application application = Application.get();
    return ProcessorCacheData.getWithCache(application,
                                           "bySupportedClass_" + supportedClass.getName(),
                                           (processors) -> ContainerUtil.filter(processors, p -> p.isSupportedClass(supportedClass))
    );
  }

  @Nonnull
  public static Collection<Processor> getProcessors(@Nonnull PsiAnnotation psiAnnotation) {
    PsiJavaCodeReferenceElement nameReferenceElement = psiAnnotation.getNameReferenceElement();
    if (nameReferenceElement == null) {
      return Collections.emptyList();
    }

    Application application = psiAnnotation.getApplication();

    String referenceName = nameReferenceElement.getReferenceName();
    if (referenceName == null || !getSupportedShortNames(application).contains(referenceName)) {
      return Collections.emptyList();
    }
    final String qualifiedName = psiAnnotation.getQualifiedName();
    if (StringUtil.isEmpty(qualifiedName) || !qualifiedName.contains("lombok")) {
      return Collections.emptyList();
    }
    return ProcessorCacheData.getWithCache(application,
                                           "byAnnotationFQN_" + qualifiedName,
                                           (processors) -> ContainerUtil.filter(processors, p -> p.isSupportedAnnotationFQN(qualifiedName))
    );
  }
}
