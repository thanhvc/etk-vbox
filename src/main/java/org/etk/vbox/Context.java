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

import java.lang.reflect.Member;

import org.etk.vbox.ModulerService;
import org.etk.vbox.MyInject;
import org.etk.vbox.MyScope;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 14, 2011  
 */
public interface Context {

  /**
   * Gets the {@link ModulerService}.
   */
  ModulerService getApplication();

  /**
   * Gets the current scope strategy. See {@link
   * ModulerService#setScopeStrategy(MyScope.Strategy)}.
   *
   * @throws IllegalStateException if no strategy has been set
   */
  MyScope.Strategy getScopeStrategy();

  /**
   * Gets the field, method or constructor which is being injected. Returns
   * {@code null} if the object currently being constructed is pre-loaded as
   * a singleton or requested from {@link ModulerService#getInstance(Class)}.
   */
  Member getMember();

  /**
   * Gets the type of the field or parameter which is being injected.
   */
  Class<?> getType();

  /**
   * Gets the name of the injection specified by {@link MyInject#value()}.
   */
  String getName();
}

