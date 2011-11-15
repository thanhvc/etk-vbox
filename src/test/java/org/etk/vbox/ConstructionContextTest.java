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

import junit.framework.TestCase;

import org.etk.vbox.ConstructionContext;
import org.etk.vbox.DependencyException;
import org.etk.vbox.ConstructionContextTest.ClassUtils.ClassContext;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 11, 2011  
 */
public class ConstructionContextTest extends TestCase {

  /**
   * Using the Proxy to invoke the method.
   * @throws Throwable
   */
  public void testCreateProxy() throws Throwable {
    ClassContext<A> context = ClassUtils.getProxy(A.class);
    assertNotNull(context);
    
    assertNotNull(context.getProxyObject());
    assertNotNull(context.getContext());
    ClassUtils.getInstance(context, AImpl.class, new ConstructionContextTest());
    
  }
  /**
   * Using the Proxy to invoke the method.
   * @throws Throwable
   */
  public void testCreateProxyWithMethodInvoker() throws Throwable {
    ClassContext<A> context = ClassUtils.getProxy(A.class);
    //because this AImpl.class is inner class so if you want to create instance you specified outer class.
    //new ConstructionContextTest() is outer class
    ClassUtils.getInstance(context, AImpl.class, new ConstructionContextTest());

    // TODO create MethodSignature
    Method method = context.unWrap().getMethod("printMysefl", new Class[] { String.class });
    assertNotNull(method);

    MethodInvoker invoker = new MethodInvoker(context, method);
    invoker.invoke("testCreateProxy");
  }
  
  /**
   * Using the Proxy to invoke the method.
   * @throws Throwable
   */
  public void testMethodWrapper() throws Throwable {
    //Create the proxy class
    ClassContext<A> context = ClassUtils.getProxy(A.class);
    //inject the implementation to specified proxy. 
    ClassUtils.getInstance(context, AImpl.class, new ConstructionContextTest());

    //TODO create MethodSignature
    Method method = context.unWrap().getMethod("printMysefl", new Class[]{String.class} );
    assertNotNull(method);
    
    MethodContext methodContext = new MethodContext(context, method) {};
    methodContext.invoke("testMethodWrapper");
  }
  
  
  
  public void testGetParameterize() throws Throwable {
    // Create the proxy class
    ClassContext<A> context = ClassUtils.getProxy(A.class);
    // inject the implementation to specified proxy.
    ClassUtils.getInstance(context, AImpl.class, new ConstructionContextTest());

    Method[] methods = context.unWrap().getDeclaredMethods();

    for (Method method : methods) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Type[] types = method.getGenericParameterTypes();
      Constructor<?> parameterConstructor = null;
      Object[] parameters = new Object[parameterTypes.length];

      // processing the parameter type for method.
      for (int i = 0; i < parameterTypes.length; i++) {
        // constructor initialize primitive
        if (parameterTypes[i].isPrimitive()) {
          //
          parameterConstructor = getCompatibleConstructor(parameterTypes[i], parameterTypes);
        } else {
          //
          parameterConstructor = parameterTypes[i].getDeclaredConstructor(new Class[] {});
        }

        if (types[i].equals(java.lang.Integer.TYPE)) {
          parameters[i] = new Integer(5);
        } else if (types[i].equals(java.lang.Boolean.TYPE)) {
          parameters[i] = Boolean.TRUE;
        } else {
          parameters[i] = parameterConstructor.newInstance(new Object[] {});
          parameters[i] = "testGetParameterize " + i;
        }
      }

      //
      MethodContext methodContext = new MethodContext(context, method) {};
      //
      methodContext.invoke(parameters);
    }

  }
  
  public void testGetParameterizeWithArray() throws Throwable {
    // Create the proxy class
    ClassContext<B> context = ClassUtils.getProxy(B.class);
    // inject the implementation to specified proxy.
    ClassUtils.getInstance(context, BImpl.class, new ConstructionContextTest());

    Method[] methods = context.unWrap().getDeclaredMethods();

    for (Method method : methods) {
      Class<?>[] parameterTypes = method.getParameterTypes();
      Type[] types = method.getGenericParameterTypes();
      Constructor<?> parameterConstructor = null;
      Object[] parameters = new Object[parameterTypes.length];

      // processing the parameter type for method.
      for (int i = 0; i < parameterTypes.length; i++) {
        // constructor initialize primitive
        if (parameterTypes[i].isPrimitive()) {
          //
          parameterConstructor = getCompatibleConstructor(parameterTypes[i], parameterTypes);
        } if (parameterTypes[i].isArray()) {
          Class<?> type = parameterTypes[i].getComponentType();
          System.out.println(type.toString());
        } else {
          //
          parameterConstructor = parameterTypes[i].getDeclaredConstructor(new Class[] {});
        }

        if (types[i].equals(java.lang.Integer.TYPE)) {
          parameters[i] = new Integer(5);
        } else if (types[i].equals(java.lang.Boolean.TYPE)) {
          parameters[i] = Boolean.TRUE;
        } else {
          parameters[i] = parameterConstructor.newInstance(new Object[] {});
          parameters[i] = "testGetParameterize " + i;
        }
      }

      //
      MethodContext methodContext = new MethodContext(context, method) {};
      //
      methodContext.invoke(parameters);
    }

  }
  
  public void testGetParameterizeWithPrimitive() throws Throwable {
    
    // Create the proxy class
    ClassContext<A> context = ClassUtils.getProxy(A.class);
    // inject the implementation to specified proxy.
    ClassUtils.getInstance(context, AImpl.class, new ConstructionContextTest());
    
    Method[] methods = context.unWrap().getDeclaredMethods();
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
            parameters[i] = "ThanhVC" + i;
          }
        }
      }
      
      method.invoke(context.getInstance(), parameters);
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
  
  static class TypeResolver<T> {
    
    public Constructor<? extends T> findConstructorIn(Class<? extends T> implementation, Class<?> ...parameterTypes) {
     
      try {
        //inner class
        return implementation.getConstructor(parameterTypes);
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
        throw new DependencyException("Could not find a suitable constructor" + " in " + implementation.getName() + ".");
      }
    }
  }
  
  interface B {
    void showArray(String[] messages);
  }
  
  class BImpl implements B {
    public BImpl() {
    }
   
    @Override
    public void showArray(String[] messages) {
      for (String message : messages) {
        System.out.println("\nThe message: " + message);
      }

    }
  }
 
  interface A {
    void printMysefl(String myMessage);
    void setConfigure(boolean isConfigure);
    void printMysefl();
    void printNumber(int myNumber);
    void printMysefl(String myMessage1, String myMessage2);
    void showArray(String[] messages);
  }
  
  class AImpl implements A {
    public AImpl() {
    }
    public void printNumber(int myNumber) {
      System.out.println("\nMy number = " + myNumber);
    }
    
    public void setConfigure(boolean isConfigure) {
      System.out.println("\nset config  = " + isConfigure);
    }
    
    public void printMysefl(String myMessage) {
      System.out.println("\nThis is ConstructionContextTest.A class " + myMessage);
    }
    
    
    public void printMysefl(String myMessage1, String myMessage2) {
      System.out.println("\nThis is message1 = " + myMessage1);
      System.out.println("This is message2 = " + myMessage2);
    }
    
    public void printMysefl() {
      System.out.println("\nThis is ConstructionContextTest.A class ");
    }
    @Override
    public void showArray(String[] messages) {
      for(String message: messages) {
        System.out.println("\nThe message: " + message);
      }
      
    }
    
    
  }
  
  
  /**
   * Utility for class
   * @author thanh_vucong
   *
   */
  static class ClassUtils {
    
    static class ClassContext<T> {
      ConstructionContext<T> context;
      final Object proxyObject;
      T myInstance;
      
      public ClassContext(ConstructionContext<T> context, final Object proxyObject) {
        this.context = context;
        this.proxyObject = proxyObject;
      }
      
      public ConstructionContext<T> getContext() {
        return context;
      }
      
      public Object getProxyObject() {
        return proxyObject;
      }
      
      @SuppressWarnings("unchecked")

      /**
       * Gets the raw type of the implementation
       * 
       * Example: 
       * interface A {};
       * class AImpl implements A {};
       * it will return Class<AImpl>.
       */
      public Class<T> unWrap() {
        return (Class<T>) myInstance.getClass();
      }

      /**
       * Gets the instance of the implementation
       * 
       * + Example: 
       * interface A {};
       * class AImpl implements A {};
       * it will return return new AImpl();
       * @return
       */
      public T getInstance() {
        return myInstance;
      }

      /**
       * Sets the instance of implementation.
       * use when we need to MethodInvoker.
       * @param myInstance
       */
      public void setMyConstructor(T myInstance) {
        this.myInstance = myInstance;
        this.context.setProxyDelegates(myInstance);
      }
      
    }
    
    /**
     * Gets the proxy object for the Type.
     * 
     * @param <T>
     * @param type
     * @return
     * @throws Throwable
     */
    public static <T> ClassContext<T> getProxy(Class<T> type) throws Throwable {
      ConstructionContext<T> constructorContext = new ConstructionContext<T>();
      Object object = constructorContext.createProxy(type);
      return new ClassContext<T>(constructorContext, object);
    }
    /**
     * new instance for implements and then injects this one to the Proxy object.
     * @param <T>
     * @param context
     * @param implementation
     * @param classes
     * @throws Throwable
     */
    public static <T> void getInstance(ClassContext<T> context, Class<? extends T> implementation, Object arg) throws Throwable {
      TypeResolver<T> resolver = new TypeResolver<T>();
      Constructor<? extends T> constructorA = resolver.findConstructorIn(implementation, ConstructionContextTest.class);
      T myInstance;
      if (constructorA.getParameterTypes().length == 0) {
        myInstance = constructorA.newInstance(new Object[0]);
        assertNotNull(myInstance);
      } else {
        //because this class which constructors belong to is inner class so that it always has 1 argument when new instance.
        myInstance = constructorA.newInstance(new Object[]{arg});
        assertNotNull(myInstance);
        //TODO here WDYT constructorContext here?????
      }
      
      context.setMyConstructor(myInstance);
    }
    
    /**
     * 
     * @param <T>
     * @param context
     * @param implementation
     * @param args
     * @throws Throwable
     */
    public static <T> void getInstance(ClassContext<T> context, Class<T> implementation, Object[] args) throws Throwable {
      TypeResolver<T> resolver = new TypeResolver<T>();
      Constructor<? extends T> constructorA = resolver.findConstructorIn(implementation, ConstructionContextTest.class);
      T myInstance;
      if (constructorA.getParameterTypes().length == 0) {
        myInstance = constructorA.newInstance(new Object[0]);
        assertNotNull(myInstance);
      } else {
        //because this class which constructors belong to is inner class so that it always has 1 argument when new instance.
        myInstance = constructorA.newInstance(args);
        assertNotNull(myInstance);
        //TODO here WDYT constructorContext here?????
      }
      
      context.setMyConstructor(myInstance);
    }
  }
  
  /**
   * MethodContext keeps method information.
   * @author thanh_vucong
   *
   */
  abstract class MethodContext {
    //apply create InternalFactory as Guice here.
    final Method method;
    final Object target;
    ClassContext<?> context;
    final MethodInvoker invoker;
    
    public MethodContext(ClassContext<?> context, Method method) {
      this.context = context;
      this.method = method;
      this.target = context.getProxyObject();
      this.invoker = new MethodInvoker(this.context, this.method);
    }
    
    protected void invoke(Object arg) throws Throwable {
      invoker.invoke(arg);
    }
    
    protected void invoke(Object[] args) throws Throwable{
      invoker.invoke(args);
    }
    
  }
  
  /**
   * MethodInvoker class which supports to invoke the method.
   * @author thanh_vucong
   *
   */
  class MethodInvoker {
    Method method;
    final Class<?>[] parameterTypes;
    final Type[] methodTypes;
    Object target;
    InvocationHandler invocationHandler;
    ClassContext<?> context;
    
    public MethodInvoker(ClassContext<?> context, Method method) {
      this.context = context;
      this.target = context.getProxyObject();
      this.method = method;
      this.parameterTypes = method.getParameterTypes();
      this.methodTypes = method.getGenericParameterTypes();
      
      if (Proxy.isProxyClass(target.getClass())) {
        this.invocationHandler = Proxy.getInvocationHandler(target);
      }
      
    }
    
    public void invoke(Object arg) throws Throwable {
      
      if(methodTypes.length > 0) {
        if (arg.getClass() != parameterTypes[0]) 
          throw new IllegalArgumentException("Method name: " + method.getName() + " with expected type( " + parameterTypes[0] + ")");
      }
      
      if (invocationHandler == null) {
        method.invoke(this.method, arg);
      } else {
        //context.getInstance() is instance of the implementation class
        //TODO ThanhVC System.out
        System.out.print("invoke method:: " + method.getName() + "::result::");
        invocationHandler.invoke(context.getInstance(), method, new Object[]{arg});
      }
    }
    
    public void invoke(Object[] args) throws Throwable {
      if (invocationHandler == null) {
        method.invoke(this.method, args);
      } else {
        //context.getInstance() is instance of the implementation class
        invocationHandler.invoke(context.getInstance(), method, args);
      }
    }
  }
}
