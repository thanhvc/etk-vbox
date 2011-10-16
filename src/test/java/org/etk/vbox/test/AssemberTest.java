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
package org.etk.vbox.test;
import junit.framework.TestCase;

import org.etk.vbox.ModuleAssembler;
import org.etk.vbox.ModulerService;
import org.etk.vbox.MyInject;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Oct 15, 2011  
 */
public class AssemberTest extends TestCase {

  public void testInjector() throws Exception {
    ModulerService application = createApplication();
    AImpl aImpl = application.inject(AImpl.class);
    
    assertNotNull(aImpl);
    assertNotNull(aImpl.getB());
    assertEquals("this class is owner B", aImpl.getB().getOwner());
    
    assertNotNull(application.getInstance(B.class));
    
  }
  
  public void testFieldInjector() throws Exception {
    ModulerService application = createApplication();
    AImpl aImpl = application.inject(AImpl.class);
    
    assertNotNull(aImpl);
    assertNotNull(aImpl.getB());
    assertEquals("this class is owner B", aImpl.getB().getOwner());
    
    assertNotNull(application.getInstance(B.class));
    A cImpl = application.getInstance(A.class);
    assertNotNull(cImpl);
    assertNotNull(cImpl.getB());
  }
  
  private ModulerService createApplication() {
    ModuleAssembler builder = new ModuleAssembler();
    
    builder.factory(B.class, BImpl.class);
    builder.factory(A.class, CImpl.class);
    return builder.create(false);
  }
  interface A {
    B getB();
  }
  
  static class AImpl implements A {
    B b;
    
    @MyInject
    public AImpl(B b) {
      this.b = b;
    }

    @Override
    public B getB() {
      return b;
    }
   
  }
  
  static class CImpl implements A {

    @MyInject B b;
    
    @Override
    public B getB() {
      return b;
    }
    
  }
  
  interface B {
    String getOwner();
  }
  
  static class BImpl implements B {
    public BImpl() {
      
    }

    @Override
    public String getOwner() {
      
      return "this class is owner B";
    }
    
  }
}
