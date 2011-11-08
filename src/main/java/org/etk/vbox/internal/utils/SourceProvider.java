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
package org.etk.vbox.internal.utils;

import static com.google.common.collect.Iterables.concat;


import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Provides access to the calling line of code.
 * 
 * @author crazybob@google.com (Bob Lee)
 */
public final class SourceProvider {

  /** Indicates that the source is unknown. */
  public static final Object UNKNOWN_SOURCE = "[unknown source]";

  private final ImmutableSet<String> classNamesToSkip;

  public static final SourceProvider DEFAULT_INSTANCE
      = new SourceProvider(ImmutableSet.of(SourceProvider.class.getName()));

  private SourceProvider(Iterable<String> classesToSkip) {
    this.classNamesToSkip = ImmutableSet.copyOf(classesToSkip);
  }

  /** Returns a new instance that also skips {@code moreClassesToSkip}. */
  public SourceProvider plusSkippedClasses(Class... moreClassesToSkip) {
    return new SourceProvider(concat(classNamesToSkip, asStrings(moreClassesToSkip)));
  }

  /** Returns the class names as Strings */
  private static List<String> asStrings(Class... classes) {
    List<String> strings = Lists.newArrayList();
    for (Class c : classes) {
      strings.add(c.getName());
    }
    return strings;
  }

  /**
   * Returns the calling line of code. The selected line is the nearest to the top of the stack that
   * is not skipped.
   */
  public StackTraceElement get() {
    for (final StackTraceElement element : new Throwable().getStackTrace()) {
      String className = element.getClassName();
      if (!classNamesToSkip.contains(className)) {
        return element;
      }
    }
    throw new AssertionError();
  }
}
