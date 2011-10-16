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
package org.etk.vbox.sandbox.fsm;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.etk.vbox.sandbox.fsm.annotation.FSMConfiguration;
import org.etk.vbox.sandbox.fsm.event.TransitionEventListener;
import org.etk.vbox.sandbox.fsm.util.ConfigurationUtil;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2011  
 */
public abstract class FSM<C, A> {

  private final State<C> startActivity;
  
  private State<C> currentActivity;
  
  private final Stack<A> stackMemory;
  
  //Defines the Transition Table
  private Map<String, TransitionEventListener<C, A>> transitionTable;
  
  private C entryType;
  
  public FSM(State<C> startActivity) {
    this.startActivity = startActivity;
    this.stackMemory = new Stack<A>();
    this.currentActivity = startActivity;
    this.transitionTable = new HashMap<String, TransitionEventListener<C,A>>();
    processConfig();
  }

  abstract protected void makeTransition();

  final public boolean accept(InputTape<C> inputTape) throws IOException {
    while (inputTape.hasNext()) {
      entryType = inputTape.next();
      makeTransition();
      if (currentActivity == null) {
        return false;
      } else if (currentActivity.isFinal()) {
        return true;
      }
    }

    return currentActivity.isFinal();
  }

  public void reset() {
    this.currentActivity = startActivity;
  }

  public A getTop() {
    return stackMemory.peek();
  }

  public void pushTop(A item) {
    stackMemory.push(item);
  }

  public void popTop() {
    stackMemory.pop();
  }

  public C getCommingInput() {
    return entryType;
  }

  public State<C> getCurrentState() {
    return currentActivity;
  }

  public void setCurrentState(State<C> nextState) {
    this.currentActivity = nextState;
  }

  private void processConfig() {
    FSMConfiguration config = this.getClass().getAnnotation(FSMConfiguration.class);
    if (config != null) {
      this.transitionTable = ConfigurationUtil.getListeners(config);
    }
  }
  
  
}
