/*
 * Copyright 2013 must-be.org
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
package consulo.lombok.impl.processors.impl;

import consulo.annotation.component.ExtensionImpl;

/**
 * @author VISTALL
 * @since 15:24/30.03.13
 */
@ExtensionImpl
public class XSlf4jAnnotationProcessor extends AbstractLoggerAnnotationProcessor {
  public XSlf4jAnnotationProcessor() {
    super("lombok.extern.slf4j.XSlf4j");
  }

  @Override
  public String getLoggerClass() {
    return "org.slf4j.ext.XLogger";
  }
}
