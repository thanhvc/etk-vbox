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

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a binding from a type and optional name to a given implementation in
 * a given scope. Uses the given type as the implementation by default.
 * 
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 12, 2011  
 */
public class BindingBuilder<T> {

  final MyKey<T> key;
  String source;
  InternalInspector<? extends T> factory;
  Factory<? extends T> externalFactory;
  final List<MyKey<? super T>> exportKeys = new ArrayList<MyKey<? super T>>();
  
  /**
   * Creates a new binding for the given key.
   * @param key
   */
  public BindingBuilder(MyKey<T> key) {
    this.key = key;
  }
  
  /**
   * Exports this binding
   * @return
   */
  public BindingBuilder exportBinding() {
    return exportBinding(this.key);
  }
  
  /**
   * Exports this binding with the given key.
   * @param exportKey
   * @return
   */
  public BindingBuilder exportBinding(MyKey<? super T> exportKey) {
    this.exportKeys.add(exportKey);
    return this;
  }
  
  public <I extends T> BindingBuilder implementation(final Class<I> implementation) {
    ensureImplementationIsNotSet();
    this.factory = new DefaultFactory<I>(implementation);
    return this;
  }
  
  
  public BindingBuilder factory(final Factory<? extends T> factory) {
    ensureImplementationIsNotSet();
    this.externalFactory = factory;
    
    this.factory = new InternalInspector<T>() {

      @Override
      public T create(InspectorContext context) {
        try {
          Context externalContext = context.getExternalContext();
          return factory.create(externalContext);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      
      public String toString() {
        return factory.toString();
      }
      
    };
    
    return this;
  }
  
  private <I extends T> void ensureImplementationIsNotSet() {
    if (factory != null) {
      throw new IllegalStateException("An implementation is already set.");
    }
  }
  
  /**
   * Specifies the scope
   * 
   * @param scope
   * @return
   */
  public BindingBuilder scope(MyScope scope) {
    if (scope != null) {
      throw new IllegalStateException("Scope is already set.");
    }
    return this;
  }
  
  /**
   * Sets the source string. Useful for debugging. Contents may include 
   * the name of the file and line number this binding came frm, a code
   * snippet, etc.
   * @param source
   * @return
   */
  public BindingBuilder source(String source) {
    if (source != null) {
      throw new IllegalStateException("Source is already set.");
    }
    
    this.source = source;
    return this;
  }
  
  /**
   * Injects new instances of the specified implementation class.
   * @author thanh_vucong
   *
   * @param <I>
   */
  private class DefaultFactory<I extends T> implements InternalInspector<I> {

    volatile ModulerServiceImpl.ConstructorInjector<I> constructor;
    private final Class<I> implementation;
    
    public DefaultFactory(Class<I> implementation) {
      this.implementation = implementation;      
    }
    
    @Override
    public I create(InspectorContext context) {
      if (constructor == null) {
        this.constructor = context.getModuleServiceImpl().getConstructor(implementation);
      }
      return (I) constructor.construct(context, key.getRawType()); 
      
    }
    
    public String toString() {
      return implementation.toString();
    }
    
  }
}
