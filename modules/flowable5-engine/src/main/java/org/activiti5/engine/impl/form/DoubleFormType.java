/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti5.engine.impl.form;

import org.flowable.engine.form.AbstractFormType;



/**
 * @author Tom Baeyens
 */
public class DoubleFormType extends AbstractFormType {
	
  private static final long serialVersionUID = 1L;

  public String getName() {
    return "double";
  }

  public String getMimeType() {
    return "plain/text";
  }

  public Object convertFormValueToModelValue(String propertyValue) {
    if (propertyValue==null || "".equals(propertyValue)) {
      return null;
    }
    return new Double(propertyValue);
  }

  public String convertModelValueToFormValue(Object modelValue) {
    if (modelValue==null) {
      return null;
    }
    return modelValue.toString();
  }
}
