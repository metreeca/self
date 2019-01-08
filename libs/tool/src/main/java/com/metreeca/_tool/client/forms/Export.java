/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca._tool.client.forms;


import com.metreeca._tool.shared.forms.Form;


public final class Export<T> extends Form<Export<T>> {

	private final T source;

	private final String type;


	public Export(final T source, final String type) {

		if ( source == null ) {
			throw new NullPointerException("null source");
		}

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		this.source=source;

		this.type=type;
	}


	@Override protected Export<T> self() {
		return this;
	}


	public T source() {
		return source;
	}


	public String type() {
		return type;
	}

	public boolean type(final String... types) { // !!! factor with Import

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
}
