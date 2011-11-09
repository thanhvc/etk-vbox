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

import java.lang.reflect.Member;

/**
 * Throw when a constant type conversion error occurs.
 * 
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 1, 2011  
 */
public class ConstantConversionException extends DependencyException {
  public ConstantConversionException(Member member, MyKey<?> key, String value, String reason) {
    super(createMessage(value, key, member, reason));
    
  }
  
  public ConstantConversionException(Member member, MyKey<?> key, String value, Throwable reason) {
    this(member, key, value, reason.toString());
  }
  
  private static String createMessage(String value, MyKey<?> key, Member member, String reason) {
    return member == null ? "Error converting '" + value + "' to " + key.getType().getSimpleName()
                             + " while getting dependency named '" + key.getName() + "'. Reason: "
                             + reason
                         : "Error converting '" + value + "' to " + key.getType().getSimpleName()
                             + " while injecting " + member.getName() + " with dependency named '"
                             + key.getName() + "' in " + member.getDeclaringClass().getSimpleName()
                             + ". Reason: " + reason;

  }

}
