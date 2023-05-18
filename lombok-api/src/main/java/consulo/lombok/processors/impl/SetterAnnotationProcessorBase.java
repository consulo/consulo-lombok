package consulo.lombok.processors.impl;

import com.intellij.java.analysis.impl.codeInspection.RemoveAnnotationQuickFix;
import com.intellij.java.language.impl.psi.impl.light.LightMethodBuilder;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PropertyUtil;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.lombok.codeInsight.quickFixes.RemoveModifierFix;
import consulo.lombok.processors.LombokFieldProcessor;
import consulo.lombok.processors.util.LombokUtil;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 2018-05-01
 */
public abstract class SetterAnnotationProcessorBase extends LombokFieldProcessor
{
	public SetterAnnotationProcessorBase(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiField psiField, @Nonnull List<PsiElement> result)
	{
		LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), PropertyUtil.suggestSetterName(psiField));
		builder.setMethodReturnType(PsiType.VOID);
		builder.setContainingClass(parent);
		builder.setNavigationElement(psiField);

		builder.addParameter(psiField.getName(), psiField.getType());

		if(psiField.hasModifierProperty(PsiModifier.STATIC))
		{
			builder.addModifier(PsiModifier.STATIC);
		}

		PsiAnnotation annotation = getAffectedAnnotation(psiField);

		LombokUtil.setAccessModifierFromAnnotation(annotation, builder, PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);

		result.add(builder);
	}

	@Override
	public boolean canBeProcessed(@Nonnull PsiField psiField)
	{
		return !psiField.hasModifierProperty(PsiModifier.FINAL);
	}

	@Override
	public void collectInspectionsForElement(@Nonnull PsiField element, @Nonnull ProblemsHolder problemsHolder)
	{
		PsiAnnotation affectedAnnotation = getAffectedAnnotation(element);

		problemsHolder.registerProblem(affectedAnnotation, "@Setter is invalid for final field", new RemoveAnnotationQuickFix(affectedAnnotation, element), new RemoveModifierFix(element, PsiModifier.FINAL));
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}
}
