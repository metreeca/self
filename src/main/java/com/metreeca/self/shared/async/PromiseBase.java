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

package com.metreeca.self.shared.async;

import java.util.Collection;
import java.util.LinkedHashSet;

import static com.metreeca.self.shared.async.Promises.promise;


public abstract class PromiseBase<V> implements Promise<V> {

	private V value;
	private Exception error;

	private Collection<Handler<V>> handlers;


	@Override public boolean fulfilled() {
		return value != null;
	}

	@Override public boolean rejected() {
		return error != null;
	}


	@Override public Promise<V> value(final V value) {

		if ( value == null ) {
			throw new NullPointerException("null value");
		}

		if ( this.value != null || this.error != null ) {
			throw new IllegalStateException("settled promise");
		}

		this.value=value;

		if ( handlers != null ) {
			try {
				for (final Handler<V> handler : handlers) {
					try { handler.value(value); } catch ( final Exception ignored ) {}
				}
			} finally {
				handlers=null;
			}
		}

		return this;
	}

	@Override public Promise<V> error(final Exception error) {

		if ( error == null ) {
			throw new NullPointerException("null error");
		}

		if ( this.value != null || this.error != null ) {
			throw new IllegalStateException("settled promise");
		}

		this.error=error;

		if ( handlers != null ) {
			try {
				for (final Handler<V> handler : handlers) {
					try { handler.error(error); } catch ( final Exception ignored ) {}
				}
			} finally {
				handlers=null;
			}
		}

		return this;
	}


	@Override public Promise<V> then(final Handler<V> handler) {

		if ( handler == null ) {
			throw new NullPointerException("null handler");
		}

		if ( value != null ) {

			try { handler.value(value); } catch ( final Exception ignored ) {}

		} else if ( error != null ) {

			try { handler.error(error); } catch ( final Exception ignored ) {}

		} else {

			if ( handlers == null ) {
				handlers=new LinkedHashSet<>(); // unique handlers in insertion order
			}

			handlers.add(handler);

		}

		return this;
	}

	@Override public Promise<V> then(final Promise<V> promise) {

		if ( promise == null ) {
			throw new NullPointerException("null promise");
		}

		return then(new Handler<V>() {

			@Override public void value(final V value) { promise.value(value); }

			@Override public void error(final Exception error) { promise.error(error); }

		});
	}


	@Override public <R> Promise<R> pipe(final Morpher<V, R> morpher) {

		if ( morpher == null ) {
			throw new NullPointerException("null morpher");
		}

		if ( value != null ) {

			try {

				return guard(morpher.value(value));

			} catch ( final Exception e ) {
				return promise(e);
			}

		} else if ( error != null ) {

			try {

				return guard(morpher.error(error));

			} catch ( final Exception e ) {
				return promise(e);
			}

		} else {

			final Promise<R> pipe=promise();

			then(new Handler<V>() {

				@Override public void value(final V value) {

					Promise<R> promise=null;

					try {
						promise=morpher.value(value);
					} catch ( final Exception e ) {
						pipe.error(e);
					}

					if ( promise != null ) {
						promise.then(pipe);
					}

				}

				@Override public void error(final Exception error) {

					Promise<R> promise=null;

					try {
						promise=morpher.error(error);
					} catch ( final Exception e ) {
						pipe.error(e);
					}

					if ( promise != null ) {
						promise.then(pipe);
					}

				}

			});

			return pipe;

		}
	}


	private <R> Promise<R> guard(final Promise<R> promise) {
		return promise != null ? promise : Promises.<R>promise();
	}

}
