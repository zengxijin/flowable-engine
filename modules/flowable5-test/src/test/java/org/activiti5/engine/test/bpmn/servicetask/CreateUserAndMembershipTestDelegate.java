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
package org.activiti5.engine.test.bpmn.servicetask;

import org.activiti5.engine.IdentityService;
import org.activiti5.engine.impl.context.Context;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;

/**
 * @author Joram Barrez
 */
public class CreateUserAndMembershipTestDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {

    IdentityService identityService = Context.getProcessEngineConfiguration().getIdentityService();
    
    String username = "Kermit";
    User user = identityService.newUser(username);
    user.setPassword("123");
    user.setFirstName("Manually");
    user.setLastName("created");
    identityService.saveUser(user);
    
    // Add admin group
    Group group = identityService.newGroup("admin");
    identityService.saveGroup(group);

    identityService.createMembership(username, "admin");
  }

}
