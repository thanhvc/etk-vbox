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

import java.util.List;

import org.etk.vbox.utils.Types;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 7, 2011  
 */
public class TypeLiteralTest extends TestCase {

  public void testWithParameterizedType() {
    TypeLiteral<List<String>> a = new TypeLiteral<List<String>>();
    TypeLiteral<List<String>> b = new TypeLiteral<List<String>>(Types.listOf(String.class));
    assertEquals(a, b);
  }
  
  public void testWithParameterizedTypeArray() {
    TypeLiteral<String[]> a = new TypeLiteral<String[]>();
    assertEquals(String.class, a.getType());
    
  }

}
