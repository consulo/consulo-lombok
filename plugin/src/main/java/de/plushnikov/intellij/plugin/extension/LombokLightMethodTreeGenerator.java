package de.plushnikov.intellij.plugin.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.TreeGenerator;
import consulo.language.impl.ast.ChangeUtil;
import consulo.language.impl.ast.TreeElement;
import consulo.language.impl.psi.SourceTreeToPsiMap;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.util.CharTable;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import org.jetbrains.annotations.Nullable;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public class LombokLightMethodTreeGenerator implements TreeGenerator {

  public LombokLightMethodTreeGenerator() {
  }

  @Override
  @Nullable
  public TreeElement generateTreeFor(PsiElement original, CharTable table, PsiManager manager) {
    TreeElement result = null;
    if (original instanceof LombokLightMethodBuilder) {
      result = ChangeUtil.copyElement((TreeElement) SourceTreeToPsiMap.psiElementToTree(original), table);
    }
    return result;
  }
}
