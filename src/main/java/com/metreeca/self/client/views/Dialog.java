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
import com.metreeca._tile.client.plugins.Overlay;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align;


public final class Dialog extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Dialog.css") TextResource skin();

	}


	private boolean humanized;

	private final Tile message;
	private final Tile details;
	private final Tile options;


	public Dialog() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append(message=$("<h1/>"))

				.append(details=$("<p/>"))

				.append(options=$("<menu/>")

						.bind("click", new Action<Event>() {
							@Override public void execute(final Event e) {
								if ( e.cancel() ) { root().<Overlay>as().close(); }
							}
						}));
	}


	public Dialog humanized() {

		this.humanized=true;

		return this;
	}


	public Dialog message(final String message) {

		this.message.text(message);

		return this;
	}

	public Dialog details(final String details) {

		this.details.text(details);

		return this;
	}


	public Dialog action(final String option) {
		return action(option, null);
	}

	public Dialog action(final String option, final Action<Event> action) {

		options.append($("<command/>").text(option).bind("click", action));

		return this;
	}


	public Dialog done(final Action<Event> action) {

		options.bind("click", action);

		return this;
	}


	public Dialog open() {

		root().<Overlay>as()

				.modal(true)
				.shading(true)

				.humanized(humanized)
				.sticky(!humanized)

				.open()
				.align(Align.Center, Align.Center);

		return this;
	}

}
