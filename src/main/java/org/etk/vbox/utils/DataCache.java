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
package org.etk.vbox.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 14, 2011  
 */
public abstract class DataCache<K, V> extends DataMap<K, V>{
  
  private static final long serialVersionUID = 0;

  transient ConcurrentMap<Object, Future<V>> futures = new ConcurrentHashMap<Object, Future<V>>();

  transient ThreadLocal<Future<V>> localFuture = new ThreadLocal<Future<V>>();
  
  /**
   * Override to lazy load values. Use as an alternative to {@link
   * #put(Object,Object)}. Invoked by getter if value isn't already cached.
   * Must not return {@code null}. This method will not be called again until
   * the garbage collector reclaims the returned value.
   */
  protected abstract V create(K key);

  V internalCreate(K key) {
    try {
      FutureTask<V> futureTask = new FutureTask<V>(new CallableCreate(key));

      // use a reference so we get the same equality semantics.
      Future<V> future = futures.putIfAbsent(key, futureTask);
      if (future == null) {
        // winning thread.
        try {
          if (localFuture.get() != null) {
            throw new IllegalStateException("Nested creations within the same cache are not allowed.");
          }
          localFuture.set(futureTask);
          futureTask.run();
          V value = futureTask.get();
          
          //put the data to caching
          putStrategy().execute(this, key, value);
          return value;
        } finally {
          localFuture.remove();
          futures.remove(key);
        }
      } else {
        // wait for winning thread.
        return future.get();
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else if (cause instanceof Error) {
        throw (Error) cause;
      }
      throw new RuntimeException(cause);
    }
  }

  /**
   * {@inheritDoc}
   *
   * If this map does not contain an entry for the given key and {@link
   * #create(Object)} has been overridden, this method will create a new
   * value, put it in the map, and return it.
   *
   * @throws NullPointerException if {@link #create(Object)} returns null.
   * @throws java.util.concurrent.CancellationException if the creation is
   *  cancelled. See {@link #cancel()}.
   */
  @SuppressWarnings("unchecked")
  @Override public V get(final Object key) {
    V value = super.get(key);
    return (value == null) ? internalCreate((K) key) : value;
  }

  /**
   * Cancels the current {@link #create(Object)}. Throws {@link
   * java.util.concurrent.CancellationException} to all clients currently
   * blocked on {@link #get(Object)}.
   */
  protected void cancel() {
    Future<V> future = localFuture.get();
    if (future == null) {
      throw new IllegalStateException("Not in create().");
    }
    future.cancel(false);
  }

  class CallableCreate implements Callable<V> {

    K key;

    public CallableCreate(K key) {
      this.key = key;
    }

    public V call() {
      // try one more time (a previous future could have come and gone.)
      V value = internalGet(key);
      if (value != null) {
        return value;
      }

      //TODO This method to call create() method for ModulerImpl.injectors(...)
      // create value.
      value = create(key);
      if (value == null) {
        throw new NullPointerException("create(K) returned null for: " + key);
      }
      return value;
    }
  }

}
