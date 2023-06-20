package consulo.lombok.impl.processors.impl;

import com.intellij.java.language.jvm.JvmModifier;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokSelfClassProcessor;
import consulo.lombok.processors.util.LombokClassUtil;
import consulo.module.extension.ModuleExtension;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;

import java.util.List;

/**
 * @author VISTALL
 * @since 20/06/2023
 */
@ExtensionImpl
public class SetterClassAnnotationProcessor extends LombokSelfClassProcessor
{
	@Inject
	public SetterClassAnnotationProcessor()
	{
		super(SetterFieldAnnotationProcessor.ANNOTATION_CLASS);
	}

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiClass psiClass, @Nonnull List<PsiElement> result)
	{
		List<PsiField> ownFields = LombokClassUtil.getOwnFields(psiClass);

		PsiAnnotation annotation = getAffectedAnnotation(parent);

		for(PsiField ownField : ownFields)
		{
			if(ownField.hasModifier(JvmModifier.STATIC))
			{
				continue;
			}

			result.add(SetterFieldAnnotationProcessor.createSetter(parent, ownField, annotation));
		}
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}

	@Nonnull
	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}
}
