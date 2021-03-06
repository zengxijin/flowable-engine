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
package org.flowable.dmn.api;

import java.util.Map;

/**
 * @author Tijs Rademakers
 * @author Yvo Swillens
 */
public interface DmnRuleService {

  RuleEngineExecutionResult executeDecisionByKey(String decisionKey, Map<String, Object> processVariables);

  RuleEngineExecutionResult executeDecisionByKeyAndTenantId(String decisionKey, Map<String, Object> processVariables, String tenantId);

  RuleEngineExecutionResult executeDecisionByKeyAndParentDeploymentId(String decisionKey, String parentDeploymentId, Map<String, Object> variables);

  RuleEngineExecutionResult executeDecisionByKeyParentDeploymentIdAndTenantId(String decisionKey, String parentDeploymentId, Map<String, Object> variables, String tenantId);
}
