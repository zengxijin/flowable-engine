package org.flowable.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.common.api.delegate.event.FlowableEntityEvent;
import org.flowable.engine.delegate.event.FlowableEntityWithVariablesEvent;
import org.flowable.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.EventLogEntryEntity;
import org.flowable.engine.impl.persistence.entity.TaskEntity;

/**
 * @author Joram Barrez
 */
public class TaskCompletedEventHandler extends AbstractTaskEventHandler {

  @Override
  public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
    
    FlowableEntityEvent activitiEntityEvent = (FlowableEntityEvent) event;

    TaskEntity task = (TaskEntity) activitiEntityEvent.getEntity();
    Map<String, Object> data = handleCommonTaskFields(task);

    long duration = timeStamp.getTime() - task.getCreateTime().getTime();
    putInMapIfNotNull(data, Fields.DURATION, duration);

    if (event instanceof FlowableEntityWithVariablesEvent) {
      FlowableEntityWithVariablesEvent activitiEntityWithVariablesEvent = (FlowableEntityWithVariablesEvent) event;
      if (activitiEntityWithVariablesEvent.getVariables() != null && !activitiEntityWithVariablesEvent.getVariables().isEmpty()) {
        Map<String, Object> variableMap = new HashMap<String, Object>();
        for (Object variableName : activitiEntityWithVariablesEvent.getVariables().keySet()) {
          putInMapIfNotNull(variableMap, (String) variableName, activitiEntityWithVariablesEvent.getVariables().get(variableName));
        }
        if (activitiEntityWithVariablesEvent.isLocalScope()) {
          putInMapIfNotNull(data, Fields.LOCAL_VARIABLES, variableMap);
        } else {
          putInMapIfNotNull(data, Fields.VARIABLES, variableMap);
        }
      }
  
    }
    
    return createEventLogEntry(task.getProcessDefinitionId(), task.getProcessInstanceId(), task.getExecutionId(), task.getId(), data);
  }

}
