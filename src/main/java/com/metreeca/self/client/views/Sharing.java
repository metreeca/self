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
import com.metreeca._tile.client.plugins.Overlay.Align;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class Sharing extends View {

	private static final Resources resources=GWT.create(Resources.class);


	private static native void select(final Tile tile) /*-{

		(tile[0] || $doc.documentElement).select();

	}-*/;

	private static native boolean copy(final Tile tile) /*-{

		(tile[0] || $doc.documentElement).select();

		return $doc.execCommand('copy');

	}-*/;


	public static interface Resources extends ClientBundle {

		@Source("Sharing.css") TextResource skin();

	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private String link="";

	private final Tile text;


	public Sharing() {
		root("<div/>")

				.skin(resources.skin().getText())

				.<Overlay>as()

				.modal(true)
				.shading(true)

				.<Tile>as()

				.append(this.text=$("<input/>")

						.attribute("type", "text")
						.attribute("readonly", true)

						.focus(new Action<Event>() {
							@Override public void execute(final Event e) { select(e.target()); }
						}))

				.append($("<button/>").is("fa fa-copy", true)

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { copy(); }
						})
				)

				.append($("<button/>").is("fa fa-check", true)

						.click(new Action<Event>() {
							@Override public void execute(final Event e) { close(); }
						})
				);
	}

	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////


	public Sharing link(final String link) {

		if ( link == null ) {
			throw new NullPointerException("null link");
		}

		this.link=link;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public Sharing open() {

		root().<Overlay>as().open().align(Align.Center, Align.Center);

		return this;
	}


	public Sharing close() {

		root().<Overlay>as().close().as();

		return this;
	}

	public Sharing copy() {

		if ( !copy(text) ) {
			root().warning(getClass(), "unable to copy link");
		}

		//return close();

		return this;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Sharing render() {

		text.value(link);

		return this;

	}

}
