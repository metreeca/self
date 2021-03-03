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

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.Item;


/**
 * Fired to request the state of an item to be stored into the browser history.
 */
public final class State {

	private final Item<?> item;
	private final boolean push;


	public State(final Item<?> item) {
		this(item, false);
	}

	public State(final Item<?> item, final boolean push) {

		if ( item == null ) {
			throw new NullPointerException("null item");
		}

		this.item=item;
		this.push=push;
	}


	public Item<?> item() {
		return item;
	}

	public boolean push() {
		return push;
	}

}
