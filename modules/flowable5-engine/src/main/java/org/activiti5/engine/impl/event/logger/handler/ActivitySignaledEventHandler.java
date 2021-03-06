package org.activiti5.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.EventLogEntryEntity;
import org.flowable.engine.delegate.event.FlowableSignalEvent;

/**
 * @author Joram Barrez
 */
public class ActivitySignaledEventHandler extends AbstractDatabaseEventLoggerEventHandler {
	
	@Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
		FlowableSignalEvent signalEvent = (FlowableSignalEvent) event;
		
		Map<String, Object> data = new HashMap<String, Object>();
		putInMapIfNotNull(data, Fields.ACTIVITY_ID, signalEvent.getActivityId());
		putInMapIfNotNull(data, Fields.ACTIVITY_NAME, signalEvent.getActivityName());
		putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, signalEvent.getProcessDefinitionId());
		putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, signalEvent.getProcessInstanceId());
		putInMapIfNotNull(data, Fields.EXECUTION_ID, signalEvent.getExecutionId());
		putInMapIfNotNull(data, Fields.ACTIVITY_TYPE, signalEvent.getActivityType());
		putInMapIfNotNull(data, Fields.BEHAVIOR_CLASS, signalEvent.getBehaviorClass());
		
		putInMapIfNotNull(data, Fields.SIGNAL_NAME, signalEvent.getSignalName());
		putInMapIfNotNull(data, Fields.SIGNAL_DATA, signalEvent.getSignalData());
		
		return createEventLogEntry(signalEvent.getProcessDefinitionId(), signalEvent.getProcessInstanceId(), 
				signalEvent.getExecutionId(), null, data);
	}

}
