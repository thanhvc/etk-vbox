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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.etk.vbox.ModuleAssembler;
import org.etk.vbox.ModulerService;
import org.etk.vbox.MyInject;
import org.etk.vbox.MyListenerInject;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 11, 2011  
 */
public class AssemblerWithMultiListenerTest extends TestCase {

  public void testInjectorLog() throws Exception {
    ModulerService service = createApplicationForLog();
    AImpl aImpl = service.inject(AImpl.class);
    
    assertNotNull(aImpl);
    assertNotNull(aImpl.getList());
    assertEquals("Size must be equals 2.", 2, aImpl.getSize());
    
    aImpl.doInsert();
  }
  

  private ModulerService createApplicationForLog() {
    ModuleAssembler builder = new ModuleAssembler();
    builder.factory(Listener.class, "LogBeginListener", LogBeginListenerImpl.class);
    builder.factory(Listener.class, "LogEndListener", LogEndListenerImpl.class);
    builder.constant("begin", Listener.BEGIN_STAGE);
    builder.constant("end", Listener.END_STAGE);
    return builder.create(false);
  }
  
  public void testInjectorDebug() throws Exception {
    ModulerService service = createApplicationForDebug();
    AImpl aImpl = service.inject(AImpl.class);
    
    assertNotNull(aImpl);
    assertNotNull(aImpl.getList());
    assertEquals("Size must be equals 2.", 2, aImpl.getSize());
    
    aImpl.doInsert();
  }
  
  private ModulerService createApplicationForDebug() {
    ModuleAssembler builder = new ModuleAssembler();
    builder.factory(Listener.class, "DebugBeginListener", DebugBeginListenerImpl.class);
    builder.factory(Listener.class, "DebugEndListener", DebugEndListenerImpl.class);
    builder.constant("begin", Listener.BEGIN_STAGE);
    builder.constant("end", Listener.END_STAGE);
    return builder.create(false);
  }
  
  public void testInjectorMyListenerInjectorFail() throws Exception {
    ModulerService service = createApplicationMyListenerInjectFail();
    try {
      FImpl aImpl = service.inject(FImpl.class);
      fail();
    } catch (Exception e) {}
  }
  
  private ModulerService createApplicationMyListenerInjectFail() {
    ModuleAssembler builder = new ModuleAssembler();
    builder.factory(Listener.class, "DebugBeginListener", DebugBeginListenerImpl.class);
    builder.factory(Listener.class, "DebugEndListener", DebugEndListenerImpl.class);
    builder.constant("begin", Listener.BEGIN_STAGE);
    builder.constant("end", Listener.END_STAGE);
    return builder.create(false);
  }
  
  interface A {
    List<Listener> getList();
    public int getSize();
    void addListener(Listener listener);
    void doInsert();
    
  }
  
  static class AImpl implements A {
    List<Listener> list = new ArrayList<Listener>();
    
    
    @MyListenerInject
    public void addListener(Listener listener) {
        this.list.add(listener);        
    }

    @Override
    public int getSize() {
      return list.size();
    }

    @Override
    public List<Listener> getList() {
      return this.list;
    }

    @Override
    public void doInsert() {
      preAction();
      System.out.println("doInsert() execution.");
      postAction();
    }
    
    private void preAction() {
      for(Listener listener : list) {
        if (Listener.BEGIN_STAGE.equals(listener.getStageType())) {
          listener.execute();
        }
      }
    }
    
    private void postAction() {
      for(Listener listener : list) {
        if (Listener.END_STAGE.equals(listener.getStageType())) {
          listener.execute();
        }
      }
    }
    
   
  }
  
  /**
   * Provides for TestCase fail
   * 
   * @author thanh_vucong
   *
   */
  interface F {
    void addListener(Listener listener, @MyInject("begin")String stage);
  }
  /**
   * Provides for testcase fail. Only use the @MyListenerInject 
   * annotation when method has only one argument.
   * 
   * @author thanh_vucong
   *
   */
  static class FImpl implements F {
    List<Listener> list = new ArrayList<Listener>();
    
    @MyListenerInject
    public void addListener(Listener listener, @MyInject("begin") String stage) {
        this.list.add(listener);        
    }

    
  }
  
   
  interface Listener {
    public static String BEGIN_STAGE = "begin";
    public static String END_STAGE = "end";
    void execute();
    String getStageType();
    
  }
  
  static class LogBeginListenerImpl implements Listener {
    final String stage;
    public LogBeginListenerImpl() {
      this.stage = BEGIN_STAGE;
    }
    
    @Override
    public void execute() {
      System.out.println("LOG:BEGIN Listener information.");
    }

    @Override
    public String getStageType() {
      return this.stage;
    }
  }
  
  static class LogEndListenerImpl implements Listener {
    
    final String stage;
    public LogEndListenerImpl() {
      this.stage = END_STAGE;
      
    }
    
    @Override
    public void execute() {
      System.out.println("LOG:END Listener information.");
    }
    
    @Override
    public String getStageType() {
      return this.stage;
    }
    
  }
  
  static class DebugBeginListenerImpl implements Listener {
    
    final String stage;
    @MyInject
    public DebugBeginListenerImpl(@MyInject("begin") String stage) {
      this.stage = stage;
    }
    
    @Override
    public void execute() {
      System.out.println("DEBUG::BEGIN Listener information.");
    }
    
    @Override
    public String getStageType() {
      return this.stage;
    }
    
  }
  
  static class DebugEndListenerImpl implements Listener {
    final String stage;
    
    @MyInject
    public DebugEndListenerImpl(@MyInject("end") String stage) {
      this.stage = stage;
    }
    
    @Override
    public void execute() {
      System.out.println("DEBUG::END Listener information.");
    }
    
    @Override
    public String getStageType() {
      return this.stage;
    }
  }
}