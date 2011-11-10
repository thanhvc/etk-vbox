/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.etk.vbox;

import java.util.HashMap;
import java.util.Map;

import org.etk.vbox.ConstructionContext;
import org.etk.vbox.ExternalContext;
import org.etk.vbox.ModulerService;
import org.etk.vbox.ModulerServiceImpl;
import org.etk.vbox.MyScope;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 13, 2011  
 */
class InspectorContext {

  final ModulerServiceImpl module;
  final Map<Object, ConstructionContext<?>> constructionContexts = new HashMap<Object, ConstructionContext<?>>();
  MyScope.Strategy scopeStrategy;
  ExternalContext<?> externalContext;
  
  InspectorContext(ModulerServiceImpl module) {
    this.module = module;
  }
  
  public ModulerServiceImpl getModuleServiceImpl() {
    return module;
  }
  
  public ModulerService getModuleService() {
    return module;
  }
  
  @SuppressWarnings("unchecked")
  <T> ExternalContext<T> getExternalContext() {
    return (ExternalContext<T>) externalContext;
  }

  void setExternalContext(ExternalContext<?> externalContext) {
    this.externalContext = externalContext;
  }
  
  /**
   * Gets the {@link ConstructionContext} from Key
   * @param <T>
   * @param key key which specific the {@link ConstructionContext} to put in the constructionContexts HashMap
   * @return
   */
  @SuppressWarnings("unchecked")
  <T> ConstructionContext<T> getConstructionContext(Object key) {
    //get construction context from Hashmap by Key.
    ConstructionContext<T> constructionContext = (ConstructionContext<T>) constructionContexts.get(key);
    
    if (constructionContext == null) {
      constructionContext = new ConstructionContext<T>();
      constructionContexts.put(key, constructionContext);
    }
    return constructionContext;
  }
}
