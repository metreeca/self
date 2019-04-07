/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca/Self.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca._bean.shared;

import com.metreeca._bean.shared.Bean.Info;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


@SuppressWarnings("OverlyComplexMethod") final class InfoDecoder {

	private final Meta.Factory factory;

	private final Map<Info, Object> beans=new HashMap<Info, Object>();


	public InfoDecoder(final Meta.Factory meta) {

		if ( meta == null ) {
			throw new NullPointerException("null meta factory");
		}

		this.factory=meta;
	}


	@SuppressWarnings("unchecked")
	public <T> T decode(final Info info) {

		if ( info == null ) {
			throw new NullPointerException("null info");
		}

		try {

			return (T)bean(info);

		} finally {
			beans.clear();
		}
	}


	private Object object(final Object object) {
		return object instanceof Info ? bean((Info)object)

				: object instanceof List ? collection((Iterable<?>)object, new ArrayList<Object>())
				: object instanceof SortedSet ? collection((Iterable<?>)object, new TreeSet<Object>())
				: object instanceof Set ? collection((Iterable<?>)object, new LinkedHashSet<Object>())
				: object instanceof Collection ? collection((Iterable<?>)object, new ArrayList<Object>())

				: object instanceof SortedMap ? map((Map<?, ?>)object, new TreeMap<Object, Object>())
				: object instanceof Map ? map((Map<?, ?>)object, new LinkedHashMap<Object, Object>())

				: object;
	}


	private Collection<Object> collection(final Iterable<?> items, final Collection<Object> collection) {

		for (final Object item : items) {
			collection.add(object(item));
		}

		return collection;
	}

	private Map<Object, Object> map(final Map<?, ?> entries, final Map<Object, Object> map) {

		for (final Map.Entry<?, ?> entry : entries.entrySet()) {
			map.put(object(entry.getKey()), object(entry.getValue()));
		}

		return map;
	}


	private Object bean(final Info info) {
		if ( beans.containsKey(info) ) {

			return beans.get(info);

		} else {

			final Meta<?> meta=factory.meta(info.type());

			// upgrade bean info

			meta.decode(info);

			if ( info.mode().equals("") ) { // regular bean

				// compute key fields and create bean (no self-references allowed)

				final Map<String, Object> args=new HashMap<String, Object>();

				for (final Map.Entry<String, Class<?>> entry : meta.key().entrySet()) {

					final String field=entry.getKey();
					final Class<?> type=entry.getValue();

					final Object value=object(info.field(field)); // !!! type exceptions

					args.put(field, fallback(value, type));
				}

				final Object bean=meta.create(args);

				// map info to bean

				beans.put(info, bean);

				// compute and set regular fields (self-references allowed)

				for (final String field : meta.fields().keySet()) {

					final Object value=object(info.field(field)); // !!! type exceptions

					if ( value != null ) {
						meta.set(bean, field, value);
					}
				}

				return bean;

			} else if ( info.mode().equals("class") ) { // class literal

				return meta.type();

			} else { // enum value

				return Enum.valueOf((Class<Enum>)meta.type(), info.mode());

			}
		}
	}


	private Object fallback(final Object value, final Class<?> type) {
		return value != null ? value

				: type == boolean.class || type == Boolean.class ? false
				: type == char.class || type == Character.class ? '\0'
				: type == byte.class || type == Byte.class ? (byte)0
				: type == short.class || type == Short.class ? (short)0
				: type == int.class || type == Integer.class ? 0
				: type == long.class || type == Long.class ? 0L
				: type == float.class || type == Float.class ? 0.0F
				: type == double.class || type == Double.class ? 0

				: type == BigInteger.class ? BigInteger.ZERO
				: type == BigDecimal.class ? BigDecimal.ZERO
				: type == String.class ? ""
				: type == Date.class ? new Date(0)

				: type == Collection.class ? Collections.emptyList()
				: type == List.class ? Collections.emptyList()
				: type == Set.class ? Collections.emptySet()
				: type == SortedSet.class ? new TreeSet<Object>() // !!! review

				: type == Map.class ? Collections.emptyMap()
				: type == SortedMap.class ? new TreeMap<Object, Object>() // !!! review

				: null;
	}
}
