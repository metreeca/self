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

package com.metreeca._tile.client.plugins;

import com.metreeca._tile.client.*;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.plugins.Menu.Resources.resources;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public final class Menu extends Plugin {

	public static interface Resources extends ClientBundle {

		@Source("Menu.css") public TextResource skin();


		public static final Resources resources=GWT.create(Resources.class); // !!! ;(gwt) broken static in overlay types

		public static final Action<Event> Click=new Action<Event>() {
			@Override public void execute(final Event e) {

				final Tile target=e.target();
				final Tile current=e.current();

				if ( !target.enabled() ) { // ignore events on disabled elements

					e.stop();

				} else if ( target.<String>property("tagName").equalsIgnoreCase("menu") ) {

					if ( target.is("expanded") ) {
						current.tree().show();
						target.is("expanded", false);
					} else {
						current.tree().hide();
						target.path().show();
						target.tree().show();
						target.is("expanded", true);
					}

				} else if ( !e.targeting() ) { // ignore events on dividers/scrollbars/...

					current.<Menu>as().close();

				}
			}
		};
	}


	@SuppressWarnings("ProtectedMemberInFinalClass") protected Menu() {}


	public Menu open() {
		return open(null);
	}

	public Menu open(final Tile master) {
		return this.<Tile>as()

				.skin(resources.skin().getText()).is("sharp", true) // sharp overlay corners

				.bind("click", Resources.Click, true) // capturing > close before executing command

				.<Overlay>as()

				.modal(true)
				.open(master)

				.as();
	}


	public Menu close() {
		return close(null);
	}

	public Menu close(final Tile master) {
		return this.<Tile>as()

				.drop("click", Resources.Click, true)

				.delay(200, new Action<Event>() {
					@Override public void execute(final Event e) { // after a grace time to complete command processing // ;(ie) >> 100 ms
						e.current().<Overlay>as().close(false, master);
					}
				})

				.as();
	}


	public Menu align(final Tile master, final Align horizontal, final Align vertical) {
		return this.<Overlay>as().align(master, horizontal, vertical).as();
	}

	public Menu align(final float x, final float y) {
		return this.<Overlay>as().align(x, y).as();
	}

}
