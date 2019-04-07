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

package com.metreeca._tool.client.forms;


import com.metreeca._tool.shared.forms.Form;


public final class Import<T> extends Form<Import<T>> {

	private final T target;

	private final String type;
	private final String data;


	public Import(final T target, final String type, final String data) {

		if ( target == null ) {
			throw new NullPointerException("null target");
		}

		if ( type == null ) {
			throw new NullPointerException("null mime");
		}

		if ( data == null ) {
			throw new NullPointerException("null data");
		}

		this.target=target;

		this.type=type;
		this.data=data;
	}


	@Override protected Import<T> self() {
		return this;
	}


	public T target() {
		return target;
	}


	public String type() {
		return type;
	}

	public boolean type(final String... types) { // !!! factor with Export

		if ( types == null ) {
			throw new NullPointerException("null types");
		}

		for (final String type : types) {

			if ( type == null ) {
				throw new NullPointerException("null type");
			}

			if ( this.type().equalsIgnoreCase(type) ) {
				return true;
			}
		}

		return false;
	}


	public String data() {
		return data;
	}
}
