package de.plushnikov.intellij.plugin.inspection.modifiers;

import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.FINAL;
import static com.intellij.java.language.psi.PsiModifier.STATIC;
import static de.plushnikov.intellij.plugin.inspection.modifiers.RedundantModifiersInfoType.*;

@ExtensionImpl
public class RedundantModifiersOnUtilityClassLombokAnnotationInspection extends LombokRedundantModifierInspection {

  public RedundantModifiersOnUtilityClassLombokAnnotationInspection() {
    super(
      LombokClassNames.UTILITY_CLASS,
      new RedundantModifiersInfo(CLASS, null, LombokBundle.message("inspection.message.utility.class.already.marks.class.final"), FINAL),
      new RedundantModifiersInfo(FIELD, null, LombokBundle.message("inspection.message.utility.class.already.marks.fields.static"), STATIC),
      new RedundantModifiersInfo(METHOD, null, LombokBundle.message("inspection.message.utility.class.already.marks.methods.static"), STATIC),
      new RedundantModifiersInfo(INNER_CLASS, null,
                                 LombokBundle.message("inspection.message.utility.class.already.marks.inner.classes.static"), STATIC)
    );
  }

  @Nonnull
  @Override
  public String getShortName() {
    return "RedundantModifiersUtilityClassLombok";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return LombokBundle.message("inspection.redundant.modifiers.utility.class.lombok.display.name");
  }
}
