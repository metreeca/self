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

package com.metreeca.self.client.faces.pie;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.js.JSObject;

import static com.metreeca._tile.client.Tile.$;


final class PieOptions extends View {

	private PieModel model;


	private final Tile layout;


	public PieOptions() {
		root("<form/>")

				.append($("<table/>")

						.append($("<tbody/>")

								.append(layout=layout())));
	}


	private Tile layout() {
		return $("<tr/>")

				.append($("<td/>")

						.text("layout"))

				.append($("<td/>")

						.append($("<label/>")

								.append($("<input type='radio' name='layout' value='0.5'/>"))
								.append($("<span/>").text("donut")))

						.append($("<br/>"))

						.append($("<label/>")

								.append($("<input type='radio' name='layout' value='0.0'/>"))
								.append($("<span/>").text("pie")))

						.change(new Action<Event>() {
							@Override public void execute(final Event e) {
								layout(e.target().value());
							}
						}));
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public PieOptions model(final PieModel model) {

		this.model=model;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void layout(final String inner) {
		root().async(model.lens(true).setSetup(model.options().set("inner", inner).json()));
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private PieOptions render() {

		if ( model != null ) {

			final JSObject options=model.options();

			final String inner=options.get("inner", PieModel.defs.string("inner"));

			layout.find("input").each(new Lambda<Tile>() {
				@Override public Tile apply(final Tile tile) {
					return tile.checked(tile.attribute("value").equals(inner));
				}
			});

		}

		return this;
	}

}
