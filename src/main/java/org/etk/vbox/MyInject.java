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

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.etk.vbox.ModulerService.DEFAULT_NAME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.etk.vbox.ModulerService;


/**
 * <p>Annotates members and parameters which should have their value[s]
 * injected.
 *
 */
@Target({METHOD, CONSTRUCTOR, FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface MyInject {

  /**
   * Dependency name. Defaults to {@link ModulerService#DEFAULT_NAME}.
   */
  String value() default DEFAULT_NAME;

  /**
   * Whether or not injection is required. Applicable only to methods and
   * fields (not constructors or parameters).
   */
  boolean required() default true;
}