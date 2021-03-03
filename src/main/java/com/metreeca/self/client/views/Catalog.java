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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca.self.shared.Item;
import com.metreeca.self.shared.forms.Items;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public class Catalog extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Catalog.css") TextResource skin();

	}


	private Class<? extends Item<?>> type;

	private Items items;


	private final Tile head;
	private final Tile list;
	private final Tile status;


	public Catalog() {
		root("<div/>")

				.skin(resources.skin().getText())

				.<Overlay>as().shading(true)

				.<Tile>as()

				.append(head=$("<header/>"))

				.append(list=$("<ul/>").is("placeholder striped", true))

				.append($("<footer/>")

						.append(status=$("<span/>"))

						.append($("<button/>").is("fa fa-close", true)

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { close(); }
								})))

				.bind(Items.class, new Action<Event>() {
					@Override public void execute(final Event e) { items(e.<Items>data()); }
				})

				.async(new Action<Event>() {
					@Override public void execute(final Event e) { render(); }
				});
	}


	public Catalog type(final Class<? extends Item<?>> type) {

		this.type=type;

		return this;
	}


	private Items items() {

		if ( items == null ) {
			root().fire(items=new Items());
		}

		return items;
	}

	private Catalog items(final Items items) {

		if ( items.equals(this.items) ) {
			this.items=items;
		}

		return render();
	}


	public Catalog open() {

		root().<Overlay>as().open().align(Align.Center, Align.Center);

		return render();
	}

	public Catalog close() {

		root().<Overlay>as().close().as();

		return this;
	}


	private Catalog render() {

		final Items items=items();

		if ( type != null && items.fulfilled() ) {

			list.clear().is("busy", false);

			final List<Item<?>> matches=filter(items.getItems());

			for (final Item<?> item : matches) {
				list.append($("<li/>")

						.append($("<button/>").text(item.getLabel())

								.click(new Action<Event>() {
									@Override public void execute(final Event e) {
										try { root().fire(item); } finally { close(); }
									}
								})));
			}

			final int size=matches.size();

			status.text((size > 1 ? size+" stored documents" : size == 1 ? "1 stored document" : "no stored documents").toLowerCase());

			root().<Overlay>as().align(Align.Center, Align.Center); // !!!

		} else {
			list.clear().is("busy", true);
			status.text("");
		}

		return this;
	}

	private List<Item<?>> filter(final Iterable<Item<?>> items) {

		final List<Item<?>> matches=new ArrayList<Item<?>>();

		for (final Item<?> item : items) {
			if ( hasType(item, type) && !item.getLabel().isEmpty() ) {
				matches.add(item);
			}
		}

		Collections.sort(matches, new Comparator<Item<?>>() {
			@Override public int compare(final Item<?> x, final Item<?> y) {
				return x.getLabel().compareToIgnoreCase(y.getLabel());
			}
		});

		return matches;
	}

	private boolean hasType(final Item<?> item, final Class<? extends Item<?>> type) {
		return item.getType().equals(type.getName());
	}
}
