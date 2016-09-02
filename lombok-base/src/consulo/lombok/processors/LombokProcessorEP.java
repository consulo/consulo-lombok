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
package consulo.lombok.processors;

import com.intellij.openapi.extensions.AbstractExtensionPointBean;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.util.xmlb.annotations.Attribute;

/**
 * @author VISTALL
 * @since 18:45/29.03.13
 */
public class LombokProcessorEP extends AbstractExtensionPointBean{
  public static final ExtensionPointName<LombokProcessorEP> EP_NAME = ExtensionPointName.create("org.consulo.lombok.processor");

  @Attribute("annotationClass")
  public String annotationClass;

  @Attribute("implementationClass")
  public String implementationClass;

  private LombokProcessor myInstance;

  public LombokProcessor getInstance() {
    if(myInstance == null) {
      try {
        final Class<LombokProcessor> aClass = findClass(implementationClass);

        if(LombokAnnotationOwnerProcessor.class.isAssignableFrom(aClass)) {
          myInstance = aClass.getConstructor(String.class).newInstance(annotationClass);
        }
        else {
          myInstance = aClass.newInstance();
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return myInstance;
  }
}
