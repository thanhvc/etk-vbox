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


import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.etk.vbox.MyScope.SINGLETON;

import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * A semi-useless microbenchmark. Spring and Guice constuct the same object
 * graph a bunch of times, and we see who can construct the most per second.
 * As of this writing Guice is more than 50X faster. Also useful for comparing 
 * pure Java configuration options.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class PerformanceComparison {

  public static void main(String[] args) throws Exception {
    // Once warm up. Takes lazy loading out of the equation and ensures we
    // created the graphs properly.
    validate(springFactory);
    validate(juiceFactory);
    validate(byHandFactory);

    for (int i2 = 0; i2 < 10; i2++) {
      iterate(springFactory, "Spring:  ");
      iterate(juiceFactory,  "Guice:   ");
      iterate(byHandFactory, "By Hand: ");

      System.err.println();
    }
  }

  static final Callable<Foo> springFactory = new Callable<Foo>() {

    final DefaultListableBeanFactory beanFactory;

    {
      beanFactory = new DefaultListableBeanFactory();

      RootBeanDefinition tee = new RootBeanDefinition(TeeImpl.class, true);
      tee.setLazyInit(true);
      ConstructorArgumentValues teeValues = new ConstructorArgumentValues();
      teeValues.addGenericArgumentValue("test");
      tee.setConstructorArgumentValues(teeValues);

      RootBeanDefinition bar = new RootBeanDefinition(BarImpl.class, false);
      ConstructorArgumentValues barValues = new ConstructorArgumentValues();
      barValues.addGenericArgumentValue(new RuntimeBeanReference("tee"));
      barValues.addGenericArgumentValue(5);
      bar.setConstructorArgumentValues(barValues);

      RootBeanDefinition foo = new RootBeanDefinition(Foo.class, false);
      MutablePropertyValues fooValues = new MutablePropertyValues();
      fooValues.addPropertyValue("i", 5);
      fooValues.addPropertyValue("bar", new RuntimeBeanReference("bar"));
      fooValues.addPropertyValue("copy", new RuntimeBeanReference("bar"));
      fooValues.addPropertyValue("s", "test");
      foo.setPropertyValues(fooValues);

      beanFactory.registerBeanDefinition("foo", foo);
      beanFactory.registerBeanDefinition("bar", bar);
      beanFactory.registerBeanDefinition("tee", tee);
    }

    public Foo call() throws Exception {
      return (Foo) beanFactory.getBean("foo");
    }
  };

  static final Callable<Foo> juiceFactory = new Callable<Foo>() {
    final ModulerService service;
    {
      ModuleAssembler builder = new ModuleAssembler();
      builder.factory(Tee.class, TeeImpl.class);
      builder.factory(Bar.class, BarImpl.class);
      builder.factory(Foo.class, Foo.class);
      builder.constant("i", 5);
      builder.constant("s", "test");
      
      service = builder.create(false);
    };

    public Foo call() throws Exception {
      return service.getInstance(Foo.class);
    }
  };

  static final Callable<Foo> byHandFactory = new Callable<Foo>() {
    final Tee tee = new TeeImpl("test");
    public Foo call() throws Exception {
      Foo foo = new Foo();
      foo.setI(5);
      foo.setS("test");
      Bar bar = new BarImpl(tee, 5);
      Bar copy = new BarImpl(tee, 5);
      foo.setBar(bar);
      foo.setCopy(copy);
      return foo;
    }
  };

  static void validate(Callable<Foo> t) throws Exception {
    Foo foo = t.call();
    assertEquals(5, foo.i);
    assertEquals("test", foo.s);
    //because on this is singleton, can not create new instance.
    assertSame(foo.bar.getTee(), foo.copy.getTee());
    assertEquals(5, foo.bar.getI());
    assertEquals("test", foo.bar.getTee().getS());
  }

  static DecimalFormat format = new DecimalFormat();

  static void iterate(Callable<Foo> callable, String label) throws Exception {
    int count = 100000;
    long time = System.currentTimeMillis();
    for (int i = 0; i < count; i++) {
      callable.call();
    }
    time = System.currentTimeMillis() - time;
    System.err.println(label + format.format(count * 1000 / time) + " creations/s");
  }

  public static class Foo {

    Bar bar;
    Bar copy;
    String s;
    int i;

    @MyInject("i")
    public void setI(int i) {
      this.i = i;
    }

    @MyInject
    public void setBar(Bar bar) {
      this.bar = bar;
    }

    @MyInject
    public void setCopy(Bar copy) {
      this.copy = copy;
    }

    @MyInject("s")
    public void setS(String s) {
      this.s = s;
    }
  }

  interface Bar {

    Tee getTee();
    int getI();
  }

  public static class BarImpl implements Bar {

    final int i;
    final Tee tee;

    @MyInject
    public BarImpl(Tee tee, @MyInject("i") int i) {
      this.tee = tee;
      this.i = i;
    }

    public Tee getTee() {
      return tee;
    }

    public int getI() {
      return i;
    }
  }

  interface Tee {

    String getS();
  }

  @MyScoped(SINGLETON)
  public static class TeeImpl implements Tee {

    final String s;

    @MyInject
    public TeeImpl(@MyInject("s") String s) {
      this.s = s;
    }

    public String getS() {
      return s;
    }
  }
}