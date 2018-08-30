/*
 * Copyright © 2013-2018 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Metreeca/Self. If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.client.faces.bar;

import com.metreeca._tile.client.View;
import com.metreeca.self.client.faces.Role;
import com.metreeca.self.client.faces.Tray;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;

import static com.metreeca._tile.client.Tile.$;


final class BarSeries extends View {

	private BarModel model=new BarModel();

	private final Role item;
	private final Role value;


	public BarSeries() {
		root("<form/>")

				.append($("<table/>")

						.append($("<tbody/>")

								.append($("<tr/>")

										.append($("<td/>")

												.text("item"))

										.append($("<td/>")

												.append(item=new Role()

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isItem(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setItem(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														})))))

						.append($("<tbody/>")

								.append($("<tr/>")

										.append($("<td/>")

												.text("value"))

										.append($("<td/>")

												.append(value=new Role().multiple(true)

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isValue(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).insertValue(path, index));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))));
	}

	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public BarModel model() {
		return model;
	}

	public BarSeries model(final BarModel model) {

		this.model=model;

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private BarSeries render() {

		final Report.Bar lens=model.lens(false);

		if ( lens != null ) {

			item.series(lens.getItem());
			value.series(lens.getValues());

		} else {

			item.series((Path)null);
			value.series((Path)null);

		}

		return this;
	}
}
