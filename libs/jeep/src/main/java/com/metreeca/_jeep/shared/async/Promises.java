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

package com.metreeca._jeep.shared.async;

import java.util.*;

import static java.util.Arrays.asList;


public final class Promises {

	private Promises() {}


	//// Factories /////////////////////////////////////////////////////////////////////////////////////////////////////

	public static <V> Promise<V> promise() {
		return new PromiseBase<V>() {};
	}

	public static <V> Promise<V> promise(final V value) {
		return value != null ? Promises.<V>promise().value(value) : Promises.<V>promise();
	}

	public static <V> Promise<V> promise(final Exception error) {
		return error != null ? Promises.<V>promise().error(error) : Promises.<V>promise();
	}


	//// Combinators ///////////////////////////////////////////////////////////////////////////////////////////////////

	@SafeVarargs public static <V> Promise<List<V>> all(final Promise<V>... promises) { return all(asList(promises)); }

	@SafeVarargs public static <V> Promise<List<V>> any(final Promise<V>... promises) { return any(asList(promises)); }

	@SafeVarargs public static <V> Promise<List<V>> one(final Promise<V>... promises) { return one(asList(promises)); }


	public static <V> Promise<List<V>> all(final Iterable<Promise<V>> promises) {

		if ( promises == null ) {
			throw new NullPointerException("null promises");
		}

		final Promise<List<V>> all=promise();

		final List<V> values=new ArrayList<>();

		for (final Iterator<Promise<V>> iterator=promises.iterator(); iterator.hasNext(); ) {

			final Promise<V> promise=iterator.next();

			if ( promise == null ) {
				throw new NullPointerException("null promise");
			}

			final int index=values.size();

			values.add(null); // add a placeholder value to compute indexes

			promise.then(new Handler<V>() {

				@Override public void value(final V v) {
					if ( !values.contains(values.set(index, v)) && !iterator.hasNext() ) { all.value(values); }
				}

				@Override public void error(final Exception error) {
					all.error(error);
				}

			});
		}

		return all;
	}

	public static <V> Promise<List<V>> any(final List<Promise<V>> promises) {
		throw new UnsupportedOperationException("to be implemented"); // !!! tbi
	}

	public static <V> Promise<List<V>> one(final List<Promise<V>> promises) {
		throw new UnsupportedOperationException("to be implemented"); // !!! tbi
	}


	@SafeVarargs public static <K, V> Promise<Map<K, V>> maps(final Promise<Map<K, V>>... promises) {
		return maps(asList(promises));
	}

	public static <K, V> Promise<Map<K, V>> maps(final Iterable<Promise<Map<K, V>>> promises) {
		return all(promises).pipe(new Morpher<List<Map<K, V>>, Map<K, V>>() {
			@Override public Promise<Map<K, V>> value(final List<Map<K, V>> maps) {

				final Map<K, V> merged=new LinkedHashMap<>();

				for (final Map<K, V> map : maps) { merged.putAll(map); }

				return promise(merged);

			}
		});
	}


	@SafeVarargs public static <V> Promise<List<V>> lists(final Promise<List<V>>... promises) {
		return lists(asList(promises));
	}

	public static <V> Promise<List<V>> lists(final Iterable<Promise<List<V>>> promises) {
		return all(promises).pipe(new Morpher<List<List<V>>, List<V>>() {
			@Override public Promise<List<V>> value(final List<List<V>> lists) {

				final List<V> merged=new ArrayList<>();

				for (final List<V> list : lists) { merged.addAll(list); }

				return promise(merged);

			}
		});
	}

}
