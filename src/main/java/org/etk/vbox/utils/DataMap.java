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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 14, 2011  
 */
public class DataMap<K, V> implements Map<K, V>, Serializable {

  private static final long serialVersionUID = 0;
  
  transient ConcurrentMap<Object, Object> delegate;
  
  public DataMap() {
    this.delegate = new ConcurrentHashMap<Object, Object>();
   
  }
  
  V internalGet(K key) {
    Object valueReference = delegate.get(key);
    return valueReference == null ? null : (V) valueReference;
  }
  
  @Override
  public int size() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean containsValue(Object value) {
    // TODO Auto-generated method stub
    return false;
  }

  protected interface Strategy {
    public Object execute(DataMap map, Object key, Object value);
  }
  
  private enum PutStrategy implements Strategy {

    PUT {
      public Object execute(DataMap map, Object key, Object value) {
        return map.delegate.put(key, value);
      }
    },
    REPLACE {
      @Override
      public Object execute(DataMap map, Object key, Object value) {
        return map.delegate.replace(key, value);
      }
    },
    PUT_IF_ABSENT {

      @Override
      public Object execute(DataMap map, Object key, Object value) {

        return map.delegate.putIfAbsent(key, value);
      }

    }
  }
  
  
  private static PutStrategy defaultPutStrategy;

  protected PutStrategy getPutStrategy() {
    return defaultPutStrategy;
  }
  
  protected Strategy putStrategy() {
    return PutStrategy.PUT;
  }
  
  @Override
  public V get(Object key) {
    ensureNotNull(key);
    return internalGet((K) key);
  }

  V execute(Strategy strategy, K key, V value) {
    ensureNotNull(key, value);
    Object valueReference = strategy.execute(this, key, value);
    return valueReference == null ? null : (V) value;
  }
  
  @Override
  public V put(K key, V value) {
    return execute(putStrategy(), key, value);
  }

  @Override
  public V remove(Object key) {
    ensureNotNull(key);
    return (V) delegate.remove(key);
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> m) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Set<K> keySet() {
    return (Set<K>) Collections.unmodifiableSet(delegate.keySet());
  }

  @Override
  public Collection<V> values() {
    return (Collection<V>) Collections.unmodifiableCollection(delegate.values());
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    return null;
  }
  
  class Entry implements Map.Entry<K, V> {
    K key;
    V value;
    
    public Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }

    public K getKey() {
      return key;
    }
   
    public V getValue() {
      return value;
    }

    public V setValue(V value) {
      return put(key, value);
    }
    
    public int hashCode() {
      return key.hashCode() * 31 + value.hashCode();
    }

    public boolean equals(Object o) {
      if (!(o instanceof DataMap.Entry)) {
        return false;
      }

      Entry entry = (Entry) o;
      return key.equals(entry.key) && value.equals(entry.value);
    }

    public String toString() {
      return key + "=" + value;
    }
  }
  
  static void ensureNotNull(Object... array) {
    for (int i = 0; i < array.length; i++) {
      if (array[i] == null) {
        throw new NullPointerException("Argument #" + i + " is null.");
      }
    }
  }
  
  static void ensureNotNull(Object o) {
    if (o == null) {
      throw new NullPointerException();
    }
  }

}
