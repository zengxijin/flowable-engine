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

package org.activiti5.engine.impl.bpmn.behavior;

import java.util.List;

import org.activiti5.engine.impl.bpmn.helper.ScopeUtil;
import org.activiti5.engine.impl.bpmn.parser.CompensateEventDefinition;
import org.activiti5.engine.impl.persistence.entity.CompensateEventSubscriptionEntity;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti5.engine.impl.pvm.process.ActivityImpl;
import org.flowable.engine.delegate.DelegateExecution;


/**
 * @author Daniel Meyer
 */
public class IntermediateThrowCompensationEventActivityBehavior extends FlowNodeActivityBehavior {

  protected final CompensateEventDefinition compensateEventDefinition;

  public IntermediateThrowCompensationEventActivityBehavior(CompensateEventDefinition compensateEventDefinition) {
    this.compensateEventDefinition = compensateEventDefinition;
  }
  
  @Override
  public void execute(DelegateExecution execution) {
    final String activityRef = compensateEventDefinition.getActivityRef();
    ActivityExecution activityExecution = (ActivityExecution) execution;
    ExecutionEntity scopeExecution = ScopeUtil.findScopeExecutionForScope((ExecutionEntity)execution, (ActivityImpl) activityExecution.getActivity());
    
    List<CompensateEventSubscriptionEntity> eventSubscriptions;
    
    if(activityRef != null) {          
      eventSubscriptions = scopeExecution.getCompensateEventSubscriptions(activityRef);
    } else {
      eventSubscriptions = scopeExecution.getCompensateEventSubscriptions();
    }
        
    if (eventSubscriptions.isEmpty()) {
      leave(activityExecution);
    } else {
      // TODO: implement async (waitForCompletion=false in bpmn)
      ScopeUtil.throwCompensationEvent(eventSubscriptions, activityExecution, false );
    }
        
  }
  
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
   
    // join compensating executions    
    if(execution.getExecutions().isEmpty()) {
      leave(execution);   
    } else {      
      ((ExecutionEntity)execution).forceUpdate();  
    }
    
  }
  

}
