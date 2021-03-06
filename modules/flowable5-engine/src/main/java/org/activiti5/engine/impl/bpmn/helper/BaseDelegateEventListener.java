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
package org.activiti5.engine.impl.bpmn.helper;

import org.flowable.engine.common.api.delegate.event.FlowableEntityEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEventListener;

/**
 * Base implementation of a {@link FlowableEventListener}, used when creating event-listeners
 * that are part of a BPMN definition.
 * 
 * @author Frederik Heremans
 */
public abstract class BaseDelegateEventListener implements FlowableEventListener {

	protected Class<?> entityClass;
	
	public void setEntityClass(Class<?> entityClass) {
	  this.entityClass = entityClass;
  }
	
	protected boolean isValidEvent(FlowableEvent event) {
		boolean valid = false;
	  if(entityClass != null) {
	  	if(event instanceof FlowableEntityEvent) {
	  		Object entity = ((FlowableEntityEvent) event).getEntity();
	  		if(entity != null) {
	  			valid = entityClass.isAssignableFrom(entity.getClass());
	  		}
	  	}
	  } else {
	  	// If no class is specified, all events are valid
	  	valid = true;
	  }
	  return valid;
  }
	
}
