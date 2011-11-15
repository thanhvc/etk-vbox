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

import static org.etk.vbox.internal.utils.Objects.nonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 14, 2011  
 */
public class MyListenerKey<T> {

  /**
   * Default binding name.
   */
  public static final String DEFAULT_METHOD_NAME = "default-method";
  
  final Class<T> rawType;
  final String typeString;
  final Type type;

  protected MyListenerKey() {
    Type superClass = getClass().getGenericSuperclass();
    
    if (superClass instanceof Class) {
      throw new RuntimeException("Missing type parameter.");
    }
    
    this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    this.rawType = getRawType(type);
    this.typeString = toString(type);
  }
  
  private MyListenerKey(Type type) {
    this.type = nonNull(type, "type");
    this.rawType = getRawType(type);
    this.typeString = toString(type);
  }
  
  /**
   * Calls {@code getName()} on {@code Class}es and {@code toString()} on other
   * {@code Type}s.
   * 
   * @param type
   * @return
   */
  private static String toString(Type type) {
    return (type instanceof Class<?>) ? ((Class<?>) type).getName() : type.toString();
  }

  private static <T> Class<T> getRawType(Type type) {
    if (type instanceof Class<?>) {
   // type is a normal class.
      return (Class<T>) type;
    } else {
      if (!(type instanceof ParameterizedType)) {
        unexpectedType(type, ParameterizedType.class);
      }
      ParameterizedType parameterizedType = (ParameterizedType) type;
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class<?>)) {
        unexpectedType(rawType, Class.class);
      }
      return (Class<T>) rawType;
    }
  }
  
  static void unexpectedType(Type token, Class<?> expected) {
    throw new AssertionError(
      "Unexpected type. Expected: " + expected.getName()
      + ", got: " + token.getClass().getName()
      + ", for type token: " + token.toString() + ".");
  }
  
  public Class<T> getRawType() {
    return rawType;
  }
  
  
  public Type getType() {
    return type;
  }

  public int hashCode() {
    return typeString.hashCode();
  }
  
  public MyListenerKey<?> rawKey() {
    return MyListenerKey.newInstance(rawType);
  }

  /**
   * Compares the binding name and type. Uses {@code String} representations
   * to compare types as the reflection API doesn't give us a lot of options
   * when it comes to comparing parameterized types.
   *
   * @inheritDoc
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MyListenerKey)) {
      return false;
    }
    MyListenerKey other = (MyListenerKey) o;
    return typeString.equals(other.typeString);
  }

  public String toString() {
    return "MyListenerKey[type=" + typeString + "']";
  }

  /**
   * Constructs a key from a raw type.
   */
  public static <T> MyListenerKey<T> newInstance(Class<T> type) {
    return new MyListenerKey<T>(type) {};
  }

   /**
   * Constructs a key from a type.
   */
  public static MyListenerKey<?> newInstance(Type type) {
    return new MyListenerKey<Object>(type) {};
  }
}
