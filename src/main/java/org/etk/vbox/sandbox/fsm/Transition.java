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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2011  
 */
public class Transition<C> {
  private final C letter;
  private final State<C> from;
  private final State<C> to;
  
  public Transition(final C letter, final State<C> from, final State<C> to) {
    this.letter = letter;
    this.from = from;
    this.to = to;
  }
  
  public C getLetter() {
    return this.letter;
  }
  
  public State<C> getTo() {
    return this.to;
  }
  
  public State<C> getFrom() {
    return this.from;
  }
  
  @Override
  public boolean equals(Object obj) {
    
    if ((obj instanceof Transition) == false) {
      return obj == null && this == null;
    }
    
    Transition transition = (Transition) obj;
    return transition.letter.equals(this.getLetter())
        && transition.getFrom().equals(this.getFrom()) && transition.getTo().equals(this.getTo()); 
  }

}
