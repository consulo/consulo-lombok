/**
 * @author VISTALL
 * @since 18/05/2023
 */
module consulo.lombok.api
{
	requires consulo.ide.api;
	requires consulo.java;

	exports consulo.lombok.codeInsight.quickFixes;
	exports consulo.lombok.processors;
	exports consulo.lombok.processors.impl;
	exports consulo.lombok.processors.util;
	exports consulo.lombok.psi.augment;
}