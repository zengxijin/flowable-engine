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
package org.flowable.engine.impl.cmd;

import org.flowable.engine.common.api.FlowableIllegalArgumentException;
import org.flowable.engine.common.api.FlowableObjectNotFoundException;
import org.flowable.engine.compatibility.Activiti5CompatibilityHandler;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.delegate.event.impl.FlowableEventBuilder;
import org.flowable.engine.impl.interceptor.Command;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.deploy.DeploymentCache;
import org.flowable.engine.impl.persistence.deploy.ProcessDefinitionCacheEntry;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.util.Activiti5Util;
import org.flowable.engine.repository.ProcessDefinition;

/**
 * @author Joram Barrez
 */
public class SetProcessDefinitionCategoryCmd implements Command<Void> {

  protected String processDefinitionId;
  protected String category;

  public SetProcessDefinitionCategoryCmd(String processDefinitionId, String category) {
    this.processDefinitionId = processDefinitionId;
    this.category = category;
  }

  public Void execute(CommandContext commandContext) {

    if (processDefinitionId == null) {
      throw new FlowableIllegalArgumentException("Process definition id is null");
    }

    ProcessDefinitionEntity processDefinition = commandContext.getProcessDefinitionEntityManager().findById(processDefinitionId);

    if (processDefinition == null) {
      throw new FlowableObjectNotFoundException("No process definition found for id = '" + processDefinitionId + "'", ProcessDefinition.class);
    }
    
    if (Activiti5Util.isActiviti5ProcessDefinition(commandContext, processDefinition)) {
      Activiti5CompatibilityHandler activiti5CompatibilityHandler = Activiti5Util.getActiviti5CompatibilityHandler(); 
      activiti5CompatibilityHandler.setProcessDefinitionCategory(processDefinitionId, category);
      return null;
    }

    // Update category
    processDefinition.setCategory(category);

    // Remove process definition from cache, it will be refetched later
    DeploymentCache<ProcessDefinitionCacheEntry> processDefinitionCache = commandContext.getProcessEngineConfiguration().getProcessDefinitionCache();
    if (processDefinitionCache != null) {
      processDefinitionCache.remove(processDefinitionId);
    }

    if (commandContext.getEventDispatcher().isEnabled()) {
      commandContext.getEventDispatcher().dispatchEvent(FlowableEventBuilder.createEntityEvent(FlowableEngineEventType.ENTITY_UPDATED, processDefinition));
    }

    return null;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

}
