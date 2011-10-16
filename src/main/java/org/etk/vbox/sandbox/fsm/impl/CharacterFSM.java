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
package org.etk.vbox.sandbox.fsm.impl;

import java.io.IOException;
import java.io.Reader;

import org.etk.vbox.sandbox.fsm.FSM;
import org.etk.vbox.sandbox.fsm.InputTape;
import org.etk.vbox.sandbox.fsm.State;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 30, 2011  
 */
public abstract class CharacterFSM extends FSM<Character, Character> {

  public CharacterFSM(State<Character> startState) {
    super(startState);
  }

  /**
   * Makes the Finite State Machine Reader.
   * @param reader
   * @return
   * @throws IOException
   */
  public boolean accept(final Reader reader) throws IOException {
    //Initialize the inputTape reader
    InputTape<Character> inputTape = new InputTape<Character>() {
      private int c = -1;

      @Override
      public boolean hasNext() throws IOException {
        c = reader.read();
        return c != -1;
      }

      @Override
      public Character next() throws IOException {
        if (c == -1) {
          throw new IllegalStateException();
        }
        return new Character((char) c);
      }
    };
    return super.accept(inputTape);
  }

}
