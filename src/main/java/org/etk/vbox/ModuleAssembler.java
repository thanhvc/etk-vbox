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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.etk.vbox.ModuleAssembler;
import org.etk.vbox.InspectorContext;
import org.etk.vbox.InternalInspector;
import org.etk.vbox.ModulerService;
import org.etk.vbox.ModulerServiceImpl;
import org.etk.vbox.MyKey;
import org.etk.vbox.MyScope;
import org.etk.vbox.MyScoped;

/**
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
          this.constructor = context.getApplicationImpl().getConstructor(implementation);
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
    final InternalInspector<? extends T> scopedFactory = scope.scopeFactory(key.getType(), key.getName(), factory);
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
