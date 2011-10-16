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

import java.util.LinkedList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2011  
 */
public class State<C> {

  private final String caption;
  
  private final List<Transition<C>> transitions;
  
  private final boolean isFinal;
  
  
  private State(String caption, boolean isEnd) {
    this.caption = caption;
    this.transitions = new LinkedList<Transition<C>>();
    this.isFinal = isEnd;
  }
  
  public static State newInstance(String caption, boolean isFinal) {
    return new State(caption, isFinal);
  }
  
  public void connectTo(C letter, State<C> target) {
    Transition newTransition = new Transition(letter, this, target);
    
    for (Transition tran : transitions) {
      if (newTransition.equals(tran)) {
        return;
      }
    }
    this.transitions.add(newTransition);
  }
  
  public State<C> findTargetState(C letter) {
    for (Transition tran : transitions) {
      if (tran.getLetter().equals(letter)) {
        return tran.getTo();
      }
    }

    return null;
  }

  public String getCaption() {
    return caption;
  }

  public List<Transition<C>> getTransitions() {
    return transitions;
  }

  public boolean isFinal() {
    return isFinal;
  }
  
  @Override
  public int hashCode() {
    return ("" + caption).hashCode();
  }
  
  
}
