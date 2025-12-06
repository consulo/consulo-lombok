package de.plushnikov.intellij.plugin.handler;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiAnnotationParameterList;
import com.intellij.java.language.psi.PsiNameValuePair;
import consulo.annotation.access.RequiredReadAction;
import consulo.java.language.impl.localize.JavaErrorLocalize;
import consulo.java.language.localize.JavaCompilationErrorLocalize;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

public final class OnXAnnotationHandler {
    private static final Pattern UNDERSCORES = Pattern.compile("__*");
    private static final Pattern CANNOT_RESOLVE_SYMBOL_UNDERSCORES_MESSAGE =
        Pattern.compile(JavaCompilationErrorLocalize.referenceUnresolved("__*").get());
    private static final Pattern CANNOT_RESOLVE_METHOD_UNDERSCORES_MESSAGE =
        Pattern.compile(JavaCompilationErrorLocalize.callUnresolved("(onMethod|onConstructor|onParam)_+").get());

    private static final LocalizeValue CANNOT_FIND_METHOD_VALUE_MESSAGE = JavaErrorLocalize.annotationMissingMethod("value");

    private static final Collection<String> ONXABLE_ANNOTATIONS = Arrays.asList(
        LombokClassNames.GETTER,
        LombokClassNames.SETTER,
        LombokClassNames.WITH,
        LombokClassNames.WITHER,
        LombokClassNames.NO_ARGS_CONSTRUCTOR,
        LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR,
        LombokClassNames.ALL_ARGS_CONSTRUCTOR,
        LombokClassNames.EQUALS_AND_HASHCODE
    );
    private static final Collection<String> ONX_PARAMETERS = Arrays.asList(
        "onConstructor",
        "onMethod",
        "onParam"
    );

    @RequiredReadAction
    public static boolean isOnXParameterAnnotation(HighlightInfo highlightInfo, PsiFile file) {
        LocalizeValue description = highlightInfo.getDescription();
        if (!(JavaCompilationErrorLocalize.annotationTypeExpected().equals(description)
            || CANNOT_RESOLVE_SYMBOL_UNDERSCORES_MESSAGE.matcher(description.get()).matches()
            || CANNOT_RESOLVE_METHOD_UNDERSCORES_MESSAGE.matcher(description.get()).matches())) {
            return false;
        }

        PsiElement highlightedElement = file.findElementAt(highlightInfo.getStartOffset());

        PsiNameValuePair nameValuePair = findContainingNameValuePair(highlightedElement);
        if (nameValuePair == null || !(nameValuePair.getContext() instanceof PsiAnnotationParameterList)) {
            return false;
        }

        String parameterName = nameValuePair.getName();
        if (null != parameterName && parameterName.contains("_")) {
            parameterName = parameterName.substring(0, parameterName.indexOf('_'));
        }
        if (!ONX_PARAMETERS.contains(parameterName)) {
            return false;
        }

      return nameValuePair.getContext().getContext() instanceof PsiAnnotation containingAnnotation
          && ONXABLE_ANNOTATIONS.contains(containingAnnotation.getQualifiedName());
    }

    @RequiredReadAction
    public static boolean isOnXParameterValue(HighlightInfo highlightInfo, PsiFile file) {
        if (!CANNOT_FIND_METHOD_VALUE_MESSAGE.equals(highlightInfo.getDescription())) {
            return false;
        }

        PsiElement highlightedElement = file.findElementAt(highlightInfo.getStartOffset());
        PsiNameValuePair nameValuePair = findContainingNameValuePair(highlightedElement);
        if (nameValuePair == null || !(nameValuePair.getContext() instanceof PsiAnnotationParameterList)) {
            return false;
        }

        PsiElement leftSibling = nameValuePair.getContext().getPrevSibling();
        return leftSibling != null && UNDERSCORES.matcher(StringUtil.notNullize(leftSibling.getText())).matches();
    }

    private static PsiNameValuePair findContainingNameValuePair(PsiElement highlightedElement) {
        PsiElement nameValuePair = highlightedElement;
        while (!(nameValuePair == null || nameValuePair instanceof PsiNameValuePair)) {
            nameValuePair = nameValuePair.getContext();
        }

        return (PsiNameValuePair) nameValuePair;
    }
}
