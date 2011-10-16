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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import org.etk.vbox.ConstructionContext;
import org.etk.vbox.DependencyException;


import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2011  
 */
public class OldConstructionContextTest extends TestCase {

  public void testCreateProxy() throws Throwable {
    ConstructionContext<A> constructorContext = new ConstructionContext<A>();
    Object object = constructorContext.createProxy(A.class);
    
    TypeResolver<AImpl> resolver = new TypeResolver<AImpl>();
    
    assertNotNull(object);
    
    assertTrue(Proxy.isProxyClass(object.getClass()));
    Constructor<AImpl> constructorA = resolver.findConstructorIn(AImpl.class, OldConstructionContextTest.class);
    
    System.out.println(constructorA.getParameterTypes().length);
    
    if (constructorA.getParameterTypes().length == 0) {
      A myInstance = constructorA.newInstance(new Object[0]);
      assertNotNull(myInstance);
    } else {
      //because this class which constructors belong to is inner class so that it always has 1 argument when new instance.
      A myInstance = constructorA.newInstance(new Object[]{new OldConstructionContextTest()});
      assertNotNull(myInstance);
      
      constructorContext.setProxyDelegates(myInstance);
      InvocationHandler invocationHandler = Proxy.getInvocationHandler(object);
      
      assertNotNull(invocationHandler);
      Method method = AImpl.class.getMethod("printMysefl", new Class[]{String.class} );
      
      
      invocationHandler.invoke(myInstance, method, new Object[]{"Test"});
    }
  }
  
  public void testGetParameterize() throws Exception {
    
    Method[] methods = AImpl.class.getDeclaredMethods();
    TypeResolver<AImpl> resolver = new TypeResolver<AImpl>();
    Constructor<AImpl> constructorA = resolver.findConstructorIn(AImpl.class, OldConstructionContextTest.class);
    A myInstance = constructorA.newInstance(new Object[]{new OldConstructionContextTest()});
    assertNotNull(myInstance);
    
    for(Method method : methods) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Type[] types = method.getGenericParameterTypes();
      Constructor<?> constructor = null; 
      Object[] parameters = new Object[parameterTypes.length];
      for(int i = 0; i < parameterTypes.length; i++) {
        //constructor initialize primitive
        if (parameterTypes[i].isPrimitive()) {
          System.out.println("super class::" + parameterTypes[i]);
          constructor = getCompatibleConstructor(parameterTypes[i], parameterTypes);
        } else {
          constructor = parameterTypes[i].getDeclaredConstructor(new Class[]{});
        }
        
        System.out.println("Type of " + i + types[i].toString());
      
        if (types[i].equals(java.lang.Integer.TYPE)) {
          parameters[i] = new Integer(5);
        } else if (types[i].equals(java.lang.Boolean.TYPE)) {
          parameters[i] = Boolean.TRUE;
        } else {
          parameters[i] = constructor.newInstance(new Object[]{});
          parameters[i] = "ThanhVC";
        }
      }
      method.invoke(myInstance, parameters);
    }
    
  }
  
  public void testGetParameterizeWithPrimitive() throws Exception {
    
    Method[] methods = AImpl.class.getDeclaredMethods();
    TypeResolver<AImpl> resolver = new TypeResolver<AImpl>();
    Constructor<AImpl> constructorA = resolver.findConstructorIn(AImpl.class, OldConstructionContextTest.class);
    A myInstance = constructorA.newInstance(new Object[]{new OldConstructionContextTest()});
    assertNotNull(myInstance);
    
    for(Method method : methods) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Type[] types = method.getGenericParameterTypes();
      Constructor<?> constructor = null; 
      Object[] parameters = new Object[parameterTypes.length];
      for(int i = 0; i < parameterTypes.length; i++) {
        //constructor initialize primitive
        if (parameterTypes[i].isPrimitive() == false) {
          constructor = parameterTypes[i].getDeclaredConstructor(new Class[]{});
          parameters[i] = constructor.newInstance(new Object[]{});
          parameters[i] = "ThanhVC";
          //if here check type is Integer.class, String.class, ...
          //SINGLE or MULTI
        } else {
          if (types[i].equals(java.lang.Integer.TYPE)) {
            parameters[i] = new Integer(5);
          } else if (types[i].equals(java.lang.Boolean.TYPE)) { 
            parameters[i] = Boolean.TRUE;
          } else {
            parameters[i] = "ThanhVC";
          }
        }
      }
      
      method.invoke(myInstance, parameters);
      
    }
    
  }
  
  /**
  * Get a compatible constructor to the supplied parameter types.
  *
  * @param clazz the class which we want to construct
  * @param parameterTypes the types required of the constructor
  *
  * @return a compatible constructor or null if none exists
  */
  public static Constructor<?> getCompatibleConstructor(Class<?> clazz, Class<?>[] parameterTypes) {
    Constructor<?>[] constructors = clazz.getConstructors();
    for (int i = 0; i < constructors.length; i++) {
      if (constructors[i].getParameterTypes().length == (parameterTypes != null ? parameterTypes.length: 0)) {
        // If we have the same number of parameters there is a shot that we have
        // a compatible
        // constructor
        Class<?>[] constructorTypes = constructors[i].getParameterTypes();
        boolean isCompatible = true;
        for (int j = 0; j < (parameterTypes != null ? parameterTypes.length : 0); j++) {
          if (!constructorTypes[j].isAssignableFrom(parameterTypes[j])) {
            // The type is not assignment compatible, however
            // we might be able to coerce from a basic type to a boxed type
            if (constructorTypes[j].isPrimitive()) {
              if (!isAssignablePrimitiveToBoxed(constructorTypes[j], parameterTypes[j])) {
                isCompatible = false;
                break;
              }
            }
          }
        }
        if (isCompatible) {
          return constructors[i];
        }
      }
    }
    return null;
  }

  /**
  * <p> Checks if a primitive type is assignable with a boxed type.</p>
  *
  * @param primitive a primitive class type
  * @param boxed     a boxed class type
  *
  * @return true if primitive and boxed are assignment compatible
  */
  private static boolean isAssignablePrimitiveToBoxed(Class<?> primitive, Class<?> boxed) {
    if (primitive.equals(java.lang.Boolean.TYPE)) {
      if (boxed.equals(java.lang.Boolean.class))
        return true;
      else
        return false;
    } else {
      if (primitive.equals(java.lang.Byte.TYPE)) {
        if (boxed.equals(java.lang.Byte.class))
          return true;
        else
          return false;
      } else {
        if (primitive.equals(java.lang.Character.TYPE)) {
          if (boxed.equals(java.lang.Character.class))
            return true;
          else
            return false;
        } else {
          if (primitive.equals(java.lang.Double.TYPE)) {
            if (boxed.equals(java.lang.Double.class))
              return true;
            else
              return false;
          } else {
            if (primitive.equals(java.lang.Float.TYPE)) {
              if (boxed.equals(java.lang.Float.class))
                return true;
              else
                return false;
            } else {
              if (primitive.equals(java.lang.Integer.TYPE)) {
                if (boxed.equals(java.lang.Integer.class))
                  return true;
                else
                  return false;
              } else {
                if (primitive.equals(java.lang.Long.TYPE)) {
                  if (boxed.equals(java.lang.Long.class))
                    return true;
                  else
                    return false;
                } else {
                  if (primitive.equals(java.lang.Short.TYPE)) {
                    if (boxed.equals(java.lang.Short.class))
                      return true;
                    else
                      return false;
                  }
                }
              }
            }
          }
        }
      }
    }
    return false;
  }
  
  class TypeResolver<T extends A> {
    
    public Constructor<T> findConstructorIn(Class<T> implementation, Class<?> ...parameterTypes) {
      //Constructor<T> found = null;
      // instead.
      try {
        
        
        /*
        for (Constructor<?> constructor : implementation.getDeclaredConstructors()) {
          found = (Constructor<T>) constructor;
        }
        if (found != null) {
          return found;
        }*/
        //inner class
        return implementation.getConstructor(parameterTypes);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        throw new DependencyException("Could not find a suitable constructor" + " in " + implementation.getName() + ".");
      }
    }
  }
  
 
  
  
  interface A {
    
    void printMysefl(String myMessage);
    
    void setConfigure(boolean isConfigure);
    /*
    void printMysefl();*/
    
    void printNumber(int myNumber);
  }
  
  class AImpl implements A {
    public AImpl() {
    }
    public void printNumber(int myNumber) {
      System.out.println("\n\n\nMy number = " + myNumber);
    }
    
    public void setConfigure(boolean isConfigure) {
      System.out.println("\n\n\nset config  = " + isConfigure);
    }
    
    public void printMysefl(String myMessage) {
      System.out.println("\n\n\nThis is OldConstructionContextTest.A class " + myMessage);
    }
    
    /*
    
    public void printMysefl(String myMessage1, String myMessage2) {
      System.out.println("\n\n\nThis is message1 = " + myMessage1);
      System.out.println("This is message2 = " + myMessage2);
    }
    
    public void printMysefl() {
      System.out.println("\n\n\nThis is OldConstructionContextTest.A class ");
    }*/
  }
}