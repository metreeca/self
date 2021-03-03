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

package com.metreeca.self.client.ports;

import com.metreeca._tile.client.*;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.shared.Cache;
import com.metreeca.self.shared.async.Handler;
import com.metreeca.self.shared.async.Promise;
import com.metreeca.self.shared.forms.Shape;
import com.metreeca.self.shared.sparql.*;


public final class ShapePort extends View {

	private static final int CacheSize=100; // maximum cache size {0 >> no limit}
	private static final int CacheTTL=15*60*1000; // cache time-to-live [ms] {0 >> no limit}


	private final Cache<Object, Promise<?>> cache=new Cache<>(CacheSize, CacheTTL); // shape fingerprint > shape entries

	private final Engine engine=new Engine();

	private final Client client=new Client() {

		@Override public Query evaluate(final Query query) {

			root().async(new Action<Event>() {
				@Override public void execute(final Event event) {
					root().<Bus>as().query(query);
				}
			});

			return query;

		}

	};


	public ShapePort() {
		root("<script/>")

				.bind(Shape.class, new Action<Event>() {
					@Override public void execute(final Event e) { shape(e.<Shape<?, ?>>data()); }
				});
	}


	//// Events ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private <E> void shape(final Shape<?, E> shape) {
		root().async(new Action<Event>() {
			@Override public void execute(final Event event) { // always async wrt to shape request > no race conditions

				entries(shape).then(new Handler<E>() {

					@Override public void value(final E entries) { shape.setEntries(entries).done(); }

					@Override public void error(final Exception error) { shape.error(error); }

				});

			}
		});
	}


	//// Caching ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private <E> Promise<E> entries(final Shape<?, E> shape) {

		final Object fingerprint=shape.fingerprint(); // !!! replace with canonical structural matching

		Promise<E> entries=(Promise<E>)cache.get(fingerprint);

		if ( entries == null ) { // cache immediately to catch similar request while processing
			cache.put(fingerprint, entries=shape.process(engine, client).then(new Handler<E>() {

				@Override public void error(final Exception error) {

					cache.remove(fingerprint); // if failed, remove from cache to enable future retries

				}

			}));
		}

		return entries;
	}

}
