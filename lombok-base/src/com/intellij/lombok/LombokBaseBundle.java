/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lombok;

import com.intellij.AbstractBundle;

/**
 * @author VISTALL
 * @since 18:20/30.03.13
 */
public class LombokBaseBundle extends AbstractBundle {
  private static final LombokBaseBundle INSTANCE = new LombokBaseBundle();

  private LombokBaseBundle() {
    super("message.LombokBase");
  }

  public static String message(String key, Object... arg) {
    return INSTANCE.getMessage(key, arg);
  }
}
