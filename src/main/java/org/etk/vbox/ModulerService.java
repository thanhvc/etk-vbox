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

import org.etk.vbox.MyScope;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 12, 2011  
 */
public interface ModulerService {

  /**
   * Default dependency name.
   */
  String DEFAULT_NAME = "default";

  /**
   * Injects dependencies into the fields and methods of an existing object.
   */
  void inject(Object o);

  /**
   * Creates and injects a new instance of type {@code implementation}.
   */
  <T> T inject(Class<T> implementation);

  /**
   * Gets an instance of the given dependency which was declared in
   * {@link ModuleAssembler}.
   */
  <T> T getInstance(Class<T> type, String name);

  /**
   * Convenience method.&nbsp;Equivalent to {@code getInstance(type,
   * DEFAULT_NAME)}.
   */
  <T> T getInstance(Class<T> type);

  /**
   * Sets the scope strategy for the current thread.
   */
  void setScopeStrategy(MyScope.Strategy scopeStrategy);

  /**
   * Removes the scope strategy for the current thread.
   */
  void removeScopeStrategy();
  
  
}
