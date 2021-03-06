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
package org.activiti5.examples.pvm;

import org.activiti5.engine.impl.pvm.PvmTransition;
import org.activiti5.engine.impl.pvm.delegate.ActivityExecution;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.ActivityBehavior;


/**
 * @author Tom Baeyens
 */
public class Decision implements ActivityBehavior {

  public void execute(DelegateExecution execution) {
    PvmTransition transition;
    ActivityExecution activityExecution = (ActivityExecution) execution;
    String creditRating = (String) execution.getVariable("creditRating");
    if (creditRating.equals("AAA+")) {
      transition = activityExecution.getActivity().findOutgoingTransition("wow");
    } else if (creditRating.equals("Aaa-")) {
      transition = activityExecution.getActivity().findOutgoingTransition("nice");
    } else {
      transition = activityExecution.getActivity().findOutgoingTransition("default");
    }

    activityExecution.take(transition);
  }
}
