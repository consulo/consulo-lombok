package de.plushnikov.intellij.plugin.processor.handler.singular;

import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiVariable;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiTypeUtil;
import jakarta.annotation.Nonnull;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;

class SingularGuavaTableHandler extends SingularMapHandler {
  private static final String COM_GOOGLE_COMMON_COLLECT_TABLE = "com.google.common.collect.Table";

  private static final String LOMBOK_ROW_KEY = "rowKey";
  private static final String LOMBOK_COLUMN_KEY = "columnKey";
  private static final String LOMBOK_VALUE = "value";

  private final boolean sortedCollection;

  SingularGuavaTableHandler(String guavaQualifiedName, boolean sortedCollection) {
    super(guavaQualifiedName);
    this.sortedCollection = sortedCollection;
  }

  @Override
  public Collection<PsiField> renderBuilderFields(@Nonnull BuilderInfo info) {
    final PsiType builderFieldKeyType = getBuilderFieldType(info.getFieldType(), info.getProject());
    return Collections.singleton(
      new LombokLightFieldBuilder(info.getManager(), info.getFieldName(), builderFieldKeyType)
        .withContainingClass(info.getBuilderClass())
        .withModifier(PsiModifier.PRIVATE)
        .withNavigationElement(info.getVariable()));
  }

  @Override
  @Nonnull
  protected PsiType getBuilderFieldType(@Nonnull PsiType psiFieldType, @Nonnull Project project) {
    final PsiManager psiManager = PsiManager.getInstance(project);
    final PsiType rowKeyType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 0);
    final PsiType columnKeyType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 1);
    final PsiType valueType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 2);

    return PsiTypeUtil.createCollectionType(psiManager, collectionQualifiedName + ".Builder", rowKeyType, columnKeyType, valueType);
  }

  @Override
  protected void addOneMethodParameter(@Nonnull LombokLightMethodBuilder methodBuilder, @Nonnull PsiType psiFieldType, @Nonnull String singularName) {
    final PsiManager psiManager = methodBuilder.getManager();
    final PsiType rowKeyType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 0);
    final PsiType columnKeyType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 1);
    final PsiType valueType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 2);

    methodBuilder.withParameter(LOMBOK_ROW_KEY, rowKeyType);
    methodBuilder.withParameter(LOMBOK_COLUMN_KEY, columnKeyType);
    methodBuilder.withParameter(LOMBOK_VALUE, valueType);
  }

  @Override
  protected void addAllMethodParameter(@Nonnull LombokLightMethodBuilder methodBuilder, @Nonnull PsiType psiFieldType, @Nonnull String singularName) {
    final PsiManager psiManager = methodBuilder.getManager();
    final PsiType rowKeyType = PsiTypeUtil.extractAllElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 0);
    final PsiType columnKeyType = PsiTypeUtil.extractAllElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 1);
    final PsiType valueType = PsiTypeUtil.extractAllElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 2);

    final PsiType collectionType = PsiTypeUtil.createCollectionType(psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, rowKeyType, columnKeyType, valueType);

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
    final String codeBlockTemplate = "if (this.{0} == null) this.{0} = {2}.{3}; \n" +
      "this.{0}.put(" + LOMBOK_ROW_KEY + ", " + LOMBOK_COLUMN_KEY + ", " + LOMBOK_VALUE + ");\n" +
      "return {4};";

    return MessageFormat.format(codeBlockTemplate, info.getFieldName(), singularName, collectionQualifiedName,
      sortedCollection ? "naturalOrder()" : "builder()", info.getBuilderChainResult());
  }

  @Override
  protected String getAllMethodBody(@Nonnull String singularName, @Nonnull BuilderInfo info) {
    final String codeBlockTemplate = """
      if({0}==null)'{'throw new NullPointerException("{0} cannot be null");'}'
      if (this.{0} == null) this.{0} = {1}.{2};\s
      this.{0}.putAll({0});
      return {3};""";

    return MessageFormat.format(codeBlockTemplate, singularName, collectionQualifiedName,
      sortedCollection ? "naturalOrder()" : "builder()", info.getBuilderChainResult());
  }

  @Override
  protected String renderBuildCode(@Nonnull PsiVariable psiVariable, @Nonnull String fieldName, @Nonnull String builderVariable) {
    final PsiManager psiManager = psiVariable.getManager();
    final PsiType psiFieldType = psiVariable.getType();

    final PsiType rowKeyType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 0);
    final PsiType columnKeyType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 1);
    final PsiType valueType = PsiTypeUtil.extractOneElementType(psiFieldType, psiManager, COM_GOOGLE_COMMON_COLLECT_TABLE, 2);

    return MessageFormat.format(
      "{4}<{1}, {2}, {3}> {0} = " +
        "{5}.{0} == null ? " +
        "{4}.<{1}, {2}, {3}>of() : " +
        "{5}.{0}.build();\n",
      fieldName, rowKeyType.getCanonicalText(false), columnKeyType.getCanonicalText(false),
      valueType.getCanonicalText(false), collectionQualifiedName, builderVariable);
  }

  @Override
  protected String getEmptyCollectionCall(@Nonnull BuilderInfo info) {
    return collectionQualifiedName + '.' + "builder()";
  }
}
