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
package org.etk.vbox.utils;

/**
 * 
 * Object Utilities
 * 
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 1, 2011  
 */
public class Objects {
  
  /**
   * Detects the null values.
   * 
   * @param <T>
   * @param t value
   * @param message to diaplay in the event of a null
   * @return
   */
  public static <T> T nonNull(T t, String message) {
    if (t == null) {
      throw new NullPointerException(message);
    }
    
    return t;
  }

}
