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

package org.flowable.standalone.scripting;

import org.flowable.engine.impl.test.ResourceFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;

/**
 * @author Tom Baeyens
 */
public class ScriptBeanAccessTest extends ResourceFlowableTestCase {

  public ScriptBeanAccessTest() {
    super("org/flowable/standalone/scripting/activiti.cfg.xml");
  }

  @Deployment
  public void testConfigurationBeanAccess() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("ScriptBeanAccess");
    assertEquals("myValue", runtimeService.getVariable(pi.getId(), "myVariable"));
  }

}
