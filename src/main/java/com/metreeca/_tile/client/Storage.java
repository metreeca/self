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

package com.metreeca._tile.client;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.*;


final class Storage extends JavaScriptObject {

	@SuppressWarnings("ProtectedMemberInFinalClass") protected Storage() {}


	public static native Storage create(final boolean persistent)  /*-{
		return persistent ? $wnd.localStorage : $wnd.sessionStorage; // !!! ;(ff) fix for dev mode ( return value always interpreted as localStorage)
	}-*/;


	public Map<String, String> map() {
		return new $Map(this);
	}


	public native int size() /*-{
		return this.length;
	}-*/;

	public native String key(final int index) /*-{
		return this.key(index);
	}-*/;

	public native String get(final String key) /*-{
		return this.getItem(key);
	}-*/;

	public native String insert(final String key, final String value) /*-{
		try { return this.getItem(key || ""); } finally { this.setItem(key || "", value || ""); }
	}-*/;

	public native String remove(final String key) /*-{
		try { return this.getItem(key || ""); } finally { this.removeItem(key || ""); }
	}-*/;

	public native Storage clear() /*-{

		this.clear();

		return this;
	}-*/;


	public static final class $Map extends AbstractMap<String, String> {

		private final Storage storage;


		public $Map(final Storage storage) {
			this.storage=storage;
		}


		@Override public Set<Entry<String, String>> entrySet() {
			return new $Set(storage);
		}

		@Override public String put(final String key, final String value) {
			return storage.insert(key, value);
		}

		@Override public void clear() {
			storage.clear();
		}
	}

	private static final class $Set extends AbstractSet<Map.Entry<String, String>> {

		private final Storage storage;


		public $Set(final Storage storage) {
			this.storage=storage;
		}


		@Override public int size() {
			return storage.size();
		}

		@Override public Iterator iterator() {
			return new $Iterator(storage);
		}
	}

	private static final class $Iterator implements Iterator<Map.Entry<String, String>> {

		private final Storage storage;

		private int last;
		private int next;


		public $Iterator(final Storage storage) {
			this.storage=storage;
		}


		@Override public boolean hasNext() {
			return next < storage.size();
		}

		@Override public Map.Entry<String, String> next() {
			if ( hasNext() ) {

				final String key=storage.key(next);
				final String value=storage.get(key);

				try {
					return new $Entry(storage, key, value);
				} finally {
					last=next++;
				}

			} else {
				throw new NoSuchElementException();
			}
		}

		@Override public void remove() {
			if ( last < next ) {

				final String key=storage.key(last);

				try {
					storage.remove(key);
				} finally {
					last=next;
				}

			} else {
				throw new IllegalStateException();
			}
		}
	}

	private static final class $Entry implements Map.Entry<String, String> {

		private final Storage storage;

		private final String key;
		private final String value;


		public $Entry(final Storage storage, final String key, final String value) {

			this.storage=storage;

			this.key=key;
			this.value=value;
		}

		@Override public String getKey() {
			return key;
		}

		@Override public String getValue() {
			return value;
		}

		@Override public String setValue(final String value) {
			return storage.insert(key, value);
		}
	}
}

