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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanhvucong.78@gmail.com
 * Nov 2, 2011  
 */
public class TypeLiteral<T> {

  final Class<? super T> rawType;
  final Type type;
  final int hashCode;
  
  /**
   * Constructs a new type literal. Derives represented class from type
   * parameter
   * <p>Clients create an empty anonymous subclass. Doing so embeds the type
   * parameter in the anonymous class's type hierarchy so we can reconstitute it
   * at runtime despite erasure.
   */
  protected TypeLiteral() {
    this.type = getSuperClassTypeParameter(getClass());
    this.rawType = (Class<? super T>) MoreTypes.getRawType(type);
    this.hashCode = type.hashCode();
  }
  
  /**
   * Unsafe. Constructors a type literal manually.
   * @param type
   */
  TypeLiteral(Type type) {
    this.type = canoncalize(checkNotNull(type, "type"));
    this.rawType = (Class<? super T>) MoreTypes.getRawType(this.type);
    this.hashCode = this.type.hashCode();
  }
  
  /**
   * Returns the type from super class's type parameter in {@link MoreTypes#canonicalize(Type)
   * canonical form}.
   */
  static Type getSuperclassTypeParameter(Class<?> subclass) {
    Type superclass = subclass.getGenericSuperclass();
    
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing type parameter.");
    }
    
    ParameterizedType parameterized = (ParameterizedType) superclass;
    return canonicalize(parameterized.getActualTypeArguments()[0]);
  }
  
  static TypeLiteral<?> fromSuperclassTypeParameter(Class<?> subclass) {
    return new TypeLiteral<Object>(getSuperclassTypeParameter(subclass));
  }
  
  /**
   * Returns the raws (non-generic) type for this type.
   * @return
   */
  public final Class<? super T> getRawType() {
    return rawType;
  }
  
  public final Type getType() {
    return type;
  }
  
  /**
   * Gets the type of this type's provider.
   */
  @SuppressWarnings("unchecked")
  final TypeLiteral<Provider<T>> providerType() {
    // This cast is safe and wouldn't generate a warning if Type had a type
    // parameter.
    return (TypeLiteral<Provider<T>>) get(Types.providerOf(getType()));
  }

  @Override public final int hashCode() {
    return this.hashCode;
  }

  @Override public final boolean equals(Object o) {
    return o instanceof TypeLiteral<?>
        && MoreTypes.equals(type, ((TypeLiteral) o).type);
  }

  @Override public final String toString() {
    return MoreTypes.typeToString(type);
  }

  /**
   * Gets type literal for the given {@code Type} instance.
   */
  public static TypeLiteral<?> get(Type type) {
    return new TypeLiteral<Object>(type);
  }

  /**
   * Gets type literal for the given {@code Class} instance.
   */
  public static <T> TypeLiteral<T> get(Class<T> type) {
    return new TypeLiteral<T>(type);
  }


  /** Returns an immutable list of the resolved types. */
  private List<TypeLiteral<?>> resolveAll(Type[] types) {
    TypeLiteral<?>[] result = new TypeLiteral<?>[types.length];
    for (int t = 0; t < types.length; t++) {
      result[t] = resolve(types[t]);
    }
    return ImmutableList.copyOf(result);
  }

  /**
   * Resolves known type parameters in {@code toResolve} and returns the result.
   */
  TypeLiteral<?> resolve(Type toResolve) {
    return TypeLiteral.get(resolveType(toResolve));
  }

  Type resolveType(Type toResolve) {
    // this implementation is made a little more complicated in an attempt to avoid object-creation
    while (true) {
      if (toResolve instanceof TypeVariable) {
        TypeVariable original = (TypeVariable) toResolve;
        toResolve = MoreTypes.resolveTypeVariable(type, rawType, original);
        if (toResolve == original) {
          return toResolve;
        }

      } else if (toResolve instanceof GenericArrayType) {
        GenericArrayType original = (GenericArrayType) toResolve;
        Type componentType = original.getGenericComponentType();
        Type newComponentType = resolveType(componentType);
        return componentType == newComponentType
            ? original
            : Types.arrayOf(newComponentType);

      } else if (toResolve instanceof ParameterizedType) {
        ParameterizedType original = (ParameterizedType) toResolve;
        Type ownerType = original.getOwnerType();
        Type newOwnerType = resolveType(ownerType);
        boolean changed = newOwnerType != ownerType;

        Type[] args = original.getActualTypeArguments();
        for (int t = 0, length = args.length; t < length; t++) {
          Type resolvedTypeArgument = resolveType(args[t]);
          if (resolvedTypeArgument != args[t]) {
            if (!changed) {
              args = args.clone();
              changed = true;
            }
            args[t] = resolvedTypeArgument;
          }
        }

        return changed
            ? Types.newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
            : original;

      } else if (toResolve instanceof WildcardType) {
        WildcardType original = (WildcardType) toResolve;
        Type[] originalLowerBound = original.getLowerBounds();
        Type[] originalUpperBound = original.getUpperBounds();

        if (originalLowerBound.length == 1) {
          Type lowerBound = resolveType(originalLowerBound[0]);
          if (lowerBound != originalLowerBound[0]) {
            return Types.supertypeOf(lowerBound);
          }
        } else if (originalUpperBound.length == 1) {
          Type upperBound = resolveType(originalUpperBound[0]);
          if (upperBound != originalUpperBound[0]) {
            return Types.subtypeOf(upperBound);
          }
        }
        return original;

      } else {
        return toResolve;
      }
    }
  }

  /**
   * Returns the generic form of {@code supertype}. For example, if this is {@code
   * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
   * Iterable.class}.
   *
   * @param supertype a superclass of, or interface implemented by, this.
   * @since 2.0
   */
  public TypeLiteral<?> getSupertype(Class<?> supertype) {
    checkArgument(supertype.isAssignableFrom(rawType),
        "%s is not a supertype of %s", supertype, this.type);
    return resolve(MoreTypes.getGenericSupertype(type, rawType, supertype));
  }

  /**
   * Returns the resolved generic type of {@code field}.
   *
   * @param field a field defined by this or any superclass.
   * @since 2.0
   */
  public TypeLiteral<?> getFieldType(Field field) {
    checkArgument(field.getDeclaringClass().isAssignableFrom(rawType),
        "%s is not defined by a supertype of %s", field, type);
    return resolve(field.getGenericType());
  }

  /**
   * Returns the resolved generic parameter types of {@code methodOrConstructor}.
   *
   * @param methodOrConstructor a method or constructor defined by this or any supertype.
   * @since 2.0
   */
  public List<TypeLiteral<?>> getParameterTypes(Member methodOrConstructor) {
    Type[] genericParameterTypes;

    if (methodOrConstructor instanceof Method) {
      Method method = (Method) methodOrConstructor;
      checkArgument(method.getDeclaringClass().isAssignableFrom(rawType),
          "%s is not defined by a supertype of %s", method, type);
      genericParameterTypes = method.getGenericParameterTypes();

    } else if (methodOrConstructor instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
      checkArgument(constructor.getDeclaringClass().isAssignableFrom(rawType),
          "%s does not construct a supertype of %s", constructor, type);
      genericParameterTypes = constructor.getGenericParameterTypes();

    } else {
      throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
    }

    return resolveAll(genericParameterTypes);
  }

  /**
   * Returns the resolved generic exception types thrown by {@code constructor}.
   *
   * @param methodOrConstructor a method or constructor defined by this or any supertype.
   * @since 2.0
   */
  public List<TypeLiteral<?>> getExceptionTypes(Member methodOrConstructor) {
    Type[] genericExceptionTypes;

    if (methodOrConstructor instanceof Method) {
      Method method = (Method) methodOrConstructor;
      checkArgument(method.getDeclaringClass().isAssignableFrom(rawType),
          "%s is not defined by a supertype of %s", method, type);
      genericExceptionTypes = method.getGenericExceptionTypes();

    } else if (methodOrConstructor instanceof Constructor) {
      Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
      checkArgument(constructor.getDeclaringClass().isAssignableFrom(rawType),
          "%s does not construct a supertype of %s", constructor, type);
      genericExceptionTypes = constructor.getGenericExceptionTypes();

    } else {
      throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
    }

    return resolveAll(genericExceptionTypes);
  }

  /**
   * Returns the resolved generic return type of {@code method}.
   *
   * @param method a method defined by this or any supertype.
   * @since 2.0
   */
  public TypeLiteral<?> getReturnType(Method method) {
    checkArgument(method.getDeclaringClass().isAssignableFrom(rawType),
        "%s is not defined by a supertype of %s", method, type);
    return resolve(method.getGenericReturnType());
  }

  
  
  
}
