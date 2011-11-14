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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aopalliance.intercept.MethodInterceptor;
import org.etk.vbox.ModuleAssembler;
import org.etk.vbox.InspectorContext;
import org.etk.vbox.InternalInspector;
import org.etk.vbox.ModulerService;
import org.etk.vbox.ModulerServiceImpl;
import org.etk.vbox.MyKey;
import org.etk.vbox.MyScope;
import org.etk.vbox.MyScoped;
import org.etk.vbox.intercept.ProxyFactoryBuilder;
import org.etk.vbox.matcher.Matcher;

/**
 * Builds a dependencies injection {@link ModuleService}. The combination of dependency type 
 * and name uniquely identified a dependency mapping; you can use the same name for two different types
 * Not safe for concurrent use.
 * 
 * <p> Adds the follwing factories by default:
 * 
 * <ul>
 *   <li> Injects the current {@link ModuleService}
 *   <li> Injects the {@link Logger} for the injected member's declaring class.
 * </ul>
 *  
 * <p> Converts constants as needed from {@code String} to any primitive type in 
 * addition to {@code enum} and {@code Class}
 *  
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 12, 2011  
 */
public final class ModuleAssembler {

  final Map<MyKey<?>, InternalInspector<?>> factories = new HashMap<MyKey<?>, InternalInspector<?>>();
  final List<InternalInspector<?>> singletonFactories = new ArrayList<InternalInspector<?>>();
  final List<Class<?>> staticInjections = new ArrayList<Class<?>>();
  boolean created;
  final ProxyFactoryBuilder proxyFactoryBuilder;
  
  public ModuleAssembler() {
    this.proxyFactoryBuilder = new ProxyFactoryBuilder();
  }
  /**
   * Convenience method.&nbsp;Equivalent to {@code factory(type,
   * Moduler.DEFAULT_NAME, implementation)}.
   *
   * @see #factory(Class, String, Class)
   */
  public <T> ModuleAssembler factory(Class<T> type, Class<? extends T> implementation) {
    return factory(type, ModulerService.DEFAULT_NAME, implementation);
  }
  
  /**
   * Maps an implementation class to a given dependency type and name. Creates
   * instances using the moduler, recursively injecting dependencies.
   *
   * <p>Sets scope to value from {@link MyScoped} annotation on the
   * implementation class. Defaults to {@link MyScope#DEFAULT} if no annotation
   * is found.
   *
   * @param type of dependency
   * @param name of dependency
   * @param implementation class
   * @return this builder
   */
  public <T> ModuleAssembler factory(final Class<T> type, String name, final Class<? extends T> implementation) {
    MyScoped scoped = implementation.getAnnotation(MyScoped.class);
    MyScope scope = scoped == null ? MyScope.DEFAULT : scoped.value();
    return factory(type, name, implementation, scope);
  }
    
  public <T> ModuleAssembler factory(final Class<T> type,
                                        final String name,
                                        final Class<? extends T> implementation,
                                        final MyScope scope) {
    
    //upper bound generic: extends and lower bound generic: super.
    
    InternalInspector<? extends T> factory = new InternalInspector<T>() {
      volatile ModulerServiceImpl.ConstructorInjector<? extends T> constructor;
      
      @Override
      public T create(InspectorContext context) {
        if (constructor == null) {
          this.constructor = context.getModuleServiceImpl().getConstructor(implementation);
        }
        return (T) constructor.construct(context, type);
      }
      
      public String toString() {
        return new LinkedHashMap<String, Object>() {{
          put("type", type);
          put("name", name);
          put("implementation", implementation);
          put("scope", scope);
        }}.toString();
      }
    };
    
    return factory(MyKey.newInstance(type, name), factory, scope);
  }
  
  public <T> ModuleAssembler factory(MyKey<T> key, InternalInspector<? extends T> factory, MyScope scope) {
    final InternalInspector<? extends T> scopedFactory = scope.scopeFactory(key.getRawType(), key.getName(), factory);
    factories.put(key, scopedFactory);
    if (scope == MyScope.SINGLETON) {
      //
    }
    return this;
  }
  
  
  <T> Constructor<T> getConstructor(Class<T> implementation) {
    try {
      return implementation.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Creates a {@link ModulerService} instance. Injects static members for classes
   * which were registered using {@link #injectStatics(Class...)}.
   *
   * @param loadSingletons If true, the moduler will load all singletons
   *  now. If false, the moduler will lazily load singletons. Eager loading
   *  is appropriate for production use while lazy loading can speed
   *  development.
   * @throws IllegalStateException if called more than once
   */
  public ModulerService create(boolean loadSingletons) {
    ensureNotCreated();
    created = true;
    final ModulerServiceImpl application = new ModulerServiceImpl(new HashMap<MyKey<?>, InternalInspector<?>>(factories));
    
    if (loadSingletons) {
      
      //Create ContextCallable
      application.callInContext(new ModulerServiceImpl.ContextualCallable<Void>() {
        public Void call(InspectorContext context) {
          for (InternalInspector<?> factory : singletonFactories) {
            //create Constructor base on context
            factory.create(context);
          }
          return null;
        }
      });
    }
    application.injectStatics(staticInjections);
    return application;
  }
  
  /**
   * Maps constant names to values.
   * 
   * @param properties
   * @return
   */
  public ModuleAssembler properties(Map<String, String> properties) {
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      constant(entry.getKey(), entry.getValue());
    }
    
    return this;
  }
  
  /**
   * Maps constant names to values.
   * 
   * @param properties
   * @return
   */
  public ModuleAssembler properties(Properties properties) {
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
      constant((String) entry.getKey(), (String) entry.getValue());
    }
    
    return this;
  }
  
  
  /**
   * Convenience method &nbsp;Equivalent to {@code alias(type, ModuleService.DEFAULT_NAME, type)}
   * @param <T>
   * @param type
   * @param alias
   * @return
   */
  public <T> ModuleAssembler alias(Class<T> type, String alias) {
    return alias(type, ModulerService.DEFAULT_NAME);
  }
  
  /**
   * Maps an existing {@link InternalInspector} to new name.
   * @param <T>
   * @param type of dependency
   * @param name of dependency
   * @param alias of to the dependency.
   * @return this assembler
   */
  public <T> ModuleAssembler alias(Class<T> type, String name, String alias) {
    return alias(MyKey.newInstance(type, name), MyKey.newInstance(type, alias));
  }
  
  /**
   * Maps an existing dependency. All methods in this class ultimately funnel through here.
   * @param <T>
   * @param key
   * @param aliasKey
   * @return
   */
  private <T> ModuleAssembler alias(final MyKey<T> key, final MyKey<T> aliasKey) {
    ensureNotCreated();
    checkKey(aliasKey);
    
    final InternalInspector<? extends T> scopedInspector = (InternalInspector<? extends T>) factories.get(key);
    if (scopedInspector == null) {
      throw new DependencyException("Dependency mapping for " + key + "doesn't exists.");
    }
    
    factories.put(aliasKey, scopedInspector);
    return this;
    
  }
  
  /**
   * Applies the given method interceptor to the methods matched by the class
   * and method matchers.
   *
   * @param classMatcher matches classes the interceptor should apply to. For
   *     example: {@code only(Runnable.class)}.
   * @param methodMatcher matches methods the interceptor should apply to. For
   *     example: {@code annotatedWith(Transactional.class)}.
   * @param interceptors to apply
   */
  public void intercept(Matcher<? super Class<?>> classMatcher,
                        Matcher<? super Method> methodMatcher,
                        MethodInterceptor... interceptors) {
    ensureNotCreated();
    proxyFactoryBuilder.intercept(classMatcher, methodMatcher, interceptors);
  }

   /*
   * Maps a constant value to given name.
   * 
   * @param name
   * @param value
   * @return
   */
  public ModuleAssembler constant(String name, String value) {
    return constant(String.class, name, value);
  }
  
  /**
   * Maps a constant value to given name.
   * 
   * @param name
   * @param value
   * @return
   */
  public ModuleAssembler constant(String name, int value) {
    return constant(int.class, name, value);
  }
  
  public ModuleAssembler constant(String name, long value) {
    return constant(long.class, name, value);
  }
  
  public ModuleAssembler constant(String name, boolean value) {
    return constant(boolean.class, name, value);
  }
  
  public ModuleAssembler constant(String name, double value) {
    return constant(double.class, name, value);
  }
  
  public ModuleAssembler constant(String name, float value) {
    return constant(float.class, name, value);
  }
  
  public ModuleAssembler constant(String name, short value) {
    return constant(short.class, name, value);
  }
  
  public ModuleAssembler constant(String name, char value) {
    return constant(char.class, name, value);
  }
  
  public ModuleAssembler constant(String name, Class value) {
    return constant(Class.class, name, value);
  }
  
  public <E extends Enum<E>> ModuleAssembler constant(String name, E value) {
    return constant(value.getDeclaringClass(), name, value);
  }
  
  /**
   * Maps a constant value to the given type and name.
   * 
   * @param <T>
   * @param type
   * @param name
   * @param value
   * @return
   */
  private <T> ModuleAssembler constant(final Class<T> type, String name, final T value) {
    InternalInspector<T> factory = new ConstantInspector<T>(value);
    
    return factory(MyKey.newInstance(type, name), factory, MyScope.DEFAULT);
  }
  
  /**
   * Ensures a key isn't already mapped.
   * @param key
   */
  private void checkKey(MyKey<?> key) {
    if (factories.containsKey(key)) {
      throw new DependencyException("Dependency mapping for " + key + " already exits.");
    }
  }
  
  /**
   * Currently we only support creating one {@link ModulerService} instance per builder.
   * If we want to support creating more than one container per builder,
   * we should move to a "factory factory" model where we create a factory
   * instance per Container. Right now, one factory instance would be
   * shared across all the containers, singletons synchronize on the
   * container when lazy loading, etc.
   */
  private void ensureNotCreated() {
    if (created) {
      throw new IllegalStateException("Container already created.");
    }
  }

  /**
   * Implemented by classes which participate in building a container.
   */
  public interface Command {

    /**
     * Contributes factories to the given builder.
     *
     * @param builder
     */
    void build(ModuleAssembler builder);
  }
}
