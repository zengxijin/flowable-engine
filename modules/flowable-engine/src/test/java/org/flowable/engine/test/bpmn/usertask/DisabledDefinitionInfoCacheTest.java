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

package org.flowable.engine.test.bpmn.usertask;



import java.util.HashMap;
import java.util.Map;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.test.AbstractFlowableTestCase;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import org.flowable.engine.test.Deployment;

import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * @author Tijs Rademakers
 */
public class DisabledDefinitionInfoCacheTest extends AbstractFlowableTestCase {

  protected static ProcessEngine cachedProcessEngine;
  
  protected void initializeProcessEngine() {
    if (cachedProcessEngine==null) {
      ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
          .createProcessEngineConfigurationFromResource("org/flowable/engine/test/bpmn/usertask/activiti.cfg.xml");
      
      cachedProcessEngine = processEngineConfiguration.buildProcessEngine();
    }
    processEngine = cachedProcessEngine;
  }

  @Deployment
  public void testChangeFormKey() {
    // first test without changing the form key
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicUserTask");
    String processDefinitionId = processInstance.getProcessDefinitionId();
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("test", task.getFormKey());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // now test with changing the form key
    ObjectNode infoNode = dynamicBpmnService.changeUserTaskFormKey("task1", "test2");
    dynamicBpmnService.saveProcessDefinitionInfo(processDefinitionId, infoNode);
    
    processInstance = runtimeService.startProcessInstanceByKey("dynamicUserTask");
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("test", task.getFormKey());
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
  @Deployment
  public void testChangeClassName() {
    // first test without changing the class name
    Map<String, Object> varMap = new HashMap<String, Object>();
    varMap.put("count", 0);
    varMap.put("count2", 0);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("dynamicServiceTask", varMap);
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "count"));
    assertEquals(0, runtimeService.getVariable(processInstance.getId(), "count2"));
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // now test with changing the class name
    varMap = new HashMap<String, Object>();
    varMap.put("count", 0);
    varMap.put("count2", 0);
    processInstance = runtimeService.startProcessInstanceByKey("dynamicServiceTask", varMap);
    
    String processDefinitionId = processInstance.getProcessDefinitionId();
    ObjectNode infoNode = dynamicBpmnService.changeServiceTaskClassName("service", "org.flowable.engine.test.bpmn.servicetask.DummyServiceTask2");
    dynamicBpmnService.saveProcessDefinitionInfo(processDefinitionId, infoNode);
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertEquals(1, runtimeService.getVariable(processInstance.getId(), "count"));
    assertEquals(0, runtimeService.getVariable(processInstance.getId(), "count2"));
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
  }
  
}