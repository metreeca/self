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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Menu;
import com.metreeca._tile.client.plugins.Protocol;
import com.metreeca.self.client.Self.Bus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public final class Logo extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Logo.css") TextResource skin();

	}


	public Logo() {
		root("<button/>").is("small metreeca", true)

				.skin(resources.skin().getText())

				.text(Tile.NBSP) /* preserve vertical alignment */

				.click(new Action<Event>() {
					@Override public void execute(final Event e) { about(); }
				})

				.hold(new Action<Event>() { // hidden tools
					@Override public void execute(final Event e) {
						$("<menu/>")

								.append($("<command/>")

										.text("Reset…")

										.click(new Action<Event>() {
											@Override public void execute(final Event event) { reset(); }
										}))

								.<Menu>as().open().align(e.current(), Align.Start, Align.After);
					}
				})


				.<Bus>as()

				.activity(new Action.Monitor() {
					@Override protected void active(final boolean active) { busy(active); }
				});

	}


	private void about() {
		root().fire(Protocol.Home.About);
	}

	private void reset() {
		root().<Bus>as().reset(false);
	}


	private void busy(final Boolean busy) {
		root().is("busy", busy);
	}

}
