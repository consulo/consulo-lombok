// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.lombok.localize.LombokLocalize;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import static consulo.util.lang.ObjectUtil.tryCast;

@ExtensionImpl
public class LombokGetterMayBeUsedInspection extends LombokGetterOrSetterMayBeUsedInspection {
    @Override
    @Nonnull
    protected String getTagName() {
        return "return";
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "LombokGetterMayBeUsed";
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LombokLocalize.inspectionLombokGetterMayBeUsedDisplayName();
    }

    @Override
    @Nonnull
    protected String getJavaDocMethodMarkup() {
        return "GETTER";
    }

    @Override
    @Nonnull
    protected String getAnnotationName() {
        return LombokClassNames.GETTER;
    }

    @Nonnull
    @Override
    protected LocalizeValue getFieldErrorMessage(String fieldName) {
        return LombokLocalize.inspectionLombokGetterMayBeUsedDisplayFieldMessage(fieldName);
    }

    @Nonnull
    @Override
    protected LocalizeValue getClassErrorMessage(String className) {
        return LombokLocalize.inspectionLombokGetterMayBeUsedDisplayClassMessage(className);
    }

    @Override
    @RequiredReadAction
    protected boolean processMethod(
        @Nonnull PsiMethod method,
        @Nonnull List<Pair<PsiField, PsiMethod>> instanceCandidates,
        @Nonnull List<Pair<PsiField, PsiMethod>> staticCandidates
    ) {
        final PsiType returnType = method.getReturnType();
        if (!method.isPublic()
            || method.isConstructor()
            || method.hasParameters()
            || method.getThrowsTypes().length != 0
            || method.isFinal()
            || method.isAbstract()
            || method.hasModifierProperty(PsiModifier.SYNCHRONIZED)
            || method.hasModifierProperty(PsiModifier.NATIVE)
            || method.hasModifierProperty(PsiModifier.STRICTFP)
            || method.getAnnotations().length != 0
            || PsiTypes.voidType().equals(returnType)
            || returnType == null
            || returnType.getAnnotations().length != 0
            || !method.isWritable()) {
            return false;
        }
        final String methodName = method.getName();
        final boolean isBooleanType = PsiTypes.booleanType().equals(returnType);
        if (isBooleanType ? !methodName.startsWith("is") : !methodName.startsWith("get")) {
            return false;
        }

        final String fieldName = StringUtil.getPropertyName(methodName);
        if (StringUtil.isEmpty(fieldName)) {
            return false;
        }

        if (method.getBody() == null) {
            return false;
        }
        final PsiStatement[] methodStatements =
            Arrays.stream(method.getBody().getStatements()).filter(e -> !(e instanceof PsiEmptyStatement)).toArray(PsiStatement[]::new);
        if (methodStatements.length != 1) {
            return false;
        }
        final PsiReturnStatement returnStatement = tryCast(methodStatements[0], PsiReturnStatement.class);
        if (returnStatement == null) {
            return false;
        }
        final PsiReferenceExpression targetRef = tryCast(
            PsiUtil.skipParenthesizedExprDown(returnStatement.getReturnValue()), PsiReferenceExpression.class);
        if (targetRef == null) {
            return false;
        }
        final @Nullable PsiExpression qualifier = targetRef.getQualifierExpression();
        final @Nullable PsiThisExpression thisExpression = tryCast(qualifier, PsiThisExpression.class);
        final PsiClass psiClass = PsiTreeUtil.getParentOfType(method, PsiClass.class);
        if (psiClass == null) {
            return false;
        }
        if (qualifier != null) {
            if (thisExpression == null) {
                return false;
            }
            else if (thisExpression.getQualifier() != null) {
                if (!thisExpression.getQualifier().isReferenceTo(psiClass)) {
                    return false;
                }
            }
        }
        final @Nullable String fieldIdentifier = targetRef.getReferenceName();
        if (!fieldName.equals(fieldIdentifier) && !StringUtil.capitalize(fieldName).equals(fieldIdentifier)) {
            return false;
        }

        final boolean isMethodStatic = method.isStatic();
        final PsiField field = psiClass.findFieldByName(fieldIdentifier, false);
        if (field == null
            || !field.isWritable()
            || isMethodStatic != field.isStatic()
            || !field.getType().equals(returnType)) {
            return false;
        }
        if (isMethodStatic) {
            staticCandidates.add(Pair.pair(field, method));
        }
        else {
            instanceCandidates.add(Pair.pair(field, method));
        }
        return true;
    }

    @Nonnull
    @Override
    protected LocalizeValue getFixName(String text) {
        return LombokLocalize.inspectionLombokGetterMayBeUsedDisplayFixName(text);
    }
}