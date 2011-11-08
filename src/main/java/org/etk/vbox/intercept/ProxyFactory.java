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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.etk.vbox.MethodAspect;
import org.etk.vbox.spi.ConstructionProxy;
import org.etk.vbox.spi.ConstructionProxyFactory;
import org.etk.vbox.spi.DefaultConstructionProxyFactory;
import org.etk.vbox.utils.DataCache;

import com.google.common.collect.Lists;

/**
 * Proxies classes applying interceptors to methods as specified 
 * in {@link ProxyFactoryBuilder}
 * 
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 8, 2011  
 */
public class ProxyFactory implements ConstructionProxyFactory {
  final List<MethodAspect> methodAspects;
  final ConstructionProxyFactory defaultFactory = new DefaultConstructionProxyFactory();

  ProxyFactory(List<MethodAspect> methodAspects) {
    this.methodAspects = methodAspects;
  }
  
  Map<Constructor<?>, ConstructionProxy<?>> constructionProxies = new DataCache<Constructor<?>, ConstructionProxy<?>>() {
    protected ConstructionProxy<?> create(Constructor<?> constructor) {
      return createConstructionProxy(constructor);
    }
  };
  
  <T> ConstructionProxy<T> createConstructionProxy(Constructor<T> constructor) {
    Class<T> declaringClass = constructor.getDeclaringClass();
    //Find applicable aspects. Bow out if none are applicable to this class
    List<MethodAspect> applicableAspects = new ArrayList<MethodAspect>();
    
    
    //filter methodAspect which matches the declaring Class
    for(MethodAspect methodAspect : methodAspects) {
      if (methodAspect.matches(declaringClass)) {
        applicableAspects.add(methodAspect);
      }
    }
    
    //
    if (applicableAspects.isEmpty()) {
      return defaultFactory.get(constructor);
    }
    
    //Gets list of methods from cglib
    List<Method> methods = new ArrayList<Method>();
    Enhancer.getMethods(declaringClass, null, methods);
    
    final Map<Method, Integer> indices = new HashMap<Method, Integer>();
    
    
    //creates method/interceptor pair holders and record.
    List<MethodInterceptorsPair> methodInterceptorsPairs = new ArrayList<MethodInterceptorsPair>();
    
    for(int i = 0; i< methods.size(); i++) {
      Method method = methods.get(i);
      methodInterceptorsPairs.add(new MethodInterceptorsPair(method));
      indices.put(method, i);
    }
    
    //Iterate over aspects and interceptors for the methods they apply
    //to.
    boolean anyMatched = false;
    /**
     * MethodAspect which contain all of the Interceptors
     */
    for (MethodAspect methodAspect : applicableAspects) {
      for(MethodInterceptorsPair pair : methodInterceptorsPairs) {
        if (methodAspect.matches(pair.method)) {
          pair.addAll(methodAspect.interceptors());
          anyMatched = true;
        }
      }
    }
    
    if (!anyMatched) {
      return defaultFactory.get(constructor);
    }

    // Creates callbacks.
    Callback[] callbacks = new Callback[methods.size()];
    Class<? extends Callback>[] callbackTypes = new Class[methods.size()];
    for (int i = 0; i < methods.size(); i++) {
      MethodInterceptorsPair methodInterceptorsPair = methodInterceptorsPairs.get(i);
      if (!methodInterceptorsPair.hasInterceptors()) {
        callbacks[i] = NoOp.INSTANCE;
        callbackTypes[i] = NoOp.class;
      } else {
        callbacks[i] = new InterceptorStackCallback(methodInterceptorsPair.method,
                                                    methodInterceptorsPair.interceptors);
        callbackTypes[i] = net.sf.cglib.proxy.MethodInterceptor.class;
      }
    }

    // Create the proxied class.
    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(declaringClass);
    enhancer.setUseCache(false); // We do enough caching.
    enhancer.setCallbackFilter(new CallbackFilter() {
      public int accept(Method method) {
        return indices.get(method);
      }
    });
    enhancer.setCallbackTypes(callbackTypes);
    enhancer.setUseFactory(false);

    Class<?> proxied = enhancer.createClass();

    // Store callbacks.
    Enhancer.registerStaticCallbacks(proxied, callbacks);

    return createConstructionProxy(proxied, constructor.getParameterTypes());

  }
  
  /**
   * Creates a construction proxy given a class and parameter types.
   */
  <T> ConstructionProxy<T> createConstructionProxy(Class<?> clazz, Class[] parameterTypes) {
    FastClass fastClass = FastClass.create(clazz);
    final FastConstructor fastConstructor = fastClass.getConstructor(parameterTypes);
    return new ConstructionProxy<T>() {
      @SuppressWarnings({ "unchecked" })
      public T newInstance(Object... arguments) throws InvocationTargetException {
        return (T) fastConstructor.newInstance(arguments);
      }
    };
  }

  
  @SuppressWarnings({"unchecked"})
  public <T> ConstructionProxy<T> get(Constructor<T> constructor) {
    return (ConstructionProxy<T>) constructionProxies.get(constructor);
  }


  
  static class MethodInterceptorsPair {
    final Method method;
    List<MethodInterceptor> interceptors;
    
    public MethodInterceptorsPair(Method method) {
      this.method = method;
    }
    
    void addAll(List<MethodInterceptor> interceptors) {
      if (this.interceptors == null) {
        this.interceptors = Lists.newArrayList();
      }
      this.interceptors.addAll(interceptors);
    }
    
    boolean hasInterceptors() {
      return interceptors != null;
    }
    
  }
}
