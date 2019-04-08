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

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class Image extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Image.css") TextResource skin();

	}


	public Image() {
		this("");
	}

	public Image(final String placeholder) {

		if ( placeholder == null ) {
			throw new NullPointerException("null placeholder");
		}

		root("<a/>")

				.skin(resources.skin().getText())

				.attribute("target", "_blank") // open image in a new window

				.style("background-image", "url('"+placeholder.trim()+"')"); // remove trailing newlines
	}

	public Image src(final String url) {

		if ( url == null ) {
			throw new NullPointerException("null url");
		}

		final String href=root().attribute("href");

		if ( href == null || !href.equals(url) ) {
			root().clear()

					.is("small busy", true)

					.attribute("href", url)

					.append(url.isEmpty() ? null : $("<img/>")

							.bind("load", new Action<Event>() {
								@Override public void execute(final Event e) {
									root().is("small busy", false)
											.style("background-image", "url('"+url.trim()+"')"); // remove trailing newlines
								}
							})

							.bind("error", new Action<Event>() {
								@Override public void execute(final Event e) {
									root().is("small busy", false);
									e.target().remove();
								}
							})

							.attribute("src", url));
		}

		return this;
	}

	public Image alt(final String description) {

		if ( description == null ) {
			throw new NullPointerException("null description");
		}

		root().attribute("alt", description);

		return this;
	}

}
