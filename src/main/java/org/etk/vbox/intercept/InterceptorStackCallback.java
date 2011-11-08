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

import net.sf.cglib.proxy.MethodProxy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.AccessibleObject;
import java.util.List;

/**
 * Intercepts a method with a stack of interceptors.
 * 
 * @author crazybob@google.com (Bob Lee)
 */
class InterceptorStackCallback implements net.sf.cglib.proxy.MethodInterceptor {

  final MethodInterceptor[] interceptors;

  final Method              method;

  public InterceptorStackCallback(Method method, List<MethodInterceptor> interceptors) {
    this.method = method;
    this.interceptors = interceptors.toArray(new MethodInterceptor[interceptors.size()]);
  }

  public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
    return new InterceptedMethodInvocation(proxy, methodProxy, arguments).proceed();
  }

  class InterceptedMethodInvocation implements MethodInvocation {

    final Object      proxy;

    final Object[]    arguments;

    final MethodProxy methodProxy;

    int               index = -1;

    public InterceptedMethodInvocation(Object proxy, MethodProxy methodProxy, Object[] arguments) {
      this.proxy = proxy;
      this.methodProxy = methodProxy;
      this.arguments = arguments;
    }

    public Object proceed() throws Throwable {
      try {
        index++;
        return index == interceptors.length ? methodProxy.invokeSuper(proxy, arguments)
                                           : interceptors[index].invoke(this);
      } finally {
        index--;
      }
    }

    public Method getMethod() {
      return method;
    }

    public Object[] getArguments() {
      return arguments;
    }

    public Object getThis() {
      return proxy;
    }

    public AccessibleObject getStaticPart() {
      return getMethod();
    }
  }
}
