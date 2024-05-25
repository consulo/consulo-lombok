package de.plushnikov.intellij.plugin.processor.handler.singular;

import com.intellij.java.language.psi.CommonClassNames;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiVariable;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiTypeUtil;
import jakarta.annotation.Nonnull;

import java.text.MessageFormat;

class SingularGuavaCollectionHandler extends SingularCollectionHandler {

  private final boolean sortedCollection;
  private final String typeCollectionQualifiedName;

  SingularGuavaCollectionHandler(String collectionQualifiedName, boolean sortedCollection) {
    super(collectionQualifiedName);
    this.sortedCollection = sortedCollection;
    this.typeCollectionQualifiedName = SingularCollectionClassNames.GUAVA_IMMUTABLE_COLLECTION.equals(collectionQualifiedName)
      ? SingularCollectionClassNames.GUAVA_IMMUTABLE_LIST : collectionQualifiedName;
  }

  @Override
  @Nonnull
  protected PsiType getBuilderFieldType(@Nonnull PsiType psiFieldType, @Nonnull Project project) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final PsiType elementType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager);

    return PsiTypeUtil.createCollectionType(psiManager, typeCollectionQualifiedName + ".Builder", elementType);
  }

  @Override
  protected void addAllMethodParameter(@Nonnull LombokLightMethodBuilder methodBuilder, @Nonnull PsiType psiFieldType, @Nonnull String singularName) {
    final PsiManager psiManager = methodBuilder.getManager();
    final PsiType elementType = PsiTypeUtil.extractAllElementType(psiFieldType, psiManager);
    final PsiType collectionType = PsiTypeUtil.createCollectionType(psiManager, CommonClassNames.JAVA_LANG_ITERABLE, elementType);

    methodBuilder.withParameter(singularName, collectionType);
  }

  @Override
  protected String getClearMethodBody(@Nonnull BuilderInfo info) {
    final String codeBlockFormat = "this.{0} = null;\n" +
      "return {1};";
    return MessageFormat.format(codeBlockFormat, info.getFieldName(), info.getBuilderChainResult());
  }

  @Override
  protected String getOneMethodBody(@Nonnull String singularName, @Nonnull BuilderInfo info) {
    final String codeBlockTemplate = """
      if (this.{0} == null) this.{0} = {2}.{3};\s
      this.{0}.add({1});
      return {4};""";

    return MessageFormat.format(codeBlockTemplate, info.getFieldName(), singularName, typeCollectionQualifiedName,
      sortedCollection ? "naturalOrder()" : "builder()", info.getBuilderChainResult());
  }

  @Override
  protected String getAllMethodBody(@Nonnull String singularName, @Nonnull BuilderInfo info) {
    final String codeBlockTemplate = """
      if({0}==null)'{'throw new NullPointerException("{0} cannot be null");'}'
      if (this.{0} == null) this.{0} = {1}.{2};\s
      this.{0}.addAll({0});
      return {3};""";

    return MessageFormat.format(codeBlockTemplate, singularName, typeCollectionQualifiedName,
      sortedCollection ? "naturalOrder()" : "builder()", info.getBuilderChainResult());
  }

  @Override
  public String renderBuildCode(@Nonnull PsiVariable psiVariable, @Nonnull String fieldName, @Nonnull String builderVariable) {
    final PsiManager psiManager = psiVariable.getManager();
    final PsiType psiFieldType = psiVariable.getType();

    final PsiType elementType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager);
    return MessageFormat.format(
      "{2}<{1}> {0} = " +
        "{4}.{0} == null ? " +
        "{3}.<{1}>of() : " +
        "{4}.{0}.build();\n",
      fieldName, elementType.getCanonicalText(false), collectionQualifiedName, typeCollectionQualifiedName, builderVariable);
  }

  @Override
  protected String getEmptyCollectionCall(@Nonnull BuilderInfo info) {
    return typeCollectionQualifiedName + '.' + "builder()";
  }
}
