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

package com.metreeca._tile.client.plugins;

import com.metreeca._tile.client.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Combo.Resources.resources;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public final class Combo extends Plugin {


	public static interface Resources extends ClientBundle {

		@Source("Combo.css") TextResource skin();

		public static final Resources resources=GWT.create(Resources.class); // !!! ;(gwt) broken static in overlay types

	}


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Combo() {}


	public Combo wire(final Menu menu) {

		if ( menu == null ) {
			throw new NullPointerException("null menu");
		}

		return this.<Tile>as().each(new Lambda<Tile>() {

			@Override public Tile apply(final Tile tile) {

				$("<span/>")

						.skin(resources.skin().getText())

						.wrap(tile

								.bind("blur", new Action<Event>() {
									@Override public void execute(final Event e) { close(menu, tile); }
								})

								.bind("keydown", new Action<Event>() { // return won't blur input
									@Override public void execute(final Event e) {
										if ( e.key("return") ) { close(menu, tile); }
									}
								}))

						.append($("<button/>")

								.attribute("type", "button")
								.attribute("tabindex", "-1")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { open(menu, tile); }
								}));

				return tile;

			}

		}).as();
	}


	private boolean open(final Menu menu) {
		return $(menu).parent().size() > 0;
	}

	private void open(final Menu menu, final Tile tile) {
		if ( !open(menu) ) {
			menu.open(tile).align(tile, Align.Fill, Align.After);
			tile.focus();
		}
	}

	private void close(final Menu menu, final Tile tile) {
		if ( open(menu) ) {
			menu.close(tile);
			tile.blur();
		}
	}

}
