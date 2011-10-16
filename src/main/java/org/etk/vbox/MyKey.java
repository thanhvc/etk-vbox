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

import org.etk.vbox.MyKey;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 13, 2011  
 */
/**
 * Dependency mapping key. Uniquely identified by the required type and name.
 *
 */
class MyKey<T> {

  final Class<T> type;
  final String name;
  final int hashCode;

  private MyKey(Class<T> type, String name) {
    if (type == null) {
      throw new NullPointerException("Type is null.");
    }
    if (name == null) {
      throw new NullPointerException("Name is null.");
    }

    this.type = type;
    this.name = name;

    hashCode = type.hashCode() * 31 + name.hashCode();
  }

  Class<T> getType() {
    return type;
  }

  String getName() {
    return name;
  }

  public int hashCode() {
    return hashCode;
  }

  public boolean equals(Object o) {
    if (!(o instanceof MyKey)) {
      return false;
    }
    if (o == this) {
      return true;
    }
    MyKey other = (MyKey) o;
    return name.equals(other.name) && type.equals(other.type);
  }

  public String toString() {
    return "[type=" + type.getName() + ", name='" + name + "']";
  }

  static <T> MyKey<T> newInstance(Class<T> type, String name) {
    return new MyKey<T>(type, name);
  }
}
