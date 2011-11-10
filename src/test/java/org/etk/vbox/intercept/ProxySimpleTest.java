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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.common.collect.Lists;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 8, 2011  
 */
public class ProxySimpleTest extends TestCase {

  public void testSimpleProxyInterceptorsFastClass() throws SecurityException, NoSuchMethodException, InvocationTargetException {
    LogInterceptor interceptor = new LogInterceptor();
    DebugInterceptor debugInterceptor = new DebugInterceptor();
    
    //creates MethodAspect
    MethodAspect methodAspect = new MethodAspect();
    methodAspect.add(interceptor, debugInterceptor);
    
    //
    ProxyFactory<Simple> factory = new ProxyFactory<Simple>(methodAspect);
    Class<?> simpleClass = factory.get(Simple.class);
    FastClass fastClass = FastClass.create(simpleClass);
    final FastConstructor fastConstructor = fastClass.getConstructor(Simple.class.getDeclaredConstructor().getParameterTypes());
    Simple instance = (Simple) fastConstructor.newInstance();

    instance.show("my message");
    
    assertTrue(instance.invoked);
  }
  
  public void testSimpleProxyInterceptorsReflection() throws InvocationTargetException, SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    LogInterceptor interceptor = new LogInterceptor();
    DebugInterceptor debugInterceptor = new DebugInterceptor();
    
    //creates MethodAspect
    MethodAspect methodAspect = new MethodAspect();
    methodAspect.add(interceptor, debugInterceptor);
    
    //
    ProxyFactory<Simple> factory = new ProxyFactory<Simple>(methodAspect);
    Class<?> simpleClass = factory.get(Simple.class);

    Simple instance = (Simple) simpleClass.newInstance();
    
    instance.invoke();
    
    instance.show("my message");
    
    instance.showTwo("my message1", "my message2");
    
    assertTrue(instance.invoked);
  }
  
  
  //Simple Test
  
  static class Simple {
    boolean invoked = false;
    
    public void invoke() {
      invoked = true;
    }
    
    public void show(String message) {
      invoked = true;
    }
    
    public void showTwo(String message1, String message2) {
      invoked = true;
    }
  }
  
  static class LogInterceptor implements MethodInterceptor {
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      try {
        System.out.println("\nLOG::BEGIN::" + invocation.getMethod().getName());
        return invocation.proceed();
      } finally {
        System.out.println("LOG::END::" + invocation.getMethod().getName());
      }
      
    }
  }
  
  static class DebugInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      info(invocation);
      return invocation.proceed();
    }
    
    private void info(MethodInvocation invocation) {
      Method method = invocation.getMethod();
      Object[] arguments = invocation.getArguments();
      System.out.print("DEBUG INFO::invoked method::" + method.getName() + "(");
      int i = 0;
      for(Object obj : arguments) {
        i++;
        if (i == arguments.length)
          System.out.print("'" + obj.toString() + "'");
        else 
          System.out.print("'" + obj.toString() + "',");
        
        
      }
      System.out.print(")\n");
      
    }
    
  }
  
  
  
  /**
   * Constructs the MethodAspect
   * @author thanh_vucong
   *
   */
  static class MethodAspect {
    
    final List<MethodInterceptor> interceptors = new ArrayList<MethodInterceptor>();
    
    /**
     * Adds the {@code MethodInterceptor}
     * @param interceptors
     */
    public void add(MethodInterceptor...interceptors) {
      this.interceptors.addAll(Arrays.asList(interceptors));
    }
    
    public List<MethodInterceptor> interceptors() {
      return interceptors;
    }
  }
  
  /**
   * Defines the ProxyFactory which creates the Proxy<Class<T>>
   * @author thanh_vucong
   *
   * @param <T>
   */
  static class ProxyFactory<T> {
    final List<MethodAspect> methodAspects = new ArrayList<MethodAspect>();
    
    public ProxyFactory(MethodAspect...aspects) {
      methodAspects.addAll(Arrays.asList(aspects));
    }
    
    public void addAll(MethodAspect...aspects) {
      methodAspects.addAll(Arrays.asList(aspects));
    }
    
    public Class<?> get(final Class<T> clazz) {
      //Gets list of methods from cglib
      List<Method> methods = getMethods(clazz);
      final Map<Method, Integer> indices = new HashMap<Method, Integer>();
      
      
      //creates method/interceptor pair holders and record.
      List<MethodInterceptorsPair> methodInterceptorsPairs = new ArrayList<MethodInterceptorsPair>();
      
      for(int i = 0; i< methods.size(); i++) {
        Method method = methods.get(i);
        methodInterceptorsPairs.add(new MethodInterceptorsPair(method));
        indices.put(method, i);
      }
      
      
      /**
       * MethodAspect which contain all of the Interceptors
       */
      for (MethodAspect methodAspect : methodAspects) {
        for(MethodInterceptorsPair pair : methodInterceptorsPairs) {
            pair.addAll(methodAspect.interceptors());
          }
        }
      
      //assign the MethodInterceptors to the each method
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
      enhancer.setSuperclass(clazz);
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

      return proxied;
    }
    
    
    
    /**
     * Gets the list of methods from given Class<T>
     * @param clazz
     * @return
     */
    private List<Method> getMethods(final Class<T> clazz) {
      List<Method> methods = new ArrayList<Method>();
      Enhancer.getMethods(clazz, null, methods);
      return methods;
    }
  }
  
  /**
   * Defines the MethodInterceptor pair.
   * @author thanh_vucong
   *
   */
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
