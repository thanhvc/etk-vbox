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

import java.lang.reflect.Member;
import java.util.LinkedHashMap;

import org.etk.vbox.Context;
import org.etk.vbox.ExternalContext;
import org.etk.vbox.ModulerService;
import org.etk.vbox.ModulerServiceImpl;
import org.etk.vbox.MyKey;
import org.etk.vbox.MyScope;




/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 14, 2011  
 */
public class ExternalContext<T> implements Context {
  final Member member;
  final MyKey<T> key;
  final ModulerServiceImpl application;
  
  public ExternalContext(Member member, MyKey<T> key, ModulerServiceImpl application) {
    this.member = member;
    this.key = key;
    this.application = application;
  }

  public Class<T> getType() {
    return key.getType();
  }

  public MyScope.Strategy getScopeStrategy() {
    return application.localScopeStrategy.get();
  }

  public ModulerService getApplication() {
    return application;
  }

  public Member getMember() {
    return member;
  }

  public String getName() {
    return key.getName();
  }

  public String toString() {
    return "Context" + new LinkedHashMap<String, Object>() {{
      put("member", member);
      put("type", getType());
      put("name", getName());
      put("application", application);
    }}.toString();
  }

  static <T> ExternalContext<T> newInstance(Member member, MyKey<T> key,
                                            ModulerServiceImpl application) {
    return new ExternalContext<T>(member, key, application);
  }

}
