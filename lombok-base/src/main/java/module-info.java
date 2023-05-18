/**
 * @author VISTALL
 * @since 18/05/2023
 */
module consulo.lombok
{
	requires transitive consulo.lombok.api;
	requires consulo.java;

	exports consulo.lombok.impl;
	exports consulo.lombok.impl.codeInsight;
	exports consulo.lombok.impl.icon;
	exports consulo.lombok.impl.intentions;
	exports consulo.lombok.impl.module.extension;
	exports consulo.lombok.impl.processors.impl;
	exports consulo.lombok.impl.psi;
	exports consulo.lombok.impl.psi.impl.source;
}