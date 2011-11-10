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

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.etk.vbox.matcher.Matcher;

/**
 * Created by The eXo Platform SAS Author : thanh_vucong
 * thanhvucong.78@gmail.com Nov 8, 2011
 */
public class MethodAspect {

  private final Matcher<? super Class<?>> classMatcher;

  private final Matcher<? super Method>   methodMatcher;

  private final List<MethodInterceptor>   interceptors;

  /**
   * @param classMatcher matches classes the interceptor should apply to. For
   *          example: {@code only(Runnable.class)}.
   * @param methodMatcher matches methods the interceptor should apply to. For
   *          example: {@code annotatedWith(Transactional.class)}.
   * @param interceptors to apply
   */
  public MethodAspect(Matcher<? super Class<?>> classMatcher,
               Matcher<? super Method> methodMatcher,
               List<MethodInterceptor> interceptors) {
    this.classMatcher = checkNotNull(classMatcher, "class matcher");
    this.methodMatcher = checkNotNull(methodMatcher, "method matcher");
    this.interceptors = checkNotNull(interceptors, "interceptors");
  }

  public MethodAspect(Matcher<? super Class<?>> classMatcher,
               Matcher<? super Method> methodMatcher,
               MethodInterceptor... interceptors) {
    this(classMatcher, methodMatcher, Arrays.asList(interceptors));
  }

  public boolean matches(Class<?> clazz) {
    return classMatcher.matches(clazz);
  }

  public boolean matches(Method method) {
    return methodMatcher.matches(method);
  }

  public List<MethodInterceptor> interceptors() {
    return interceptors;
  }
}
