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
package org.etk.vbox.internal.utils;


import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Helps with {@code toString()} methods.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class ToStringBuilder {

  // Linked hash map ensures ordering.
  final Map<String, Object> map = new LinkedHashMap<String, Object>();

  final String name;

  public ToStringBuilder(String name) {
    this.name = name;
  }

  public ToStringBuilder(Class type) {
    this.name = type.getSimpleName();
  }

  public ToStringBuilder add(String name, Object value) {
    if (map.put(name, value) != null) {
      throw new RuntimeException("Duplicate names: " + name);
    }
    return this;
  }

  public String toString() {
    return name + map.toString();
  }
}

