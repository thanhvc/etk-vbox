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
package org.etk.vbox.intercept;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.etk.vbox.MethodAspect;
import org.etk.vbox.matcher.Matcher;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 8, 2011  
 */
public class ProxyFactoryBuilder {

  final List<MethodAspect> methodAspects = new ArrayList<MethodAspect>();
  
  /**
   * Applies the given method interceptor to the methods matched by the class and method queries.
   * @param classMatcher
   * @param methodMatcher
   * @param interceptors
   * @return
   */
  public ProxyFactoryBuilder intercept(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher, MethodInterceptor...interceptors) {
    methodAspects.add(new MethodAspect(classMatcher, methodMatcher, interceptors));
    return this;
  }
  
  /**
   * Creates a {@code ProxyFactory}
   * @return
   */
  public ProxyFactory create() {
    return new ProxyFactory(new ArrayList<MethodAspect>(methodAspects));
  }
}