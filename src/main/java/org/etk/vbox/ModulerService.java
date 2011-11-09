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

import org.etk.vbox.MyScope;

/**
 * Injects dependencies into constructors, methods, and fields annotated with
 * {@code MyInject}. Immutable.
 * 
 * <p> When injecting a method or constructor, you can additionally annotate its parameters with
 * {@link MyInject} and specify a dependency name. When a parameter has no annotation, 
 * the ModuleService uses the name from the method or constructor's {@link MyInject} annotation 
 * respectively.
 * 
 * <p> For example:
 * class Foo {
 *   //Inject the int constant named "i"
 *   &#64;MyInject("i") int i;
 *   
 *   //Inject the default implementation of Bar and the String constant named "s"
 *   &#64;Inject Foo(Bar bar, @Inject("s") String s) {
 *      ...
 *   }
 *   
 *   // Inject the default implementation of Baz and the Bob implementation
 *   // named "foo".
 *   &#64;Inject void initialize(Baz baz, @Inject("foo") Bob bob) {
 *      ...
 *   }
 *   
 *   //Inject the default implementation of Tee 
 *   &#64;Inject void setTee(Tee tee) {
 *      ...
 *   }
 *   
 * }
 * 
 * </pre>
 * 
 * <p>To create and inject an instance of {@code Foo}:
 * <pre>
 *   ModuleService module = ...;
 *   Foo foo = module.inject(Foo.class);
 *</pre>   
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          exo@exoplatform.com
 * Oct 12, 2011  
 */
public interface ModulerService {

  /**
   * Default dependency name.
   */
  String DEFAULT_NAME = "default";

  /**
   * Injects dependencies into the fields and methods of an existing object.
   */
  void inject(Object o);

  /**
   * Creates and injects a new instance of type {@code implementation}.
   */
  <T> T inject(Class<T> implementation);

  /**
   * Gets an instance of the given dependency which was declared in
   * {@link ModuleAssembler}.
   */
  <T> T getInstance(Class<T> type, String name);

  /**
   * Convenience method.&nbsp;Equivalent to {@code getInstance(type,
   * DEFAULT_NAME)}.
   */
  <T> T getInstance(Class<T> type);

  /**
   * Sets the scope strategy for the current thread.
   */
  void setScopeStrategy(MyScope.Strategy scopeStrategy);

  /**
   * Removes the scope strategy for the current thread.
   */
  void removeScopeStrategy();
  
  /**
   * Checks whether the {@code ModuleService} has a binding for given key.
   * 
   * @param key binding key
   * @return {@code true} if a binding existing for the given key.
   */
  boolean hasBindingFor(MyKey<?> key);
  
}
