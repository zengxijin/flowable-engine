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
package org.flowable.form.engine.impl.util;

import org.flowable.engine.common.api.FlowableException;
import org.flowable.form.engine.FormEngineConfiguration;
import org.flowable.form.engine.impl.context.Context;
import org.flowable.form.engine.impl.persistence.deploy.DeploymentManager;
import org.flowable.form.engine.impl.persistence.deploy.FormDefinitionCacheEntry;
import org.flowable.form.engine.impl.persistence.entity.FormDefinitionEntity;
import org.flowable.form.engine.impl.persistence.entity.FormDefinitionEntityManager;
import org.flowable.form.model.FormModel;

/**
 * A utility class that hides the complexity of {@link FormDefinitionEntity} and {@link Decision} lookup. 
 * Use this class rather than accessing the decision table cache or {@link DeploymentManager} directly.
 * 
 * @author Joram Barrez
 * @author Tijs Rademakers
 */
public class FormUtil {
  
  public static FormDefinitionEntity getFormDefinitionEntity(String formDefinitionId) {
    return getFormDefinitionEntity(formDefinitionId, false);
  }

  public static FormDefinitionEntity getFormDefinitionEntity(String formDefinitionId, boolean checkCacheOnly) {
    if (checkCacheOnly) {
      FormDefinitionCacheEntry cacheEntry = Context.getFormEngineConfiguration().getFormDefinitionCache().get(formDefinitionId);
      if (cacheEntry != null) {
        return cacheEntry.getFormDefinitionEntity();
      }
      return null;
    } else {
      // This will check the cache in the findDeployedFormDefinitionById method
      return Context.getFormEngineConfiguration().getDeploymentManager().findDeployedFormDefinitionById(formDefinitionId);
    }
  }

  public static FormModel getFormDefinition(String formDefinitionId) {
    FormEngineConfiguration formEngineConfiguration = Context.getFormEngineConfiguration();
    DeploymentManager deploymentManager = formEngineConfiguration.getDeploymentManager();
      
    // This will check the cache in the findDeployedFormDefinitionById and resolveFormDefinition method
    FormDefinitionEntity formDefinitionEntity = deploymentManager.findDeployedFormDefinitionById(formDefinitionId);
    FormDefinitionCacheEntry cacheEntry = deploymentManager.resolveFormDefinition(formDefinitionEntity);
    return formEngineConfiguration.getFormJsonConverter().convertToFormModel(cacheEntry.getFormDefinitionJson(), 
        cacheEntry.getFormDefinitionEntity().getId(), cacheEntry.getFormDefinitionEntity().getVersion());
  }
  
  public static FormModel getFormDefinitionFromCache(String formId) {
    FormEngineConfiguration formEngineConfiguration = Context.getFormEngineConfiguration();
    FormDefinitionCacheEntry cacheEntry = formEngineConfiguration.getFormDefinitionCache().get(formId);
    if (cacheEntry != null) {
      return formEngineConfiguration.getFormJsonConverter().convertToFormModel(cacheEntry.getFormDefinitionJson(), 
          cacheEntry.getFormDefinitionEntity().getId(), cacheEntry.getFormDefinitionEntity().getVersion());
    }
    return null;
  }
  
  public static FormDefinitionEntity getFormDefinitionFromDatabase(String formDefinitionId) {
    FormDefinitionEntityManager formDefinitionEntityManager = Context.getFormEngineConfiguration().getFormDefinitionEntityManager();
    FormDefinitionEntity formDefinition = formDefinitionEntityManager.findById(formDefinitionId);
    if (formDefinition == null) {
      throw new FlowableException("No form definitionfound with id " + formDefinitionId);
    }
    
    return formDefinition;
  }
}
