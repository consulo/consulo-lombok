package de.plushnikov.intellij.plugin.problem;

import de.plushnikov.intellij.plugin.LombokBundle;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author Plushnikov Michail
 */
public interface ProblemSink {
  boolean deepValidation();

  boolean success();

  void markFailed();

  LombokProblem addWarningMessage(@Nonnull @PropertyKey(resourceBundle = LombokBundle.PATH_TO_BUNDLE) String key, Object... params);

  LombokProblem addErrorMessage(@Nonnull @PropertyKey(resourceBundle = LombokBundle.PATH_TO_BUNDLE) String key, Object... params);

}
