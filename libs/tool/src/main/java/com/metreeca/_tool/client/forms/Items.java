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

package com.metreeca._tool.client.forms;

import com.metreeca._tool.shared.Item;
import com.metreeca._tool.shared.forms.Form;

import java.util.*;


public final class Items extends Form<Items> {

	private final Map<String, Item<?>> items=new LinkedHashMap<String, Item<?>>(); // uuid > item


	@Override protected Items self() {
		return this;
	}


	public boolean isEmpty() {
		return items.isEmpty();
	}


	public Collection<Item<?>> getItems() {
		return Collections.unmodifiableCollection(items.values());
	}

	public Items insertItem(final Item<?> item) {

		if ( item == null ) {
			throw new NullPointerException("null item");
		}

		items.put(item.getUUID(), item);

		return this;
	}

	public Items removeItem(final Item<?> item) {

		if ( item == null ) {
			throw new NullPointerException("null item");
		}

		items.remove(item.getUUID());

		return this;
	}
}
