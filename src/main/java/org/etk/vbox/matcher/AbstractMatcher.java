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

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS Author : thanh_vucong
 * thanhvucong.78@gmail.com Nov 8, 2011
 */
public abstract class AbstractMatcher<T> implements Matcher<T> {

  public Matcher<T> and(final Matcher<? super T> other) {
    return new AndMatcher<T>(this, other);
  }

  public Matcher<T> or(Matcher<? super T> other) {
    return new OrMatcher<T>(this, other);
  }

  private static class AndMatcher<T> extends AbstractMatcher<T> implements Serializable {

    private final Matcher<? super T> a, b;

    public AndMatcher(Matcher<? super T> a, Matcher<? super T> b) {
      this.a = a;
      this.b = b;
    }

    @Override
    public boolean matches(T t) {
      return a.matches(t) && b.matches(t);
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof AndMatcher && ((AndMatcher) other).a.equals(a)
          && ((AndMatcher) other).b.equals(b);
    }

    @Override
    public int hashCode() {
      return 41 * (a.hashCode() ^ b.hashCode());
    }

    @Override
    public String toString() {
      return "and(" + a + ", " + b + ")";
    }

    private static final long serialVersionUID = 0;

  }

  private static class OrMatcher<T> extends AbstractMatcher<T> implements Serializable {
    private final Matcher<? super T> a, b;

    public OrMatcher(Matcher<? super T> a, Matcher<? super T> b) {
      this.a = a;
      this.b = b;
    }

    public boolean matches(T t) {
      return a.matches(t) || b.matches(t);
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof OrMatcher && ((OrMatcher) other).a.equals(a)
          && ((OrMatcher) other).b.equals(b);
    }

    @Override
    public int hashCode() {
      return 37 * (a.hashCode() ^ b.hashCode());
    }

    @Override
    public String toString() {
      return "or(" + a + ", " + b + ")";
    }

    private static final long serialVersionUID = 0;
  }
}
