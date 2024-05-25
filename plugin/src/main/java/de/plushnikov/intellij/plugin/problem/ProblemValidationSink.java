package de.plushnikov.intellij.plugin.problem;

import consulo.language.editor.inspection.ProblemHighlightType;
import de.plushnikov.intellij.plugin.LombokBundle;
import jakarta.annotation.Nonnull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Plushnikov Michail
 */
public class ProblemValidationSink implements ProblemSink {
  private final Set<LombokProblem> problems = new HashSet<>();
  private boolean validationFailed = false;

  @Override
  public boolean deepValidation() {
    return true;
  }

  @Override
  public boolean success() {
    return !validationFailed;
  }

  @Override
  public void markFailed() {
    validationFailed = true;
  }

  public Set<LombokProblem> getProblems() {
    return problems;
  }

  @Override
  public LombokProblemInstance addWarningMessage(@Nonnull String key, @Nonnull Object... params) {
    return addProblem(LombokBundle.message(key, params), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
  }

  @Override
  public LombokProblemInstance addErrorMessage(@Nonnull String key, @Nonnull Object... params) {
    return addProblem(LombokBundle.message(key, params), ProblemHighlightType.GENERIC_ERROR);
  }

  private LombokProblemInstance addProblem(String message,
                                           ProblemHighlightType highlightType) {
    final LombokProblemInstance lombokProblem = new LombokProblemInstance(message, highlightType);
    problems.add(lombokProblem);
    return lombokProblem;
  }
}
