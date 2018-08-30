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

package com.metreeca._bean.shared;

import com.metreeca._bean.shared.Bean.Info;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;


@SuppressWarnings("OverlyComplexMethod") final class InfoEncoder {

	private final Meta.Factory factory;

	private final Map<Object, Info> infos=new IdentityHashMap<Object, Info>();


	public InfoEncoder(final Meta.Factory meta) {

		if ( meta == null ) {
			throw new NullPointerException("null meta factory");
		}

		this.factory=meta;
	}


	public Info encode(final Object object) {

		if ( object == null ) {
			throw new NullPointerException("null object");
		}

		try {

			return bean(object);

		} finally {
			infos.clear();
		}
	}


	private Object object(final Object object) {
		return object == null ? null

				: object instanceof Boolean ? object
				: object instanceof Character ? object
				: object instanceof Byte ? object
				: object instanceof Short ? object
				: object instanceof Integer ? object
				: object instanceof Long ? object
				: object instanceof Float ? object
				: object instanceof Double ? object

				: object instanceof BigInteger ? object
				: object instanceof BigDecimal ? object
				: object instanceof String ? object
				: object instanceof Date ? object

				: object instanceof List ? collection((Iterable<?>)object, new ArrayList<Object>())
				: object instanceof SortedSet ? collection((Iterable<?>)object, new TreeSet<Object>())
				: object instanceof Set ? collection((Iterable<?>)object, new LinkedHashSet<Object>())
				: object instanceof Collection ? collection((Iterable<?>)object, new ArrayList<Object>())

				: object instanceof SortedMap ? map((Map<?, ?>)object, new TreeMap<Object, Object>())
				: object instanceof Map ? map((Map<?, ?>)object, new LinkedHashMap<Object, Object>())

				: bean(object);
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


	private Info bean(final Object bean) {
		if ( infos.containsKey(bean) ) {

			return infos.get(bean);

		} else if ( bean instanceof Class ) {

			final Meta<?> meta=factory.meta((Class<?>)bean);

			return new Info().type(meta.type().getName()).mode("class");

		} else if ( bean instanceof Enum ) {

			final Meta<?> meta=factory.meta(((Enum<?>)bean).getDeclaringClass());

			return new Info().type(meta.type().getName()).mode(((Enum<?>)bean).name());

		} else {

			final Meta<?> meta=factory.meta(bean.getClass());
			final Info info=new Info().type(meta.type().getName());

			infos.put(bean, info);

			for (final Map.Entry<String, Class<?>> entry : meta.key().entrySet()) {

				final String field=entry.getKey();
				final Class<?> type=entry.getValue();
				final Object value=meta.get(bean, field);

				info.field(field, object(type.equals(Boolean.class) ? value : optional(value)));
			}

			for (final Map.Entry<String, Class<?>> entry : meta.fields().entrySet()) {

				final String field=entry.getKey();
				final Class<?> type=entry.getValue();
				final Object value=meta.get(bean, field);

				info.field(field, object(type.equals(Boolean.class) ? value : optional(value)));
			}

			return meta.encode(info);
		}
	}


	private Object optional(final Object value) {
		return value == null ? null

				: value instanceof Boolean && !((Boolean)value) ? null
				: value instanceof Character && (Character)value == '\0' ? null
				: value instanceof Byte && (Byte)value == 0 ? null
				: value instanceof Short && (Short)value == 0 ? null
				: value instanceof Integer && (Integer)value == 0 ? null
				: value instanceof Long && (Long)value == 0 ? null
				: value instanceof Float && (Float)value == 0 ? null
				: value instanceof Double && (Double)value == 0 ? null

				: value instanceof BigInteger && value.equals(BigInteger.ZERO) ? null
				: value instanceof BigDecimal && value.equals(BigDecimal.ZERO) ? null
				: value instanceof String && ((String)value).isEmpty() ? null
				: value instanceof Date && ((Date)value).getTime() == 0 ? null

				: value instanceof Collection && ((Collection<?>)value).isEmpty() ? null
				: value instanceof Map && ((Map<?, ?>)value).isEmpty() ? null

				: value;
	}
}
