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

package org.activiti5.engine.impl.history.handler;

import org.activiti5.engine.impl.context.Context;
import org.activiti5.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;


/**
 * @author Tom Baeyens
 */
public class StartEventEndHandler implements ExecutionListener {

  public void notify(DelegateExecution execution) {
    String activityId = ((ExecutionEntity)execution).getActivityId();
    
    Context.getCommandContext().getHistoryManager()
      .recordStartEventEnded((ExecutionEntity) execution, activityId);
  }

}
