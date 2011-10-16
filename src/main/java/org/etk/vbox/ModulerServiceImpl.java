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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.etk.vbox.ConstructionContext;
import org.etk.vbox.DependencyException;
import org.etk.vbox.ExternalContext;
import org.etk.vbox.InspectorContext;
import org.etk.vbox.InternalInspector;
import org.etk.vbox.ModulerService;
import org.etk.vbox.ModulerServiceImpl;
import org.etk.vbox.MyInject;
import org.etk.vbox.MyKey;
import org.etk.vbox.MyScope;
import org.etk.vbox.utils.DataCache;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 13, 2011  
 */
public final class ModulerServiceImpl implements ModulerService {

  final Map<MyKey<?>, InternalInspector<?>> inspectors;
  
  ModulerServiceImpl(Map<MyKey<?>, InternalInspector<?>> factories) {
    this.inspectors = factories;
  }
  
  @SuppressWarnings("unchecked")
  <T> InternalInspector<? extends T> getFactory(MyKey<T> key) {
    return (InternalInspector<T>) inspectors.get(key);
  }
  
  /**
   * Field and method injectors.
   * Define to keep all of the dependencies of key(class implementation).
   * Example: A(B b, C c) this return 2 dependency injectors for constructor.
   */
  final Map<Class<?>, List<Injector>> dependencyInjectors = new DataCache<Class<?>, List<Injector>>() {
        protected List<Injector> create(Class<?> key) {
          List<Injector> injectors = new ArrayList<Injector>();
          addInjectors(key, injectors);
          return injectors;
        }
      };
      
  /**
   * This methods is heart of the IOC when executes the collect all of injectors.
   *     
   * @param clazz
   * @param injectors
   */
  void addInjectors(Class clazz, List<Injector> injectors) {
    if (clazz == Object.class) {
      return ;
    }
    
    //Add injectors for superclass first
    addInjectors(clazz.getSuperclass(), injectors);
    
    addInjectorsForFields(clazz.getDeclaredFields(), false, injectors);
    addInjectorsForMethods(clazz.getDeclaredMethods(), false, injectors);
  }
  
  void injectStatics(List<Class<?>> staticInjections) {
    final List<Injector> injectors = new ArrayList<Injector>();

    for (Class<?> clazz : staticInjections) {
      addInjectorsForFields(clazz.getDeclaredFields(), true, injectors);
      addInjectorsForMethods(clazz.getDeclaredMethods(), true, injectors);
    }

    callInContext(new ContextualCallable<Void>() {
      public Void call(InspectorContext context) {
        for (Injector injector : injectors) {
          injector.inject(context, null);
        }
        return null;
      }
    });
  }
  
  void addInjectorsForMethods(Method[] methods, boolean statics, List<Injector> injectors) {
    addInjectorsForMembers(Arrays.asList(methods),
                           statics,
                           injectors,
                           new InjectorFactory<Method>() {
      
          public Injector create(ModulerServiceImpl application, Method method, String name) throws MissingDependencyException {
            return new MethodInjector(application, method, name);
          }
        });
  }
  
  
  private static Object[] getParameters(Member member,
                                        InspectorContext context,
                                        ParameterInjector[] parameterInjectors) {
    if (parameterInjectors == null) {
      return null;
    }

    Object[] parameters = new Object[parameterInjectors.length];
    for (int i = 0; i < parameters.length; i++) {
      parameters[i] = parameterInjectors[i].inject(member, context);
    }
    return parameters;
  }
  
  
  interface InjectorFactory<M extends Member & AnnotatedElement> {
    Injector create(ModulerServiceImpl application, M member, String name) throws MissingDependencyException;
  }
  
  void addInjectorsForFields(Field[] fields, boolean statics, List<Injector> injectors) {
    addInjectorsForMembers(Arrays.asList(fields), statics, injectors, new InjectorFactory<Field>() {

      @Override
      public Injector create(ModulerServiceImpl application, Field field, String name) throws MissingDependencyException {
        return new FieldInjector(application, field, name);
      }
      
    });
  }
  
  
  private boolean isStatic(Member member) {
    return Modifier.isStatic(member.getModifiers());
  }
  
  <M extends Member & AnnotatedElement> void addInjectorsForMembers(List<M> members,
                                                                    boolean statics,
                                                                    List<Injector> injectors,
                                                                    InjectorFactory<M> injectorFactory) {
    for (M member : members) {
      if (isStatic(member) == statics) {
        MyInject inject = member.getAnnotation(MyInject.class);
        if (inject != null) {
          try {
            injectors.add(injectorFactory.create(this, member, inject.value()));
          } catch (MissingDependencyException e) {
            if (inject.required()) {
              throw new DependencyException(e);
            }
          }
        }
      }
    }
  }
  
  public void inject(final Object o) {
    callInContext(new ContextualCallable<Void>() {
      public Void call(InspectorContext context) {
        inject(o, context);
        return null;
      }
    });
  }
  
  /**
   * Executes object with InternalContext
   * @param o
   * @param context
   */
  void inject(Object o, InspectorContext context) {
    List<Injector> injectors = this.dependencyInjectors.get(o.getClass());
    for (Injector injector : injectors) {
      injector.inject(context, o);
    }
  }
  
  /**
   * This is entry point to execute the injector processing.
   * Inject to implementation's Method, Field, and Constructor.
   * 
   */
  public <T> T inject(final Class<T> implementation) {
    return callInContext(new ContextualCallable<T>() {
      public T call(InspectorContext context) {
        return inject(implementation, context);
      }
    });
  }
  
  /**
   * Executes inject Implementation with InternalContext
   * @param <T>
   * @param implementation
   * @param context
   * @return
   */
  <T> T inject(Class<T> implementation, InspectorContext context) {
    try {
      ConstructorInjector<T> constructor = getConstructor(implementation);
      return implementation.cast(constructor.construct(context, implementation));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public <T> T getInstance(final Class<T> type, final String name) {
    return callInContext(new ContextualCallable<T>() {
      public T call(InspectorContext context) {
        return getInstance(type, name, context);
      }
    });
  }

  public <T> T getInstance(final Class<T> type) {
    return callInContext(new ContextualCallable<T>() {
      public T call(InspectorContext context) {
        return getInstance(type, context);
      }
    });
  }
  
  <T> T getInstance(Class<T> type, InspectorContext context) {
    return getInstance(type, DEFAULT_NAME, context);
  }
  
  
  
  @SuppressWarnings("unchecked")
  <T> T getInstance(Class<T> type, String name, InspectorContext context) {
    ExternalContext<?> previous = context.getExternalContext();
    MyKey<T> key = MyKey.newInstance(type, name);
    context.setExternalContext(ExternalContext.newInstance(null, key, this));
    try {
      return getFactory(key).create(context);
    } finally {
      context.setExternalContext(previous);
    }
  }

  final ThreadLocal<MyScope.Strategy> localScopeStrategy = new ThreadLocal<MyScope.Strategy>();

  public void setScopeStrategy(MyScope.Strategy scopeStrategy) {
    this.localScopeStrategy.set(scopeStrategy);
  }

  public void removeScopeStrategy() {
    this.localScopeStrategy.remove();
  }
  
  Map<Class<?>, ConstructorInjector> constructors = new DataCache<Class<?>, ConstructorInjector>() {
    @SuppressWarnings("unchecked")
    protected ConstructorInjector<?> create(Class<?> implementation) {
      return new ConstructorInjector(ModulerServiceImpl.this, implementation);
    }
  };
  
  /**
   * 
   * @param <M>
   * @param member
   * @param annotations
   * @param parameterTypes
   * @param defaultName
   * @return
   * @throws MissingDependencyException
   */
  <M extends AccessibleObject & Member> ParameterInjector<?>[] getParametersInjectors(M member,
                                                                                      Annotation[][] annotations,
                                                                                      Class[] parameterTypes,
                                                                                      String defaultName) throws MissingDependencyException {
    List<ParameterInjector<?>> parameterInjectors = new ArrayList<ParameterInjector<?>>();
    Iterator<Annotation[]> annotationsIterator = Arrays.asList(annotations).iterator();
    
    for(Class<?> parameterType : parameterTypes) {
      MyInject annotation = findInject(annotationsIterator.next());
      String name = annotation == null ? defaultName : annotation.value();
      MyKey<?> key = MyKey.newInstance(parameterType, name);
      parameterInjectors.add(createParameterInjector(key, member));
    }
    
    return toArray(parameterInjectors);
  }
  
  @SuppressWarnings("unchecked")
  private ParameterInjector<?>[] toArray(List<ParameterInjector<?>> parameterInjections) {

    return parameterInjections.toArray(new ParameterInjector[parameterInjections.size()]);

  }
  
  <T> ParameterInjector<T> createParameterInjector(MyKey<T> key, Member member) throws MissingDependencyException {
    //TODO This point is very important because it get the factory of parameter injector
    //example
    // class AImpl {
    //   B b;
    //   Aimpl(@injector B b) {
    // }
    // }
    // class B {}
    InternalInspector<? extends T> factory = getFactory(key);
    if (factory == null) {
      throw new MissingDependencyException("No mapping found for dependency " + key + " in " + member + ".");
    }
    
    ExternalContext<T> externalContext = ExternalContext.newInstance(member, key, this);
    return new ParameterInjector<T>(externalContext, factory);
  }
  
  /**
   * Finds the {@link MyInject} annotation in an array of annotations.
   * @param annotations
   * @return
   */
  MyInject findInject(Annotation[] annotations) {
    for(Annotation annotation : annotations) {
      if (annotation.annotationType() == MyInject.class) {
        return MyInject.class.cast(annotation);
      }
    }
    return null;
  }
  
  /**
   * Gets a constructor function for a given implementation class.
   */
  @SuppressWarnings("unchecked")
  <T> ConstructorInjector<T> getConstructor(Class<T> implementation) {
    return constructors.get(implementation);
  }
  
  ThreadLocal<InspectorContext[]> localContext = new ThreadLocal<InspectorContext[]>() {
    protected InspectorContext[] initialValue() {
      return new InspectorContext[1];
    }
  };
  
  /**--------------------------------------------------------------------------------------------------------------*/
  /**
   * 
   * @author thanh_vucong
   *
   * @param <T>
   */
  interface ContextualCallable<T> {
    T call(InspectorContext context);
  }
  
  /**
   * Looks up thread local context. Creates (and removes) a new context if
   * necessary.
   */
  <T> T callInContext(ContextualCallable<T> callable) {
    InspectorContext[] reference = localContext.get();
    if (reference[0] == null) {
      reference[0] = new InspectorContext(this);
      try {
        return callable.call(reference[0]);
      } finally {
        // Only remove the context if this call created it.
        reference[0] = null;
      }
    } else {
      // Someone else will clean up this context.
      return callable.call(reference[0]);
    }
  }
  
  /**--------------------------------------------------------------------------------------------------------------*/
  interface Injector {
    /**
     * This method is very important to execute injecting
     *  the dependencies to the Object.
     *  
     * @param context
     * @param o
     */
    void inject(InspectorContext context, Object o);
  }
  
  /**--------------------------------------------------------------------------------------------------------------*/
  /**
   * Method injector
   * @author thanh_vucong
   *
   */
  static class MethodInjector implements Injector {

    final Method method;
    final ParameterInjector<?>[] parameterInjectors;

    public MethodInjector(ModulerServiceImpl module, Method method, String name) throws MissingDependencyException {
      this.method = method;
      method.setAccessible(true);

      Class<?>[] parameterTypes = method.getParameterTypes();
      if (parameterTypes.length == 0) {
        throw new DependencyException(method + " has no parameters to inject.");
      }
      parameterInjectors = module.getParametersInjectors(method, method.getParameterAnnotations(), parameterTypes, name);
    }
    
    /**
     * Executes the injector the Objects[] to the specified method.
     */
    public void inject(InspectorContext context, Object o) {
      try {
        method.invoke(o, getParameters(method, context, parameterInjectors));
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
 
 
  /**--------------------------------------------------------------------------------------------------------------*/
  
  /**
   * ConstructorInjector
   */
  static class ConstructorInjector<T> {
    final Class<T> implementation;
    final List<Injector> injectors;
    final Constructor<T> constructor;
    final ParameterInjector<?>[] parameterInjectors;
    
    ConstructorInjector(ModulerServiceImpl module, Class<T> implementation) {
      this.implementation = implementation;
      constructor = findConstructorIn(implementation);
      constructor.setAccessible(true);
      
      try {
        MyInject inject = constructor.getAnnotation(MyInject.class);
        parameterInjectors = inject == null ? null : module.getParametersInjectors(constructor, constructor.getParameterAnnotations(), constructor.getParameterTypes(), inject.value());
      } catch (MissingDependencyException e) {
        throw new DependencyException(e);
      }
      
      injectors = module.dependencyInjectors.get(implementation);
    }
    
    /**
     * Finds the cons
     * @param implementation
     * @return
     */
    private Constructor<T> findConstructorIn(Class<T> implementation) {
      Constructor<T> found = null;
      //step 1: get constructor[] of implementation.
      //step 2: find the constructor which was configured the injector annotation.
      //throw exception if more than one constructor which was configured I
      for(Constructor<?> constructor : implementation.getDeclaredConstructors()) {
        if(constructor.getAnnotation(MyInject.class) != null) {
          
          if(found != null) {
            throw new DependencyException("More than one constructor with @Inject found in " + implementation + ".");
          }
          found = (Constructor<T>) constructor;
        }
      }
      
      if (found != null) {
        return found;
      }
      
      try {
        return implementation.getDeclaredConstructor();
      } catch (NoSuchMethodException e) {
        //Note:: Maybe can raise the exception if the class which is being injected is inner class.
        //So you can not get the default constructor because it's default constructor is public B(outer-class);
        //If you want to avoid in this case. you should simply add the static keyword in this class.
        throw new DependencyException("Could not find a suitable constructor" + " in " + implementation.getName() + ".");
      }
    }
    
    /**
     * Construct an instance. Returns {@code Object} instead of {@code T}
     * because it may return a proxy.
     */
    Object construct(InspectorContext context, Class<? super T> expectedType) {
      ConstructionContext<T> constructionContext = context.getConstructionContext(this);

      // We have a circular reference between constructors. Return a proxy.
      // Circular reference A depend on B and B depend on A.
      
      if (constructionContext.isConstructing()) {
        // TODO : if we can't proxy this object, can we proxy the
        // other object?
        return constructionContext.createProxy(expectedType);
      }

      // If we're re-entering this factory while injecting fields or methods,
      // return the same instance. This prevents infinite loops.
      T t = constructionContext.getCurrentReference();
      if (t != null) {
        return t;
      }

      try {
        // First time through...
        constructionContext.startConstruction();
        try {
          Object[] parameters = getParameters(constructor, context, parameterInjectors);
          t = constructor.newInstance(parameters);
          constructionContext.setProxyDelegates(t);
        } finally {
          constructionContext.finishConstruction();
        }

        // Store reference. If an injector re-enters this factory, they'll
        // get the same reference.
        constructionContext.setCurrentReference(t);

        // Inject fields and methods.
        for (Injector injector : injectors) {
          injector.inject(context, t);
        }

        return t;
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } finally {
        constructionContext.removeCurrentReference();
      }
    }
  }
  /**--------------------------------------------------------------------------------------------------------------*/
  /**
   * Method Injector
   * @author thanh_vucong
   *
   */
  static class FieldInjector implements Injector {

    final Field field;
    final InternalInspector<?> factory;
    final ExternalContext<?> externalContext;

    public FieldInjector(ModulerServiceImpl application, Field field, String name) throws MissingDependencyException {
      this.field = field;
      field.setAccessible(true);

      MyKey<?> key = MyKey.newInstance(field.getType(), name);
      factory = application.getFactory(key);
      if (factory == null) {
        throw new MissingDependencyException("No mapping found for dependency " + key + " in " + field + ".");
      }

      this.externalContext = ExternalContext.newInstance(field, key, application);
    }

    public void inject(InspectorContext context, Object o) {
      ExternalContext<?> previous = context.getExternalContext();
      context.setExternalContext(externalContext);
      try {
        field.set(o, factory.create(context));
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      } finally {
        context.setExternalContext(previous);
      }
    }
  }
  
  /**--------------------------------------------------------------------------------------------------------------*/
  
  /**
   * 
   * @author thanh_vucong
   *
   * @param <T>
   */
  
  static class ParameterInjector<T> {
    final ExternalContext<T> externalContext;
    final InternalInspector<? extends T> factory;
    
    public ParameterInjector(ExternalContext<T> externalContext, InternalInspector<? extends T> factory) {
      this.externalContext = externalContext;
      this.factory = factory;
    }
    
    T inject(Member member, InspectorContext context) {
      ExternalContext<?> previous = context.getExternalContext();
      context.setExternalContext(externalContext);
      try {
        return factory.create(context);
      } finally {
        context.setExternalContext(previous);
      }
    }
  }
  
  /**--------------------------------------------------------------------------------------------------------------*/
  static class MissingDependencyException extends Exception {

    MissingDependencyException(String message) {
      super(message);
    }
  }
}
