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

import com.metreeca._tile.client.Tile;
import com.metreeca._tile.client.View;
import com.metreeca._tool.client.views.Image;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class TermInfo extends View {

	private static final int NotesLength=200; // maximum notes length


	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("TermInfo.css") TextResource skin();

	}


	private final Tile header;
	private final Tile footer;


	public TermInfo() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append(header=$("<header/>"))
				.append(footer=$("<footer/>"));

	}


	public TermInfo term(final Term term) {

		header.clear();

		if ( term != null ) {

			final String image=term.getImage();

			header.insertInto(root())

					.append(new TermView().term(term))

					.append(image.isEmpty() ? null : new Image().src(image))

					.append($("<p/>").text(clip(term.getNotes(), NotesLength)));

		} else {
			header.remove();
		}

		return this;
	}

	public TermInfo details(final Path path, final Term... terms) {
		return details(path, false, terms);
	}

	public TermInfo details(final Path path, final boolean highlight, final Term... terms) {

		if ( path != null ) {

			final Tile section=$("<section/>").appendTo(footer)

					.is("highlight", highlight);

			$("<span/>").appendTo(section)

					.text(path.label());

			final Tile values=$("<div/>").appendTo(section);

			if ( terms != null ) {
				for (final Term term : terms) {
					if ( term != null ) { values.append(new TermView().term(term)); }
				}
			}
		}

		return this;
	}


	private String clip(final String text, final int length) {
		return text.length() <= length ? text : text.substring(0, length-1)+"…";
	}
}
