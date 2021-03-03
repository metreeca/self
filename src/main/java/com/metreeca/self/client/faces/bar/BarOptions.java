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

package com.metreeca.self.client.faces.bar;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.js.JSObject;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;


final class BarOptions extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("BarOptions.xml") TextResource form();

	}


	private BarModel model=new BarModel();


	public BarOptions() {
		root(resources.form().getText())

				.change(new Action<Event>() {
					@Override public void execute(final Event e) { // !!! factor

						final Tile target=e.target();

						final String name=target.name();
						final String value=target.value();

						final Boolean checked=target.property("checked");

						option(name, checked == null || checked ? value : "false");
					}
				});
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public BarOptions model(final BarModel model) {

		this.model=model;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void option(final String name, final String value) {
		root().async(model.lens(true).setSetup(model.options().set(name, value).json()));
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private BarOptions render() {

		if ( model != null ) {

			final JSObject options=model.options();

			root().find("input[type=checkbox], input[type=radio]").each(new Lambda<Tile>() { // !!! factor
				@Override public Tile apply(final Tile tile) {

					final String name=tile.name();
					final String value=options.get(name, BarModel.defs.string(name));

					return tile.checked(tile.value().equals(value));

				}
			});

		}

		return this;
	}

}
