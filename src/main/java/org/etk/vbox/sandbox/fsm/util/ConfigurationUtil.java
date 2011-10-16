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
package org.etk.vbox.sandbox.fsm.util;

import java.util.HashMap;
import java.util.Map;

import org.etk.vbox.sandbox.fsm.annotation.FSMConfiguration;
import org.etk.vbox.sandbox.fsm.annotation.ListenerConfig;
import org.etk.vbox.sandbox.fsm.event.TransitionEventListener;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 30, 2011  
 */
public class ConfigurationUtil {

  public static <C, A> Map<String, TransitionEventListener<C, A>> getListeners(FSMConfiguration config) {
    Map<String, TransitionEventListener<C, A>> listenerMap = new HashMap<String, TransitionEventListener<C, A>>();

    for (ListenerConfig listenerConfig : config.listeners()) {
      try {
        String id = listenerConfig.id();
        Class<TransitionEventListener> clazz = listenerConfig.type();
        TransitionEventListener<C, A> listener = (TransitionEventListener<C, A>) clazz.newInstance();
        listenerMap.put(id, listener);
      } catch (Exception ex) {
        // TODO: Add the logging system
      }
    }
    return listenerMap;
  }
}
