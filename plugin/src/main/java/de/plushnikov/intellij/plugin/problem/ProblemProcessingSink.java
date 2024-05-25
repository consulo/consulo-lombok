package de.plushnikov.intellij.plugin.problem;

import jakarta.annotation.Nonnull;

/**
 * @author Plushnikov Michail
 */
public class ProblemProcessingSink implements ProblemSink {

  private boolean validationFailed = false;

  @Override
  public boolean deepValidation() {
    return false;
  }

  @Override
  public boolean success() {
    return !validationFailed;
  }

  @Override
  public void markFailed() {
    validationFailed = true;
  }

  @Override
  public LombokProblem addWarningMessage(@Nonnull String key, @Nonnull Object ... params) {
    return LombokProblem.BLACKHOLE;
  }

  @Override
  public LombokProblem addErrorMessage(@Nonnull String key, @Nonnull Object... params) {
    return LombokProblem.BLACKHOLE;
  }
}
