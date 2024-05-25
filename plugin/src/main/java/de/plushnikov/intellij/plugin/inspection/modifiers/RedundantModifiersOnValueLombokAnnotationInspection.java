package de.plushnikov.intellij.plugin.inspection.modifiers;

import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.*;

/**
 * @author Rowicki Michał
 */
@ExtensionImpl
public class RedundantModifiersOnValueLombokAnnotationInspection extends LombokRedundantModifierInspection {

  public RedundantModifiersOnValueLombokAnnotationInspection() {
    super(
      LombokClassNames.VALUE,
      new RedundantModifiersInfo(RedundantModifiersInfoType.CLASS, null,
                                 LombokBundle.message("inspection.message.value.already.marks.class.final"), FINAL),
      new RedundantModifiersInfo(RedundantModifiersInfoType.FIELD, STATIC,
                                 LombokBundle.message("inspection.message.value.already.marks.non.static.fields.final"), FINAL),
      new RedundantModifiersInfo(RedundantModifiersInfoType.FIELD, STATIC,
                                 LombokBundle.message("inspection.message.value.already.marks.non.static.package.local.fields.private"), PRIVATE));
  }

  @Nonnull
  @Override
  public String getShortName() {
    return "RedundantModifiersValueLombok";
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return LombokBundle.message("inspection.redundant.modifiers.value.lombok.display.name");
  }
}
