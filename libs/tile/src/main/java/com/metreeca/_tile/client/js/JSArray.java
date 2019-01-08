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

package com.metreeca._tile.client.js;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Iterator;
import java.util.NoSuchElementException;


public abstract class JSArray<T> extends JavaScriptObject implements Iterable<T> { // !!! refactor

	public static native <T> JSArray<T> array() /*-{ return []; }-*/;

	public static native <T> JSArray<T> array(final String json) /*-{ return $wnd.JSON.parse(json); }-*/;


	protected JSArray() {}


	@Override public final Iterator<T> iterator() {
		return new Iterator<T>() {

			private int next;

			@Override public boolean hasNext() {
				return next < length();
			}

			@Override public T next() {

				if ( next >= length() ) {
					throw new NoSuchElementException("no more elements");
				}

				return get(next++);
			}

			@Override public void remove() {
				throw new UnsupportedOperationException("unsupported item removal");
			}

		};
	}


	public final native int length() /*-{

		return this.length;

	}-*/;


	public final native T get(final int index) /*-{

		return this[index];

	}-*/;


	public final native JSArray<T> push(final boolean element) /*-{

		this.push(element);

		return this;

	}-*/;

	public final native JSArray<T> push(final float element) /*-{

		this.push(element);

		return this;

	}-*/;

	public final native JSArray<T> push(final T element) /*-{

		this.push(element);

		return this;

	}-*/;

}
