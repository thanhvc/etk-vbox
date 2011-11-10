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

/**
 * An object capable of providing instances of type {@code T}. Providers are used in numerous ways
 * by Vbox:
 *
 * <ul>
 * <li>When the default means for obtaining instances (an injectable or parameterless constructor)
 * is insufficient for a particular binding, the module can specify a custom {@code Provider}
 * instead, to control exactly how vbox creates or obtains instances for the binding.
 *
 * <li>An implementation class may always choose to have a {@code Provider<T>} instance injected,
 * rather than having a {@code T} injected directly.  This may give you access to multiple
 * instances, instances you wish to safely mutate and discard, instances which are out of scope
 * (e.g. using a {@code @RequestScoped} object from within a {@code @SessionScoped} object), or
 * instances that will be initialized lazily.
 *
 * <li>A custom {@link Scope} is implemented as a decorator of {@code Provider<T>}, which decides
 * when to delegate to the backing provider and when to provide the instance some other way.
 *
 * <li>The {@link Injector} offers access to the {@code Provider<T>} it uses to fulfill requests
 * for a given key, via the {@link Injector#getProvider} methods.
 * </ul>
 *
 * @param <T> the type of object this provides
 *
 * @author crazybob@google.com (Bob Lee)
 */
public interface Provider<T> extends javax.inject.Provider<T> {

  /**
   * Provides an instance of {@code T}. Must never return {@code null}.
   *
   * @throws OutOfScopeException when an attempt is made to access a scoped object while the scope
   *     in question is not currently active
   * @throws ProvisionException if an instance cannot be provided. Such exceptions include messages
   *     and throwables to describe why provision failed.
   */
  T get();
}

