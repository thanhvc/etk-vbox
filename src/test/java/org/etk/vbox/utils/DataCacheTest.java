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

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 14, 2011  
 */
public class DataCacheTest extends TestCase {

  DataCache<String, String> caches = new DataCache<String, String>() {
    
    @Override
    protected String create(String key) {
      System.out.println("DataCacheTest.create:: running here.");
      System.out.println("Key:: " + key);
      return "ThanhVC";
    }
  };
  
  public void testPutLazyLoad() throws Exception {
    String value = caches.get("k1");
    assertEquals("ThanhVC", value);
    
    value = caches.get("k1");
    assertEquals("ThanhVC", value);
    
  }
}

