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

import java.util.concurrent.Callable;

import org.etk.vbox.InspectorContext;
import org.etk.vbox.InternalInspector;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 13, 2011  
 */
public enum MyScope {
  
  DEFAULT {

    @Override
    <T> InternalInspector<? extends T> scopeFactory(Class<T> type,
                                                  String name,
                                                  InternalInspector<? extends T> factory) {
      return factory;
    }
    
  },
  SINGLETON {

    @Override
    <T> InternalInspector<? extends T> scopeFactory(Class<T> type,
                                                  String name,
                                                  final InternalInspector<? extends T> factory) {
      return new InternalInspector<T>() {
        T instance;

        @Override
        public T create(InspectorContext context) {
          if (instance == null) {
            instance = factory.create(context);
          }
          return instance;
        }

      };
    }
    
  };
  
  <T> Callable<? extends T> toCallable(final InspectorContext context, final InternalInspector<? extends T> factory) {
    return new Callable<T>() {
      public T call() throws Exception {
        return factory.create(context);
      }
    };
  }
  /**
   * Wraps factory with scoping logic.
   */
  abstract <T> InternalInspector<? extends T> scopeFactory(Class<T> type,
                                                         String name,
                                                         InternalInspector<? extends T> factory);

  /**
   * Pluggable scoping strategy. Enables users to provide custom implementations of request, session, and wizard scopes.
   * @author thanh_vucong
   *
   */
  public interface Strategy {
    /**
     * Finds an object for the given type and name in the request scope.
     * Creates a new object if necessary using the given factory.
     * @param <T>
     * @param type
     * @param name
     * @param factory
     * @return
     * @throws Exception
     */
    <T> T findInRequest(Class<T> type, String name, Callable<? extends T> factory) throws Exception;
    
    /**
     * Finds an object for the given type and name in the session scope.
     * Creates a new object if necessary using the given factory.
     * @param <T>
     * @param type
     * @param name
     * @param factory
     * @return
     * @throws Exception
     */
    <T> T findSession(Class<T> type, String name, Callable<? extends T> factory) throws Exception;
    
    /**
     * Finds an object for the given type and name in the wizard scope.
     * Creates a new object if necessary using the given factory.
     */
    <T> T findInWizard(Class<T> type, String name, Callable<? extends T> factory) throws Exception;
  }
}
