/**
 * @author VISTALL
 * @since 18/05/2023
 */
open module consulo.lombok {
    requires consulo.application.api;
    requires consulo.code.editor.api;
    requires consulo.color.scheme.api;
    requires consulo.component.api;
    requires consulo.document.api;
    requires consulo.file.editor.api;
    requires consulo.find.api;
    requires consulo.index.io;
    requires consulo.language.api;
    requires consulo.ide.impl;
    requires consulo.language.impl;
    requires consulo.language.editor.api;
    requires consulo.language.editor.refactoring.api;
    requires consulo.language.code.style.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.module.api;
    requires consulo.module.content.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.virtual.file.system.api;
    requires consulo.util.collection;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.lang;

    requires consulo.java;
}
