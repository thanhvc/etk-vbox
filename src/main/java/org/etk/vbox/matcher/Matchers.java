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
package org.etk.vbox.matcher;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

/**
 * Matcher implementations. Supports matching classes and methods.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class Matchers {
  private Matchers() {}

  /**
   * Returns a matcher which matches any input.
   */
  public static Matcher<Object> any() {
    return ANY;
  }

  private static final Matcher<Object> ANY = new Any();

  private static class Any extends AbstractMatcher<Object> implements Serializable {
    public boolean matches(Object o) {
      return true;
    }

    @Override public String toString() {
      return "any()";
    }

    public Object readResolve() {
      return any();
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Inverts the given matcher.
   */
  public static <T> Matcher<T> not(final Matcher<? super T> p) {
    return new Not<T>(p);
  }

  private static class Not<T> extends AbstractMatcher<T> implements Serializable {
    final Matcher<? super T> delegate;

    private Not(Matcher<? super T> delegate) {
      this.delegate = checkNotNull(delegate, "delegate");
    }

    public boolean matches(T t) {
      return !delegate.matches(t);
    }

    @Override public boolean equals(Object other) {
      return other instanceof Not
          && ((Not) other).delegate.equals(delegate);
    }

    @Override public int hashCode() {
      return -delegate.hashCode();
    }

    @Override public String toString() {
      return "not(" + delegate + ")";
    }

    private static final long serialVersionUID = 0;
  }

  private static void checkForRuntimeRetention(
      Class<? extends Annotation> annotationType) {
    Retention retention = annotationType.getAnnotation(Retention.class);
    checkArgument(retention != null && retention.value() == RetentionPolicy.RUNTIME,
        "Annotation " + annotationType.getSimpleName() + " is missing RUNTIME retention");
  }

  /**
   * Returns a matcher which matches elements (methods, classes, etc.)
   * with a given annotation.
   */
  public static Matcher<AnnotatedElement> annotatedWith(
      final Class<? extends Annotation> annotationType) {
    return new AnnotatedWithType(annotationType);
  }

  private static class AnnotatedWithType extends AbstractMatcher<AnnotatedElement>
      implements Serializable {
    private final Class<? extends Annotation> annotationType;

    public AnnotatedWithType(Class<? extends Annotation> annotationType) {
      this.annotationType = checkNotNull(annotationType, "annotation type");
      checkForRuntimeRetention(annotationType);
    }

    public boolean matches(AnnotatedElement element) {
      return element.getAnnotation(annotationType) != null;
    }

    @Override public boolean equals(Object other) {
      return other instanceof AnnotatedWithType
          && ((AnnotatedWithType) other).annotationType.equals(annotationType);
    }

    @Override public int hashCode() {
      return 37 * annotationType.hashCode();
    }

    @Override public String toString() {
      return "annotatedWith(" + annotationType.getSimpleName() + ".class)";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches elements (methods, classes, etc.)
   * with a given annotation.
   */
  public static Matcher<AnnotatedElement> annotatedWith(
      final Annotation annotation) {
    return new AnnotatedWith(annotation);
  }

  private static class AnnotatedWith extends AbstractMatcher<AnnotatedElement>
      implements Serializable {
    private final Annotation annotation;

    public AnnotatedWith(Annotation annotation) {
      this.annotation = checkNotNull(annotation, "annotation");
      checkForRuntimeRetention(annotation.annotationType());
    }

    public boolean matches(AnnotatedElement element) {
      Annotation fromElement = element.getAnnotation(annotation.annotationType());
      return fromElement != null && annotation.equals(fromElement);
    }

    @Override public boolean equals(Object other) {
      return other instanceof AnnotatedWith
          && ((AnnotatedWith) other).annotation.equals(annotation);
    }

    @Override public int hashCode() {
      return 37 * annotation.hashCode();
    }

    @Override public String toString() {
      return "annotatedWith(" + annotation + ")";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches subclasses of the given type (as well as
   * the given type).
   */
  public static Matcher<Class> subclassesOf(final Class<?> superclass) {
    return new SubclassesOf(superclass);
  }

  private static class SubclassesOf extends AbstractMatcher<Class>
      implements Serializable {
    private final Class<?> superclass;

    public SubclassesOf(Class<?> superclass) {
      this.superclass = checkNotNull(superclass, "superclass");
    }

    public boolean matches(Class subclass) {
      return superclass.isAssignableFrom(subclass);
    }

    @Override public boolean equals(Object other) {
      return other instanceof SubclassesOf
          && ((SubclassesOf) other).superclass.equals(superclass);
    }

    @Override public int hashCode() {
      return 37 * superclass.hashCode();
    }

    @Override public String toString() {
      return "subclassesOf(" + superclass.getSimpleName() + ".class)";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches objects equal to the given object.
   */
  public static Matcher<Object> only(Object value) {
    return new Only(value);
  }

  private static class Only extends AbstractMatcher<Object>
      implements Serializable {
    private final Object value;

    public Only(Object value) {
      this.value = checkNotNull(value, "value");
    }

    public boolean matches(Object other) {
      return value.equals(other);
    }

    @Override public boolean equals(Object other) {
      return other instanceof Only
          && ((Only) other).value.equals(value);
    }

    @Override public int hashCode() {
      return 37 * value.hashCode();
    }

    @Override public String toString() {
      return "only(" + value + ")";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches only the given object.
   */
  public static Matcher<Object> identicalTo(final Object value) {
    return new IdenticalTo(value);
  }

  private static class IdenticalTo extends AbstractMatcher<Object>
      implements Serializable {
    private final Object value;

    public IdenticalTo(Object value) {
      this.value = checkNotNull(value, "value");
    }

    public boolean matches(Object other) {
      return value == other;
    }

    @Override public boolean equals(Object other) {
      return other instanceof IdenticalTo
          && ((IdenticalTo) other).value == value;
    }

    @Override public int hashCode() {
      return 37 * System.identityHashCode(value);
    }

    @Override public String toString() {
      return "identicalTo(" + value + ")";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches classes in the given package. Packages are specific to their
   * classloader, so classes with the same package name may not have the same package at runtime.
   */
  public static Matcher<Class> inPackage(final Package targetPackage) {
    return new InPackage(targetPackage);
  }

  private static class InPackage extends AbstractMatcher<Class> implements Serializable {
    private final transient Package targetPackage;
    private final String packageName;

    public InPackage(Package targetPackage) {
      this.targetPackage = checkNotNull(targetPackage, "package");
      this.packageName = targetPackage.getName();
    }

    public boolean matches(Class c) {
      return c.getPackage().equals(targetPackage);
    }

    @Override public boolean equals(Object other) {
      return other instanceof InPackage
          && ((InPackage) other).targetPackage.equals(targetPackage);
    }

    @Override public int hashCode() {
      return 37 * targetPackage.hashCode();
    }

    @Override public String toString() {
      return "inPackage(" + targetPackage.getName() + ")";
    }

    public Object readResolve() {
      return inPackage(Package.getPackage(packageName));
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches classes in the given package and its subpackages. Unlike
   * {@link #inPackage(Package) inPackage()}, this matches classes from any classloader.
   * 
   * @since 2.0
   */
  public static Matcher<Class> inSubpackage(final String targetPackageName) {
    return new InSubpackage(targetPackageName);
  }

  private static class InSubpackage extends AbstractMatcher<Class> implements Serializable {
    private final String targetPackageName;

    public InSubpackage(String targetPackageName) {
      this.targetPackageName = targetPackageName;
    }

    public boolean matches(Class c) {
      String classPackageName = c.getPackage().getName();
      return classPackageName.equals(targetPackageName)
          || classPackageName.startsWith(targetPackageName + ".");
    }

    @Override public boolean equals(Object other) {
      return other instanceof InSubpackage
          && ((InSubpackage) other).targetPackageName.equals(targetPackageName);
    }

    @Override public int hashCode() {
      return 37 * targetPackageName.hashCode();
    }

    @Override public String toString() {
      return "inSubpackage(" + targetPackageName + ")";
    }

    private static final long serialVersionUID = 0;
  }

  /**
   * Returns a matcher which matches methods with matching return types.
   */
  public static Matcher<Method> returns(
      final Matcher<? super Class<?>> returnType) {
    return new Returns(returnType);
  }

  private static class Returns extends AbstractMatcher<Method> implements Serializable {
    private final Matcher<? super Class<?>> returnType;

    public Returns(Matcher<? super Class<?>> returnType) {
      this.returnType = checkNotNull(returnType, "return type matcher");
    }

    public boolean matches(Method m) {
      return returnType.matches(m.getReturnType());
    }

    @Override public boolean equals(Object other) {
      return other instanceof Returns
          && ((Returns) other).returnType.equals(returnType);
    }

    @Override public int hashCode() {
      return 37 * returnType.hashCode();
    }

    @Override public String toString() {
      return "returns(" + returnType + ")";
    }

    private static final long serialVersionUID = 0;
  }
}
