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
package org.flowable.app.service.editor;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.flowable.app.domain.editor.AbstractModel;
import org.flowable.app.domain.editor.AppDefinition;
import org.flowable.app.domain.editor.AppModelDefinition;
import org.flowable.app.domain.editor.Model;
import org.flowable.app.repository.editor.ModelRepository;
import org.flowable.app.service.api.AppDefinitionService;
import org.flowable.app.service.api.ModelService;
import org.flowable.app.service.exception.BadRequestException;
import org.flowable.app.service.exception.InternalServerErrorException;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.SubProcess;
import org.flowable.bpmn.model.UserTask;
import org.flowable.dmn.model.DmnDefinition;
import org.flowable.dmn.xml.converter.DmnXMLConverter;
import org.flowable.editor.dmn.converter.DmnJsonConverter;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.idm.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Can't merge this with {@link AppDefinitionService}, as it doesn't have visibility of domain models needed to do the publication.
 * 
 * @author jbarrez
 */
@Service
@Transactional
public class AppDefinitionPublishService {

  private static final Logger logger = LoggerFactory.getLogger(AppDefinitionPublishService.class);

  @Autowired
  protected ModelService modelService;
  
  @Autowired
  protected RepositoryService repositoryService;
  
  @Autowired
  protected ModelRepository modelRepository;
  
  @Autowired
  protected ObjectMapper objectMapper;
  
  protected DmnJsonConverter dmnJsonConverter = new DmnJsonConverter();
  protected DmnXMLConverter dmnXMLConverter = new DmnXMLConverter();

  public void publishAppDefinition(String comment, Model appDefinitionModel, User user) {

    // Create new version of the app model
    modelService.createNewModelVersion(appDefinitionModel, comment, user);

    AppDefinition appDefinition = resolveAppDefinition(appDefinitionModel);

    if (CollectionUtils.isNotEmpty(appDefinition.getModels())) {
      DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
          .name(appDefinitionModel.getName())
          .key(appDefinitionModel.getKey());
      
      deploymentBuilder.addString(appDefinitionModel.getKey() + ".app", getAppDefinitionJson(appDefinition));
      
      Map<String, Model> formMap = new HashMap<String, Model>();
      Map<String, Model> decisionTableMap = new HashMap<String, Model>();

      for (AppModelDefinition appModelDef : appDefinition.getModels()) {

        AbstractModel processModel = modelService.getModel(appModelDef.getId());
        if (processModel == null) {
          logger.error("Model " + appModelDef.getId() + " for app definition " + appDefinitionModel.getId() + " could not be found");
          throw new BadRequestException("Model for app definition could not be found");
        }
        
        List<Model> referencedModels = modelRepository.findByParentModelId(processModel.getId());
        for (Model childModel : referencedModels) {
          if (Model.MODEL_TYPE_FORM == childModel.getModelType()) {
            formMap.put(childModel.getId(), childModel);
            
          } else if (Model.MODEL_TYPE_DECISION_TABLE == childModel.getModelType()) {
            decisionTableMap.put(childModel.getId(), childModel);
          }
        }

        BpmnModel bpmnModel = modelService.getBpmnModel(processModel, formMap, decisionTableMap);
        Map<String, StartEvent> startEventMap = processNoneStartEvents(bpmnModel);
        
        for (Process process : bpmnModel.getProcesses()) {
          processUserTasks(process.getFlowElements(), process, startEventMap);
        }
        
        byte[] modelXML = modelService.getBpmnXML(bpmnModel);
        deploymentBuilder.addInputStream(processModel.getKey().replaceAll(" ", "") + ".bpmn", new ByteArrayInputStream(modelXML));
      }
      
      if (formMap.size() > 0) {
        for (String formId : formMap.keySet()) {
          Model formInfo = formMap.get(formId);
          deploymentBuilder.addString("form-" + formInfo.getKey() + ".form", formInfo.getModelEditorJson());
        }
      }
      
      if (decisionTableMap.size() > 0) {
        for (String decisionTableId : decisionTableMap.keySet()) {
          Model decisionTableInfo = decisionTableMap.get(decisionTableId);
          try {
            JsonNode decisionTableNode = objectMapper.readTree(decisionTableInfo.getModelEditorJson());
            DmnDefinition dmnDefinition = dmnJsonConverter.convertToDmn(decisionTableNode, decisionTableInfo.getId(), 
                decisionTableInfo.getVersion(), decisionTableInfo.getLastUpdated());
            byte[] dmnXMLBytes = dmnXMLConverter.convertToXML(dmnDefinition);
            deploymentBuilder.addBytes("dmn-" + decisionTableInfo.getKey() + ".dmn", dmnXMLBytes);
            
          } catch (Exception e) {
            logger.error("Error converting decision table to XML " + decisionTableInfo.getName(), e);
            throw new InternalServerErrorException("Error converting decision table to XML " + decisionTableInfo.getName());
          }
        }
      }

      deploymentBuilder.deploy();
    }
  }
  
  protected Map<String, StartEvent> processNoneStartEvents(BpmnModel bpmnModel) {
    Map<String, StartEvent> startEventMap = new HashMap<String, StartEvent>();
    for (Process process : bpmnModel.getProcesses()) {
      for (FlowElement flowElement : process.getFlowElements()) {
        if (flowElement instanceof StartEvent) {
          StartEvent startEvent = (StartEvent) flowElement;
          if (CollectionUtils.isEmpty(startEvent.getEventDefinitions())) {
            if (StringUtils.isEmpty(startEvent.getInitiator())) {
              startEvent.setInitiator("initiator");
            }
            startEventMap.put(process.getId(), startEvent);
            break;
          }
        }
      }
    }
    return startEventMap;
  }
  
  protected void processUserTasks(Collection<FlowElement> flowElements, Process process, Map<String, StartEvent> startEventMap) {

    for (FlowElement flowElement : flowElements) {
      if (flowElement instanceof UserTask) {
        UserTask userTask = (UserTask) flowElement;
        if ("$INITIATOR".equals(userTask.getAssignee())) {
          if (startEventMap.get(process.getId()) != null) {
            userTask.setAssignee("${" + startEventMap.get(process.getId()).getInitiator() + "}");
          }
        }

      } else if (flowElement instanceof SubProcess) {
        processUserTasks(((SubProcess) flowElement).getFlowElements(), process, startEventMap);
      }
    }
  }
  
  protected AppDefinition resolveAppDefinition(Model appDefinitionModel) {
    try {
      AppDefinition appDefinition = objectMapper.readValue(appDefinitionModel.getModelEditorJson(), AppDefinition.class);
      return appDefinition;
      
    } catch (Exception e) {
      logger.error("Error deserializing app " + appDefinitionModel.getId(), e);
      throw new InternalServerErrorException("Could not deserialize app definition");
    }
  }

  protected String getAppDefinitionJson(AppDefinition appDefinition) {
    ObjectNode appDefinitionNode = objectMapper.createObjectNode();
    appDefinitionNode.put("theme", appDefinition.getTheme());
    appDefinitionNode.put("icon", appDefinition.getIcon());
    return appDefinitionNode.toString();
  }
}
