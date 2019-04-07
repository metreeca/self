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

package com.metreeca._jeep.shared;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;


/**
 * LRU cache with size and time-to-live limits.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public final class Cache<K, V> {

	private final int size; // maximum cache size {0 >> no limit}
	private final int ttl; // cache time-to-live [ms] {0 >> no limit}

	private final Map<K, Entry<V>> entries=new LinkedHashMap<>(); // entries in insertion order


	public Cache() {
		this(0, 0);
	}

	public Cache(final int size, final int ttl) {

		if ( size < 0 ) {
			throw new IllegalArgumentException("illegal size ["+size+"]");
		}

		if ( ttl < 0 ) {
			throw new IllegalArgumentException("illegal ttl ["+ttl+"]");
		}

		this.size=size;
		this.ttl=ttl;
	}


	public V get(final K key) {

		final Entry<V> entry=entries.remove(key);

		if ( entry != null && entry.live(ttl) ) {

			entries.put(key, entry); // re-insert as most recently used entry

			return entry.value;

		} else {
			return null;
		}
	}

	public void put(final K key, final V value) {

		entries.remove(key);
		entries.put(key, new Entry<>(value)); // re-create as most recently used entry

		if ( size > 0 && entries.size() > size ) { // remove the least recently used entry

			final Iterator<Map.Entry<K, Entry<V>>> iterator=entries.entrySet().iterator();

			iterator.next();
			iterator.remove();
		}
	}

	public void remove(final K key) {
		entries.remove(key);
	}


	private static final class Entry<V> {

		private final V value;
		private final long created;


		public Entry(final V value) {
			this.value=value;
			this.created=currentTimeMillis();
		}

		public boolean live(final int ttl) {
			return ttl == 0 || created+ttl >= currentTimeMillis();
		}
	}
}
