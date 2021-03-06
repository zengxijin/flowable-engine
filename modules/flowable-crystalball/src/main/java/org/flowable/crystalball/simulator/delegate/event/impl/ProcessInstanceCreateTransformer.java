package org.flowable.crystalball.simulator.delegate.event.impl;

import org.flowable.crystalball.simulator.SimulationEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEntityEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * @author martin.grofcik
 */
public class ProcessInstanceCreateTransformer extends Activiti2SimulationEventFunction {

  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private final String processDefinitionIdKey;
  private final String businessKey;
  private final String variablesKey;

  public ProcessInstanceCreateTransformer(String simulationEventType, String processDefinitionIdKey, String businessKey, String variablesKey) {
    super(simulationEventType);
    this.processDefinitionIdKey = processDefinitionIdKey;
    this.businessKey = businessKey;
    this.variablesKey = variablesKey;
  }

  @Override
  public SimulationEvent apply(FlowableEvent event) {
    if (FlowableEngineEventType.ENTITY_INITIALIZED.equals(event.getType()) && (event instanceof FlowableEntityEvent) && ((FlowableEntityEvent) event).getEntity() instanceof ProcessInstance
        && ((ExecutionEntity) ((FlowableEntityEvent) event).getEntity()).isProcessInstanceType()) {

      ProcessInstance processInstance = (ProcessInstance) ((FlowableEntityEvent) event).getEntity();
      ExecutionEntity executionEntity = (ExecutionEntity) ((FlowableEntityEvent) event).getEntity();

      Map<String, Object> simEventProperties = new HashMap<String, Object>();
      simEventProperties.put(processDefinitionIdKey, processInstance.getProcessDefinitionId());
      simEventProperties.put(businessKey, processInstance.getBusinessKey());
      simEventProperties.put(variablesKey, executionEntity.getVariables());
      simEventProperties.put(PROCESS_INSTANCE_ID, executionEntity.getProcessInstanceId());

      return new SimulationEvent.Builder(simulationEventType).
              simulationTime(Context.getProcessEngineConfiguration().getClock().getCurrentTime().getTime()).
              properties(simEventProperties).
              priority(2).
              build();
    }
    return null;
  }
}
