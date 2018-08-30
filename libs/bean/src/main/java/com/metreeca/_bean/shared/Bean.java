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

import java.lang.annotation.*;
import java.util.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {

	public String[] value() default {};


	public static final class Info implements Iterable<Info> {

		private int version;

		private String type=Object.class.getName();
		private String mode=""; // empty string for regular beans, "class" for type literals, name for enum values

		private final Map<String, Object> fields=new LinkedHashMap<String, Object>();


		@Override public Iterator<Info> iterator() {
			return visit().iterator();
		}


		public int version() {
			return version;
		}

		public Info version(final int version) {

			if ( version < 0 ) {
				throw new IllegalArgumentException("illegal version number ["+version+"]");
			}

			this.version=version;

			return this;
		}


		public String type() {
			return type;
		}

		public Info type(final String type) {

			if ( type == null ) {
				throw new NullPointerException("null type");
			}

			this.type=type;

			return this;
		}


		public String mode() {
			return mode;
		}

		public Info mode(final String mode) {

			if ( mode == null ) {
				throw new NullPointerException("null mode");
			}

			this.mode=mode;

			return this;
		}


		public Map<String, Object> fields() {
			return Collections.unmodifiableMap(fields);
		}

		public Object field(final String name) {

			if ( name == null ) {
				throw new NullPointerException("null name");
			}

			return fields.get(name);
		}

		public Info field(final String name, final Object value) {

			if ( name == null ) {
				throw new NullPointerException("null name");
			}

			if ( value == null ) {
				fields.remove(name);
			} else {
				fields.put(name, value);
			}

			return this;
		}


		public String string(final String name) {

			final Object value=field(name);

			return value instanceof String ? (String)value : null;
		}


		public Set<Info> visit() {

			final Set<Info> infos=new LinkedHashSet<Info>();

			final Queue<Object> queue=new LinkedList<Object>(Collections.singleton(this));

			while ( !queue.isEmpty() ) {

				final Object object=queue.remove();

				if ( object instanceof Info && infos.add((Info)object) ) {

					for (final Object value : ((Info)object).fields.values()) {
						queue.add(value);
					}

				} else if ( object instanceof Collection ) {

					for (final Object item : (Collection<?>)object) {
						queue.add(item);
					}

				} else if ( object instanceof Map ) {

					for (final Map.Entry<?, ?> entry : ((Map<?, ?>)object).entrySet()) {
						queue.add(entry.getKey());
						queue.add(entry.getValue());
					}

				}
			}

			return infos;
		}
	}

}
