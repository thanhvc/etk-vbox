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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Class utilities.
 */
public final class Classes {

  public static boolean isInnerClass(Class<?> clazz) {
    return !Modifier.isStatic(clazz.getModifiers())
        && clazz.getEnclosingClass() != null;
  }

  public static boolean isConcrete(Class<?> clazz) {
    int modifiers = clazz.getModifiers();
    return !clazz.isInterface() && !Modifier.isAbstract(modifiers);
  }

  /**
   * Formats a member as concise string, such as {@code java.util.ArrayList.size},
   * {@code java.util.ArrayList<init>()} or {@code java.util.List.remove()}.
   */
  public static String toString(Member member) {
    Class<? extends Member> memberType = Classes.memberType(member);
  
    if (memberType == Method.class) {
      return member.getDeclaringClass().getName() + "." + member.getName() + "()";
    } else if (memberType == Field.class) {
      return member.getDeclaringClass().getName() + "." + member.getName();
    } else if (memberType == Constructor.class) {
      return member.getDeclaringClass().getName() + ".<init>()";
    } else {
      throw new AssertionError();
    }
  }

  /**
   * Returns {@code Field.class}, {@code Method.class} or {@code Constructor.class}.
   */
  public static Class<? extends Member> memberType(Member member) {
    checkNotNull(member, "member");
  
    if (member instanceof Field) {
      return Field.class;
  
    } else if (member instanceof Method) {
      return Method.class;
  
    } else if (member instanceof Constructor) {
      return Constructor.class;
  
    } else {
      throw new IllegalArgumentException(
          "Unsupported implementation class for Member, " + member.getClass());
    }
  }
}

