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

package com.metreeca._bean.client;

import com.metreeca._bean.shared.Bean;
import com.metreeca._bean.shared.Meta;
import com.metreeca._bean.shared.MetaException;

import com.google.gwt.core.client.GWT;

import java.util.*;


@SuppressWarnings({"unchecked", "AbstractClassNeverImplemented"}) // implemented by MetaGenerator
public abstract class ClientMeta<T> implements Meta<T> {

	public static Meta.Factory factory() {
		return GWT.create(Meta.Factory.class);
	}


	private final Class<T> type;

	private final Map<String, Class<?>> key=new LinkedHashMap<String, Class<?>>();
	private final Map<String, Class<?>> fields=new LinkedHashMap<String, Class<?>>();


	private final Creator creator;
	private final Coder encoder;
	private final Coder decoder;

	private final Map<String, Getter> getters=new HashMap<String, Getter>();
	private final Map<String, Setter> setters=new HashMap<String, Setter>();


	protected ClientMeta(final Class<T> type, final Creator creator, final Coder encoder, final Coder decoder) {
		this.type=type;
		this.creator=creator;
		this.encoder=encoder;
		this.decoder=decoder;
	}


	protected final void key(final String field, final Class<?> type, final Getter getter) {
		key.put(field, type);
		getters.put(field, getter);
	}

	protected final void field(final String field, final Class<?> type, final Getter getter, final Setter setter) {
		fields.put(field, type);
		getters.put(field, getter);
		setters.put(field, setter);
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


	@Override public T create() throws MetaException {
		return create(Collections.<String, Object>emptyMap());
	}

	@Override public T create(final Map<String, Object> values) throws MetaException {

		if ( values == null ) {
			throw new NullPointerException("null values");
		}

		if ( creator == null ) {
			throw new MetaException("enumerated type ["+type.getName()+"]");
		}

		return (T)creator.create(values);
	}


	@Override public Object get(final Object bean, final String field) throws MetaException {

		if ( field == null ) {
			throw new NullPointerException("null field");
		}

		final Getter getter=getters.get(field);

		if ( getter == null ) {
			throw new IllegalArgumentException("unknown field ["+type(bean)+"."+field+"]");
		}

		try {
			return bean == null ? null : getter.get(bean);
		} catch ( final Exception e ) {
			throw new MetaException("failed to get bean field ["+type(bean)+"."+field+"]", e);
		}
	}

	@Override public void set(final Object bean, final String field, final Object value) throws MetaException {

		if ( field == null ) {
			throw new NullPointerException("null field");
		}

		final Setter setter=setters.get(field);

		if ( setter == null ) {
			throw new IllegalArgumentException("unknown field ["+type(bean)+"."+field+"]");
		}

		try {
			if ( bean != null ) { setter.set(bean, value); }
		} catch ( final Exception e ) {
			throw new MetaException("failed to set bean field ["+type(bean)+"."+field+" = "+value+" ("+type(value)+")]", e);
		}
	}


	@Override public Bean.Info encode(final Bean.Info info) {

		if ( encoder != null ) {
			encoder.code(info);
		}

		return info;
	}

	@Override public Bean.Info decode(final Bean.Info info) {

		if ( decoder != null ) {
			decoder.code(info);
		}

		return info;
	}


	private String type(final Object object) {
		return object == null ? null : object.getClass().getName();
	}


	abstract static class Factory implements Meta.Factory {

		private final Map<String, Meta<?>> metas=new HashMap<String, Meta<?>>();


		@Override public Meta<?> meta(final String type) throws MetaException {

			if ( type == null ) {
				throw new NullPointerException("null type");
			}

			final Meta<?> meta=metas.get(type);

			if ( meta == null ) {
				throw new MetaException("unknown bean type ["+type+"]");
			}

			return meta;
		}

		@Override public <T> Meta<T> meta(final Class<T> type) throws MetaException {

			if ( type == null ) {
				throw new NullPointerException("null type");
			}

			return (Meta<T>)meta(type.getName());
		}


		protected final void meta(final String type, final Meta<?> meta) {
			metas.put(type, meta);
		}

		protected final void alias(final String alias, final String meta) {
			metas.put(alias, metas.get(meta));
		}
	}


	@SuppressWarnings("InterfaceNeverImplemented") static interface Creator {

		public Object create(Map<String, Object> values);

	}

	@SuppressWarnings("InterfaceNeverImplemented") static interface Coder {

		public void code(final Bean.Info info);

	}

	@SuppressWarnings("InterfaceNeverImplemented") static interface Getter {

		public Object get(final Object bean);

	}

	@SuppressWarnings("InterfaceNeverImplemented") static interface Setter {

		public void set(final Object bean, final Object value);

	}
}
