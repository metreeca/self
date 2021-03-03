/*
 * Copyright © 2013-2021 Metreeca srl. All rights reserved.
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

import java.util.Map;


public interface Meta<T> {

	public Class<T> type();

	public Map<String, Class<?>> key();

	public Map<String, Class<?>> fields();


	public T create() throws MetaException;

	public T create(Map<String, Object> values) throws MetaException;


	public Object get(final Object bean, final String field) throws MetaException;

	public void set(final Object bean, final String field, final Object value) throws MetaException;


	public Bean.Info encode(final Bean.Info info); // !!! remove

	public Bean.Info decode(final Bean.Info info); // !!! remove


	public static interface Factory {

		public Meta<?> meta(final String type) throws MetaException;

		public <T> Meta<T> meta(final Class<T> type) throws MetaException;

	}

}
