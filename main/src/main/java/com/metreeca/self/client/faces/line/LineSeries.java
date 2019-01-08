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

package com.metreeca.self.client.faces.line;

import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;
import com.metreeca.self.client.faces.Role;
import com.metreeca.self.client.faces.Tray;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;

import static com.metreeca._tile.client.Tile.$;


final class LineSeries extends View {

	private LineModel model=new LineModel();

	private final Role item;

	private final Role x;
	private final Role ys;


	public LineSeries() {
		root("<form/>")

				.append($("<table/>")

						.append($("<tbody/>")

								.append($("<tr/>")

										.append($("<td/>")

												.text("item"))

										.append($("<td/>")

												.append(item=new Role()

														.tray(new Tray<Path>() { // !!! factor

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

												.text("x"))

										.append($("<td/>")

												.append(x=new Role()

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isValue(path); // !!! date support
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setX(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))

								.append($("<tr/>")

										.append($("<td/>")

												.text("y"))

										.append($("<td/>")

												.append(ys=new Role().multiple(true)

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isNumber(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).insertY(path, index));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))))


				.bind(LineModel.class, new Action<Event>() {
					@Override public void execute(final Event e) { model(e.<LineModel>data()); }
				});
	}

	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public LineModel model() {
		return model;
	}

	public LineSeries model(final LineModel model) {

		this.model=model;

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private LineSeries render() {

		final Report.Line lens=model.lens(false);

		item.series(lens == null ? null : lens.getItem());

		x.series(lens == null ? null : lens.getX());
		ys.series(lens == null ? null : lens.getYs());

		return this;
	}
}
