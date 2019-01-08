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

package com.metreeca._tool.shared.forms;

import com.metreeca._jeep.shared.async.*;


/**
 * Data transfer object.
 *
 * @param <T> the self-bounded type
 */
public abstract class Form<T extends Form<T>> implements Promise<T> {

	public static <F extends Form<F>> Promise<F> promise(final Form<F> form) {

		if ( form == null ) {
			throw new NullPointerException("null form");
		}

		return form.promise;
	}


	private final Promise<T> promise=Promises.promise();


	protected abstract T self();


	public T done() {

		promise.value(self());

		return self();
	}


	@Override public boolean fulfilled() {
		return promise.fulfilled();
	}

	@Override public boolean rejected() {
		return promise.rejected();
	}


	@Override public T value(final T value) {

		if ( value == null ) {
			throw new NullPointerException("null value");
		}

		promise.value(value);

		return self();
	}

	@Override public T error(final Exception error) {

		if ( error == null ) {
			throw new NullPointerException("null error");
		}

		promise.error(error);

		return self();
	}


	@Override public T then(final Handler<T> handler) {

		if ( handler == null ) {
			throw new NullPointerException("null handler");
		}

		promise.then(handler);

		return self();
	}

	@Override public T then(final Promise<T> promise) {

		if ( promise == null ) {
			throw new NullPointerException("null promise");
		}

		this.promise.then(promise);

		return self();
	}


	@Override public <R> Promise<R> pipe(final Morpher<T, R> morpher) {

		if ( morpher == null ) {
			throw new NullPointerException("null morpher");
		}

		return promise.pipe(morpher);
	}

}
