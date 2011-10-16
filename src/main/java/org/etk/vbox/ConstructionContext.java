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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 13, 2011  
 */
class ConstructionContext<T> {

  
  T currentReference;
  boolean constructing;

  List<DelegatingInvocationHandler<T>> invocationHandlers;

  T getCurrentReference() {
    return currentReference;
  }

  void removeCurrentReference() {
    this.currentReference = null;
  }

  void setCurrentReference(T currentReference) {
    this.currentReference = currentReference;
  }

  boolean isConstructing() {
    return constructing;
  }

  void startConstruction() {
    this.constructing = true;
  }

  void finishConstruction() {
    this.constructing = false;
    invocationHandlers = null;
  }

  Object createProxy(Class<? super T> expectedType) {
    // TODO: if I create a proxy which implements all the interfaces of
    // the implementation type, I'll be able to get away with one proxy
    // instance (as opposed to one per caller).

    if (!expectedType.isInterface()) {
      throw new DependencyException(expectedType.getName() + " is not an interface.");
    }

    if (invocationHandlers == null) {
      invocationHandlers = new ArrayList<DelegatingInvocationHandler<T>>();
    }

    DelegatingInvocationHandler<T> invocationHandler = new DelegatingInvocationHandler<T>();
    invocationHandlers.add(invocationHandler);

    return Proxy.newProxyInstance(expectedType.getClassLoader(), new Class[] { expectedType }, invocationHandler);
  }

  void setProxyDelegates(T delegate) {
    if (invocationHandlers != null) {
      for (DelegatingInvocationHandler<T> invocationHandler : invocationHandlers) {
        invocationHandler.setDelegate(delegate);
      }
    }
  }

  static class DelegatingInvocationHandler<T> implements InvocationHandler {

    T delegate;

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (delegate == null) {
        throw new IllegalStateException(
            "Not finished constructing. Please don't call methods on this"
                + " object until the caller's construction is complete.");
      }

      try {
        return method.invoke(delegate, args);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (IllegalArgumentException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw e.getTargetException();
      }
    }

    void setDelegate(T delegate) {
      this.delegate = delegate;
    }
  }
}
