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
package org.activiti5.engine.delegate.event.impl;

import org.activiti5.engine.ActivitiIllegalArgumentException;
import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.delegate.event.FlowableEngineEventType;
import org.flowable.engine.impl.delegate.event.ActivitiEngineEntityEvent;

/**
 * Base class for all {@link FlowableEvent} implementations, related to entities.
 * 
 * @author Frederik Heremans
 */
public class ActivitiEntityEventImpl extends ActivitiEventImpl implements ActivitiEngineEntityEvent {

	protected Object entity;
	
	public ActivitiEntityEventImpl(Object entity, FlowableEngineEventType type) {
		super(type);
		if(entity == null) {
			throw new ActivitiIllegalArgumentException("Entity cannot be null.");
		}
	  this.entity = entity;
  }
	
	@Override
	public Object getEntity() {
		return entity;
	}
}
