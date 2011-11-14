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
package org.etk.vbox.test;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.etk.vbox.MyKey;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 12, 2011  
 */
public class MyKeyTest extends TestCase {
  
  public void foo(List<String> a, List<String> b) {}
  
  public void testEquality() throws Exception {
    Method m = getClass().getMethod("foo", List.class, List.class);
    Type[] types = m.getGenericParameterTypes();
    assertEquals(types[0], types[1]);
    MyKey<List<String>> k = new MyKey<List<String>>() {};
    assertEquals(types[0], k.getType());
    assertFalse(types[0].equals(new MyKey<List<Integer>>() {}.getType()));

  }

}
