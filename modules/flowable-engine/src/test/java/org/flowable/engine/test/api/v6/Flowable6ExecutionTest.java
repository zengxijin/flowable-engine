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
package org.flowable.engine.test.api.v6;

import java.util.ArrayList;
import java.util.List;

import org.flowable.engine.common.api.delegate.event.FlowableEvent;
import org.flowable.engine.common.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.delegate.event.FlowableActivityEvent;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.history.HistoryLevel;
import org.flowable.engine.impl.test.PluggableFlowableTestCase;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Task;
import org.flowable.engine.test.Deployment;
import org.junit.Test;

public class Flowable6ExecutionTest extends PluggableFlowableTestCase {

  @Test
  @Deployment
  public void testOneTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    
    List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, executionList.size());
    
    Execution rootProcessInstance = null;
    Execution childExecution = null;
    
    for (Execution execution : executionList) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        rootProcessInstance = execution;
        
        assertNull(execution.getActivityId());
      
      } else {
        childExecution = execution;
        
        assertTrue(execution.getId().equals(execution.getProcessInstanceId()) == false);
        assertEquals("theTask", execution.getActivityId());
      }
    }
    
    assertNotNull(rootProcessInstance);
    assertNotNull(childExecution);

    Task task = taskService.createTaskQuery().singleResult();
    assertEquals(childExecution.getId(), task.getExecutionId());

    taskService.complete(task.getId());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      assertEquals(3, historicActivities.size());
      
      List<String> activityIds = new ArrayList<String>();
      activityIds.add("theStart");
      activityIds.add("theTask");
      activityIds.add("theEnd");
      
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        activityIds.remove(historicActivityInstance.getActivityId());
        assertEquals(childExecution.getId(), historicActivityInstance.getExecutionId());
      }
      
      assertEquals(0, activityIds.size());
    }
  }
  
  @Test
  @Deployment
  public void testOneNestedTaskProcess() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneNestedTaskProcess");
    
    List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, executionList.size());
    
    Execution rootProcessInstance = null;
    Execution childExecution = null;
    
    for (Execution execution : executionList) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        rootProcessInstance = execution;
        
        assertNull(execution.getActivityId());
      
      } else {
        childExecution = execution;
        
        assertTrue(execution.getId().equals(execution.getProcessInstanceId()) == false);
        assertEquals("theTask1", execution.getActivityId());
      }
    }
    
    assertNotNull(rootProcessInstance);
    assertNotNull(childExecution);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(childExecution.getId(), task.getExecutionId());

    taskService.complete(task.getId());
    
    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(3, executionList.size());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("subTask", task.getTaskDefinitionKey());
    Execution subTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertEquals("subTask", subTaskExecution.getActivityId());
    
    Execution subProcessExecution = runtimeService.createExecutionQuery().executionId(subTaskExecution.getParentId()).singleResult();
    assertEquals("subProcess", subProcessExecution.getActivityId());
    assertEquals(rootProcessInstance.getId(), subProcessExecution.getParentId());
    
    taskService.complete(task.getId());
    
    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, executionList.size());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTrue(childExecution.getId().equals(task.getExecutionId()) == false);
    
    Execution finalTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertEquals("theTask2", finalTaskExecution.getActivityId());
    
    assertEquals(rootProcessInstance.getId(), finalTaskExecution.getParentId());
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      assertEquals(8, historicActivities.size());
      
      List<String> activityIds = new ArrayList<String>();
      activityIds.add("theStart");
      activityIds.add("theTask1");
      activityIds.add("subProcess");
      activityIds.add("subStart");
      activityIds.add("subTask");
      activityIds.add("subEnd");
      activityIds.add("theTask2");
      activityIds.add("theEnd");
      
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        String activityId = historicActivityInstance.getActivityId();
        activityIds.remove(activityId);
        
        if ("theStart".equalsIgnoreCase(activityId) ||
          "theTask1".equalsIgnoreCase(activityId)) {
          
          assertEquals(childExecution.getId(), historicActivityInstance.getExecutionId());
          
        } else if ("theTask2".equalsIgnoreCase(activityId) ||
              "theEnd".equalsIgnoreCase(activityId)) {
              
          assertEquals(finalTaskExecution.getId(), historicActivityInstance.getExecutionId());
        
        } else if ("subStart".equalsIgnoreCase(activityId) ||
            "subTask".equalsIgnoreCase(activityId) ||
            "subEnd".equalsIgnoreCase(activityId)) {
          
          assertEquals(subTaskExecution.getId(), historicActivityInstance.getExecutionId());
        
        } else if ("subProcess".equalsIgnoreCase(activityId)) {
          assertEquals(subProcessExecution.getId(), historicActivityInstance.getExecutionId());
        }
      }
      
      assertEquals(0, activityIds.size());
    }
  }
  
  @Test
  @Deployment
  public void testSubProcessWithTimer() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessWithTimer");
    
    List<Execution> executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, executionList.size());
    
    Execution rootProcessInstance = null;
    Execution childExecution = null;
    
    for (Execution execution : executionList) {
      if (execution.getId().equals(execution.getProcessInstanceId())) {
        rootProcessInstance = execution;
        
        assertNull(execution.getActivityId());
      
      } else {
        childExecution = execution;
        
        assertTrue(execution.getId().equals(execution.getProcessInstanceId()) == false);
        assertEquals("theTask1", execution.getActivityId());
      }
    }
    
    assertNotNull(rootProcessInstance);
    assertNotNull(childExecution);

    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals(childExecution.getId(), task.getExecutionId());

    taskService.complete(task.getId());
    
    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(4, executionList.size());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertEquals("subTask", task.getTaskDefinitionKey());
    Execution subTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertEquals("subTask", subTaskExecution.getActivityId());
    
    Execution subProcessExecution = runtimeService.createExecutionQuery().executionId(subTaskExecution.getParentId()).singleResult();
    assertEquals("subProcess", subProcessExecution.getActivityId());
    assertEquals(rootProcessInstance.getId(), subProcessExecution.getParentId());
    
    taskService.complete(task.getId());
    
    executionList = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(2, executionList.size());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    assertTrue(childExecution.getId().equals(task.getExecutionId()) == false);
    
    Execution finalTaskExecution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
    assertEquals("theTask2", finalTaskExecution.getActivityId());
    
    assertEquals(rootProcessInstance.getId(), finalTaskExecution.getParentId());
    
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricActivityInstance> historicActivities = historyService.createHistoricActivityInstanceQuery()
          .processInstanceId(processInstance.getId())
          .list();
      assertEquals(8, historicActivities.size());
      
      List<String> activityIds = new ArrayList<String>();
      activityIds.add("theStart");
      activityIds.add("theTask1");
      activityIds.add("subProcess");
      activityIds.add("subStart");
      activityIds.add("subTask");
      activityIds.add("subEnd");
      activityIds.add("theTask2");
      activityIds.add("theEnd");
      
      for (HistoricActivityInstance historicActivityInstance : historicActivities) {
        String activityId = historicActivityInstance.getActivityId();
        activityIds.remove(activityId);
        
        if ("theStart".equalsIgnoreCase(activityId) ||
          "theTask1".equalsIgnoreCase(activityId)) {
          
          assertEquals(childExecution.getId(), historicActivityInstance.getExecutionId());
          
        } else if ("theTask2".equalsIgnoreCase(activityId) ||
              "theEnd".equalsIgnoreCase(activityId)) {
              
          assertEquals(finalTaskExecution.getId(), historicActivityInstance.getExecutionId());
        
        } else if ("subStart".equalsIgnoreCase(activityId) ||
            "subTask".equalsIgnoreCase(activityId) ||
            "subEnd".equalsIgnoreCase(activityId)) {
          
          assertEquals(subTaskExecution.getId(), historicActivityInstance.getExecutionId());
        
        } else if ("subProcess".equalsIgnoreCase(activityId)) {
          assertEquals(subProcessExecution.getId(), historicActivityInstance.getExecutionId());
        }
      }
      
      assertEquals(0, activityIds.size());
    }
  }
  
  @Test
  @Deployment
  public void testSubProcessEvents() {
    SubProcessEventListener listener = new SubProcessEventListener();
    processEngineConfiguration.getEventDispatcher().addEventListener(listener);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("subProcessEvents");
    
    Task task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    Execution subProcessExecution = runtimeService.createExecutionQuery().processInstanceId(processInstance.getId()).activityId("subProcess").singleResult();
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    task = taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
    taskService.complete(task.getId());
    
    assertProcessEnded(processInstance.getId());
    
    // Verify Events
    List<FlowableEvent> events = listener.getEventsReceived();
    assertEquals(2, events.size());
    
    FlowableActivityEvent event = (FlowableActivityEvent) events.get(0);
    assertEquals("subProcess", event.getActivityType());
    assertEquals(subProcessExecution.getId(), event.getExecutionId());
    
    event = (FlowableActivityEvent) events.get(1);
    assertEquals("subProcess", event.getActivityType());
    assertEquals(subProcessExecution.getId(), event.getExecutionId());
    
    processEngineConfiguration.getEventDispatcher().removeEventListener(listener);
  }
  
  public class SubProcessEventListener implements FlowableEventListener {
 
    private List<FlowableEvent> eventsReceived;

    public SubProcessEventListener() {
      eventsReceived = new ArrayList<FlowableEvent>();
    }

    public List<FlowableEvent> getEventsReceived() {
      return eventsReceived;
    }

    public void clearEventsReceived() {
      eventsReceived.clear();
    }

    @Override
    public void onEvent(FlowableEvent activitiEvent) {
      if (activitiEvent instanceof FlowableActivityEvent) {
        FlowableActivityEvent event = (FlowableActivityEvent) activitiEvent;
        if ("subProcess".equals(event.getActivityType())) {
          eventsReceived.add(event);
        }
      } else if (activitiEvent instanceof FlowableActivityCancelledEvent) {
        FlowableActivityCancelledEvent event = (FlowableActivityCancelledEvent) activitiEvent;
        if ("subProcess".equals(event.getActivityType())) {
          eventsReceived.add(event);
        }
      }
    }

    @Override
    public boolean isFailOnException() {
      return true;
    }
  }
}
