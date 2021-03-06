package org.activiti5.engine.impl.event.logger.handler;

import java.util.HashMap;
import java.util.Map;

import org.activiti5.engine.impl.interceptor.CommandContext;
import org.activiti5.engine.impl.persistence.entity.EventLogEntryEntity;
import org.flowable.engine.delegate.event.FlowableMessageEvent;

/**
 * @author Joram Barrez
 */
public class ActivityMessageEventHandler extends AbstractDatabaseEventLoggerEventHandler {
	
	@Override
	public EventLogEntryEntity generateEventLogEntry(CommandContext commandContext) {
		FlowableMessageEvent messageEvent = (FlowableMessageEvent) event;
		
		Map<String, Object> data = new HashMap<String, Object>();
		putInMapIfNotNull(data, Fields.ACTIVITY_ID, messageEvent.getActivityId());
		putInMapIfNotNull(data, Fields.ACTIVITY_NAME, messageEvent.getActivityName());
		putInMapIfNotNull(data, Fields.PROCESS_DEFINITION_ID, messageEvent.getProcessDefinitionId());
		putInMapIfNotNull(data, Fields.PROCESS_INSTANCE_ID, messageEvent.getProcessInstanceId());
		putInMapIfNotNull(data, Fields.EXECUTION_ID, messageEvent.getExecutionId());
		putInMapIfNotNull(data, Fields.ACTIVITY_TYPE, messageEvent.getActivityType());
		putInMapIfNotNull(data, Fields.BEHAVIOR_CLASS, messageEvent.getBehaviorClass());
		
		putInMapIfNotNull(data, Fields.MESSAGE_NAME, messageEvent.getMessageName());
		putInMapIfNotNull(data, Fields.MESSAGE_DATA, messageEvent.getMessageData());
		
		return createEventLogEntry(messageEvent.getProcessDefinitionId(), messageEvent.getProcessInstanceId(), 
				messageEvent.getExecutionId(), null, data);
	}

}
