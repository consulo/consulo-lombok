package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.*;
import de.plushnikov.intellij.plugin.processor.clazz.builder.*;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsOldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsPredefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.*;
import de.plushnikov.intellij.plugin.processor.field.*;
import de.plushnikov.intellij.plugin.processor.method.BuilderClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.BuilderMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.DelegateMethodProcessor;

@ActionImpl(id = "delombokAny")
public class DelombokEverythingAction extends AbstractDelombokAction {
    public DelombokEverythingAction() {
        super(LombokLocalize.actionDelombokAnyText(), LombokLocalize.actionDelombokAnyDescription());
    }

    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            true,
            ProcessorUtil.getProcessor(RequiredArgsConstructorProcessor.class),
            ProcessorUtil.getProcessor(AllArgsConstructorProcessor.class),
            ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class),

            ProcessorUtil.getProcessor(DataProcessor.class),
            ProcessorUtil.getProcessor(GetterProcessor.class),
            ProcessorUtil.getProcessor(ValueProcessor.class),
            ProcessorUtil.getProcessor(WitherProcessor.class),
            ProcessorUtil.getProcessor(SetterProcessor.class),
            ProcessorUtil.getProcessor(EqualsAndHashCodeProcessor.class),
            ProcessorUtil.getProcessor(ToStringProcessor.class),

            ProcessorUtil.getProcessor(CommonsLogProcessor.class),
            ProcessorUtil.getProcessor(JBossLogProcessor.class),
            ProcessorUtil.getProcessor(Log4jProcessor.class),
            ProcessorUtil.getProcessor(Log4j2Processor.class),
            ProcessorUtil.getProcessor(LogProcessor.class),
            ProcessorUtil.getProcessor(Slf4jProcessor.class),
            ProcessorUtil.getProcessor(XSlf4jProcessor.class),
            ProcessorUtil.getProcessor(FloggerProcessor.class),
            ProcessorUtil.getProcessor(CustomLogProcessor.class),

            ProcessorUtil.getProcessor(GetterFieldProcessor.class),
            ProcessorUtil.getProcessor(SetterFieldProcessor.class),
            ProcessorUtil.getProcessor(WitherFieldProcessor.class),
            ProcessorUtil.getProcessor(DelegateFieldProcessor.class),
            ProcessorUtil.getProcessor(DelegateMethodProcessor.class),

            ProcessorUtil.getProcessor(FieldNameConstantsOldProcessor.class),
            ProcessorUtil.getProcessor(FieldNameConstantsFieldProcessor.class),
            ProcessorUtil.getProcessor(FieldNameConstantsProcessor.class),
            ProcessorUtil.getProcessor(FieldNameConstantsPredefinedInnerClassFieldProcessor.class),

            ProcessorUtil.getProcessor(UtilityClassProcessor.class),
            ProcessorUtil.getProcessor(StandardExceptionProcessor.class),

            ProcessorUtil.getProcessor(BuilderPreDefinedInnerClassFieldProcessor.class),
            ProcessorUtil.getProcessor(BuilderPreDefinedInnerClassMethodProcessor.class),
            ProcessorUtil.getProcessor(BuilderClassProcessor.class),
            ProcessorUtil.getProcessor(BuilderClassMethodProcessor.class),
            ProcessorUtil.getProcessor(BuilderMethodProcessor.class),
            ProcessorUtil.getProcessor(BuilderProcessor.class),

            ProcessorUtil.getProcessor(SuperBuilderPreDefinedInnerClassFieldProcessor.class),
            ProcessorUtil.getProcessor(SuperBuilderPreDefinedInnerClassMethodProcessor.class),
            ProcessorUtil.getProcessor(SuperBuilderClassProcessor.class),
            ProcessorUtil.getProcessor(SuperBuilderProcessor.class)
        );
    }
}
