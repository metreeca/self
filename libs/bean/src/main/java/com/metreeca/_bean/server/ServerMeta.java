/*
 * Copyright © 2013-2018 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Metreeca/Self. If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca._bean.server;

import com.metreeca._bean.shared.Bean;
import com.metreeca._bean.shared.Meta;
import com.metreeca._bean.shared.MetaException;

import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;


@SuppressWarnings("unchecked")
public final class ServerMeta<T> implements Meta<T> {

	private static final Collection<Class<?>> Supported=new HashSet<Class<?>>(Arrays.asList(
			SortedSet.class, Set.class, List.class, Collection.class,
			SortedMap.class, Map.class
	));


	public static Map<String, String> aliases() {
		try {

			final Map<String, String> aliases=new HashMap<String, String>();

			// !!! ;(idea) META-INF/com.metreeca._bean not on classpath while building artifacts…
			//aliases.put("com.metreeca.rover.shared.Report", "com.metreeca.self.shared.Report");
			//aliases.put("com.metreeca.tool.client.ports.StorePort$Memo", "com.metreeca._tool.client.ports.StorePort$Memo");

			final Enumeration<URL> maps=ClassLoader.getSystemResources("META-INF/com.metreeca._bean");

			while ( maps.hasMoreElements() ) {

				final URL map=maps.nextElement();
				final Properties properties=new Properties();

				properties.load(map.openStream());

				for (final Map.Entry<Object, Object> entry : properties.entrySet()) {

					final String alias=(String)entry.getKey();
					final String type=(String)entry.getValue();

					final String current=aliases.put(alias, type);

					if ( current != null && !current.equals(type) ) { // handle multiple copies of META-INF in path
						throw new IllegalStateException("conflicting alias for type "+alias+"["+map+"]");
					}
				}
			}

			return aliases;

		} catch ( final IOException unexpected ) {
			throw new IOError(unexpected);
		}
	}


	public static Factory factory() {
		return new Factory() {

			private final Map<String, Meta<?>> cache=new HashMap<String, Meta<?>>();

			{
				for (final Map.Entry<String, String> alias : aliases().entrySet()) {
					cache.put(alias.getKey(), meta(alias.getValue())); // prime the cache with aliased bens
				}
			}

			@Override public Meta<?> meta(final String type) throws MetaException {

				if ( type == null ) {
					throw new NullPointerException("null type");
				}

				try {

					final Meta<?> meta=cache.get(type);

					return meta != null ? meta : meta(Class.forName(type));

				} catch ( final ClassNotFoundException ignored ) {
					throw new MetaException("unknown bean type ["+type+"]");
				}
			}

			@Override public <T> Meta<T> meta(final Class<T> type) throws MetaException {

				if ( type == null ) {
					throw new NullPointerException("null type");
				}

				final String name=type.getName();

				Meta<?> meta=cache.get(name);

				if ( meta == null ) {
					cache.put(name, meta=new ServerMeta(type));
				}

				return (Meta<T>)meta;
			}

		};
	}


	private final Class<T> type;
	private final Map<String, Class<?>> key;
	private final Map<String, Class<?>> fields;

	private final Constructor<?> constructor; // preferred constructor (null for enumerated types)
	private final Constructor<?> fallback; // fallback no-args constructor (null if missing and for enumerated types)

	private final Map<String, Method> getters;
	private final Map<String, Method> setters;

	private final Method encoder;
	private final Method decoder;


	public ServerMeta(final Class<T> type) {

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		this.getters=getters(type);
		this.setters=setters(type);

		this.encoder=encoder(type);
		this.decoder=decoder(type);

		this.type=type;
		this.key=key(type, getters, setters);
		this.fields=fields(type, getters, setters);

		this.constructor=constructor(type, key.values());
		this.fallback=fallback(type);
	}


	@Override public Class<T> type() {
		return type;
	}

	@Override public Map<String, Class<?>> key() {
		return Collections.unmodifiableMap(key);
	}

	@Override public Map<String, Class<?>> fields() {
		return Collections.unmodifiableMap(fields);
	}


	@Override public T create() {
		return create(Collections.<String, Object>emptyMap());
	}

	@Override public T create(final Map<String, Object> values) {

		if ( values == null ) {
			throw new NullPointerException("null values");
		}

		if ( type.isEnum() ) {
			throw new MetaException("enumerated type ["+type.getName()+"]");
		}

		final Object[] args=new Object[key.size()];

		int index=0;

		for (final String arg : key.keySet()) {
			args[index++]=values.get(arg);
		}

		try {

			return (T)(fallback != null && values.isEmpty() ? fallback.newInstance() : constructor.newInstance(args));

		} catch ( final Exception e ) {
			throw new MetaException("failed to create bean instance ["+type.getName()+"]", e);
		}
	}


	@Override public Object get(final Object bean, final String field) {

		if ( field == null ) {
			throw new NullPointerException("null field");
		}

		final Method getter=getters.get(field);

		if ( getter == null ) {
			throw new IllegalArgumentException("unknown field ["+type(bean)+"."+field+"]");
		}

		try {
			return bean == null ? null : getter.invoke(bean);
		} catch ( final Exception e ) {
			throw new MetaException("failed to get bean field ["+type(bean)+"."+field+"]", e);
		}
	}

	@Override public void set(final Object bean, final String field, final Object value) {

		if ( field == null ) {
			throw new NullPointerException("null field");
		}

		final Method setter=setters.get(field);

		if ( setter == null ) {
			throw new IllegalArgumentException("unknown field ["+type(bean)+"."+field+"]");
		}

		try {
			if ( bean != null ) { setter.invoke(bean, value); }
		} catch ( final Exception e ) {
			throw new MetaException("failed to set bean field ["+type(bean)+"."+field+" = "+value+" ("+type(value)+")]", e);
		}
	}


	@Override public Bean.Info encode(final Bean.Info info) {

		if ( info == null ) {
			throw new NullPointerException("null info");
		}

		if ( encoder != null ) {
			try {

				encoder.invoke(null, info);

			} catch ( final Exception e ) {
				throw new MetaException("failed do encode info ["+type().getName()+"]", e);
			}
		}

		return info;
	}

	@Override public Bean.Info decode(final Bean.Info info) {

		if ( info == null ) {
			throw new NullPointerException("null info");
		}

		if ( decoder != null ) {
			try {

				decoder.invoke(null, info);

			} catch ( final Exception e ) {
				throw new MetaException("failed do decode info ["+type().getName()+"]", e);
			}
		}

		return info;
	}


	private Constructor<?> constructor(final Class<?> type, final Collection<Class<?>> signature) {
		try {

			return type.isEnum() ? null : type.getConstructor(signature.toArray(new Class<?>[signature.size()]));

		} catch ( final NoSuchMethodException ignored ) {
			throw new IllegalArgumentException("missing constructor ["+type.getName()+"("+signature+")]");
		}
	}

	private Constructor<?> fallback(final Class<?> type) {
		try {


			return type.isEnum() ? null : type.getConstructor();
		} catch ( final NoSuchMethodException ignored ) {
			return null;
		}
	}

	private Map<String, Method> getters(final Class<?> type) {

		final Map<String, Method> getters=new HashMap<String, Method>();

		for (final Method method : type.getMethods()) {

			final String name=get(method.getName());

			if ( name != null && method.getParameterTypes().length == 0 ) {
				getters.put(name, method);
			}
		}

		return getters;
	}

	private Map<String, Method> setters(final Class<?> type) {

		final Map<String, Method> setters=new HashMap<String, Method>();

		for (final Method method : type.getMethods()) { // list settable fields

			final String name=set(method.getName());

			if ( name != null && method.getParameterTypes().length == 1 ) {
				setters.put(name, method);
			}
		}

		for (final Map.Entry<String, Method> entry : getters(type).entrySet()) { // pair setters

			final String name=entry.getKey();
			final Class<?> value=entry.getValue().getReturnType();

			final Method setter=setter(type, "set"+Character.toUpperCase(name.charAt(0))+name.substring(1), value);

			if ( setter != null ) {
				setters.put(name, setter);
			}
		}

		return setters;
	}


	private Method setter(final Class<?> type, final String name, final Class<?> value) {

		try {

			final Method setter=type.getMethod(name, value);

			if ( Modifier.isPublic(setter.getModifiers()) ) {
				return setter;
			}

		} catch ( final NoSuchMethodException ignored ) {}

		if ( value.getSuperclass() != null ) {

			final Method setter=setter(type, name, value.getSuperclass());

			if ( setter != null ) {
				return setter;
			}
		}

		for (final Class<?> i : value.getInterfaces()) {

			final Method setter=setter(type, name, i);

			if ( setter != null ) {
				return setter;
			}

		}

		return null;
	}


	private Method encoder(final Class<T> type) {
		try {

			final Method encoder=type.getMethod("encode", Bean.Info.class);
			final int modifiers=encoder.getModifiers();

			return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) ? encoder : null;

		} catch ( final NoSuchMethodException ignored ) {
			return null;
		}
	}

	private Method decoder(final Class<T> type) {
		try {

			final Method decoder=type.getMethod("decode", Bean.Info.class);
			final int modifiers=decoder.getModifiers();

			return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) ? decoder : null;

		} catch ( final NoSuchMethodException ignored ) {
			return null;
		}
	}


	private Map<String, Class<?>> key(final Class<?> type,
			final Map<String, Method> getters, final Map<String, Method> setters) {

		final Bean bean=type.getAnnotation(Bean.class);

		if ( bean == null ) {
			throw new IllegalArgumentException("no @Bean annotation ["+type+"]");
		}

		if ( type.isEnum() && bean.value().length > 0 ) {
			throw new IllegalArgumentException("constructor args for enumerated type ["+type+"]");
		}

		final Map<String, Class<?>> key=new LinkedHashMap<String, Class<?>>();

		for (final String arg : bean.value()) {

			// key fields must be evaluated before creating the bean > read-only to prevent self references

			final Method getter=getters.get(arg);
			final Method setter=setters.get(arg);

			if ( getter == null ) {
				throw new IllegalArgumentException("unreadable key field ["+type.getName()+"."+arg+"]");
			}

			if ( setter != null ) {
				throw new IllegalArgumentException("writable key field ["+type.getName()+"."+arg+"]");
			}

			key.put(arg, getter.getReturnType());
		}

		return key;
	}

	private Map<String, Class<?>> fields(final Class<?> type,
			final Map<String, Method> getters, final Map<String, Method> setters) {

		final Map<String, Class<?>> fields=new HashMap<String, Class<?>>();

		for (final Map.Entry<String, Method> entry : getters.entrySet()) {

			final String name=entry.getKey();
			final Method getter=entry.getValue();
			final Method setter=setters.get(name);

			if ( setter != null ) {

				final Class<?> get=getter.getReturnType();
				final Class<?> set=setter.getParameterTypes()[0];

				if ( !set.isAssignableFrom(get) ) {
					throw new IllegalArgumentException(
							"unpaired accessors ["+type.getName()+"."+name+" = "+get.getName()+"/"+set.getName()+"]");
				}

				if ( Collection.class.isAssignableFrom(get) && !Supported.contains(get) ) {
					throw new UnsupportedOperationException(
							"unsupported collection type ["+type.getName()+"."+name+" = "+get.getName()+"]");
				}

				if ( Map.class.isAssignableFrom(get) && !Supported.contains(get) ) {
					throw new UnsupportedOperationException(
							"unsupported map type ["+type.getName()+"."+name+" = "+get.getName()+"]");
				}

				fields.put(name, get);
			}
		}

		return fields;
	}


	private String get(final String name) {
		return lower(name.startsWith("get") ? name.substring(3) : name.startsWith("is") ? name.substring(2) : null);
	}

	private String set(final String name) {
		return lower(name.startsWith("set") ? name.substring(3) : null);
	}

	private String lower(final String name) {
		return name != null && name.length() == 1 ||
				name != null && name.length() >= 2 && Character.isLowerCase(name.charAt(1))
				|| name != null && name.length() >= 3 && Character.isLowerCase(name.charAt(2))
				? Character.toLowerCase(name.charAt(0))+name.substring(1)
				: name;
	}


	private String type(final Object object) {
		return object == null ? null : object.getClass().getName();
	}
}
