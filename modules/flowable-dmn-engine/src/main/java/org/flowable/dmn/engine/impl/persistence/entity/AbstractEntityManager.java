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
package org.flowable.dmn.engine.impl.persistence.entity;

import org.flowable.dmn.engine.DmnEngineConfiguration;
import org.flowable.dmn.engine.impl.persistence.AbstractManager;
import org.flowable.engine.common.impl.persistence.entity.Entity;
import org.flowable.engine.common.impl.persistence.entity.EntityManager;
import org.flowable.engine.common.impl.persistence.entity.data.DataManager;

/**
 * @author Joram Barrez
 */
public abstract class AbstractEntityManager<EntityImpl extends Entity> extends AbstractManager implements EntityManager<EntityImpl> {

  public AbstractEntityManager(DmnEngineConfiguration dmnEngineConfiguration) {
    super(dmnEngineConfiguration);
  }
  
  /*
   * CRUD operations
   */
  
  @Override
  public EntityImpl findById(String entityId) {
    return getDataManager().findById(entityId);
  }
  
  @Override
  public EntityImpl create() {
    return getDataManager().create();
  }

  @Override
  public void insert(EntityImpl entity) {
    insert(entity, true);
  }
  
  @Override
  public void insert(EntityImpl entity, boolean fireEvent) {
    getDataManager().insert(entity);
  }
  
  @Override
  public EntityImpl update(EntityImpl entity) {
    return update(entity, true);
  }
  
  @Override
  public EntityImpl update(EntityImpl entity, boolean fireEvent) {
    EntityImpl updatedEntity = getDataManager().update(entity);
    
    return updatedEntity;
  }
  
  @Override
  public void delete(String id) {
    EntityImpl entity = findById(id);
    delete(entity);
  }
  
  @Override
  public void delete(EntityImpl entity) {
    delete(entity, true);
  }
  
  @Override
  public void delete(EntityImpl entity, boolean fireEvent) {
    getDataManager().delete(entity);
  }

  protected abstract DataManager<EntityImpl> getDataManager();

}
