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
package org.etk.vbox.sandbox.fsm.event;

import org.etk.vbox.sandbox.fsm.State;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2011  
 */
public class FSMEvent<C, A> {

  private final State<C> machineState;
  
  private final A machineStack;
  
  private final String name;
  
  public FSMEvent(String name, State<C> state, A stack) {
    this.name = name;
    this.machineState = state;
    this.machineStack = stack;
  }
  
  public FSMEvent(State<C> state, A stack) {
    this(state.getCaption(), state, stack);
  }
  
  @Override
  public boolean equals(Object obj) {

    if (!(obj instanceof FSMEvent)) {
      return false;
    }

    FSMEvent e = (FSMEvent<C, A>) obj;

    return (this == e)
        || (this.machineState.equals(e.machineState) && this.machineStack.equals(e.machineStack));
  }

  public String getName() {
    return name;
  }
  
  
  
  
}
