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

package com.metreeca._jeep.shared.async;

public interface Promise<V> {

	public boolean fulfilled();

	public boolean rejected();


	public Promise<V> value(final V value);

	public Promise<V> error(final Exception error);


	public Promise<V> then(final Handler<V> handler);

	public Promise<V> then(final Promise<V> promise);


	public <R> Promise<R> pipe(Morpher<V, R> morpher);

}
