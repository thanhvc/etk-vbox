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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.etk.vbox.spi.ConstructionProxy;

import static org.etk.vbox.matcher.Matchers.*;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 8, 2011  
 */
public class ProxyFactoryTest extends TestCase {

  public void testSimpleCase() throws NoSuchMethodException, InvocationTargetException {
    //Defines the SimpleInterceptor which implements the {@code org.aopalliance.intercept.MethodInterceptor}
    SimpleInterceptor interceptor = new SimpleInterceptor();
    LogInterceptor logInterceptor = new LogInterceptor();
    
    //Defines the {@code ProxyFactoryBuilder}
    ProxyFactoryBuilder builder = new ProxyFactoryBuilder();
    
    
    builder.intercept(any(), any(), logInterceptor, interceptor);
    
    //Creates the {@code ProxyFactory}
    ProxyFactory factory = builder.create();
    //Gets constructor of the ConstructionProxy<Simple> class
    ConstructionProxy<Simple> constructor = factory.get(Simple.class.getDeclaredConstructor());

    //newInstance the Simple
    Simple simple = constructor.newInstance();
    //invoke this method.
    simple.invoke();
    assertTrue(simple.invoked);
    assertTrue(interceptor.invoked);

  }
  
  static class Simple {
    boolean invoked = false;
    
    public void invoke() {
      invoked = true;
      System.out.println("Simple::invoke() invoked!");
    }
  }
  
  static class SimpleInterceptor implements MethodInterceptor {
    boolean invoked = false;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      invoked = true;
      return invocation.proceed();
    }
    
  }
  
  static class LogInterceptor implements MethodInterceptor {
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
      try {
        System.out.println("LOG::BEGIN::" + invocation.getMethod().getName());
        return invocation.proceed();
      } finally {
        System.out.println("LOG::END::" + invocation.getMethod().getName());
      }
      
    }
  }
  
  
}
