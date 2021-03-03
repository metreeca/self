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

package com.metreeca.self.client.faces.marker;

import com.metreeca._tile.client.View;
import com.metreeca.self.client.faces.Role;
import com.metreeca.self.client.faces.Tray;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;

import static com.metreeca._tile.client.Tile.$;


final class MarkerSeries extends View {

	private MarkerModel model=new MarkerModel();

	private final Role item;
	private final Role group;

	private final Role point;
	private final Role value;
	private final Role detail;


	MarkerSeries() {
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

														}))))

								.append($("<tr/>")

										.append($("<td/>")

												.text("group"))

										.append($("<td/>")

												.append(group=new Role()

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isGroup(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setGroup(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														})))))

						.append($("<tbody/>")

								.append($("<tr/>")

										.append($("<td/>")

												.text("point"))

										.append($("<td/>")

												.append(point=new Role()

														.tray(new Tray<Path>() { // !!! factor

															@Override public boolean accepts(final Path path) {
																return model.isPoint(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setPoint(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))

								.append($("<tr/>")

										.append($("<td/>")

												.text("value"))

										.append($("<td/>")

												.append(value=new Role()

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isValue(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setValue(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														})))))

						.append($("<tbody/>")

								.append($("<tr/>")

										.append($("<td/>")

												.text("detail"))

										.append($("<td/>")

												.append(detail=new Role().multiple(true)

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isDetail(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).insertDetail(path, index));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))));
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public MarkerModel model() {
		return model;
	}

	public MarkerSeries model(final MarkerModel model) {

		this.model=model;

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private MarkerSeries render() {

		final Report.Marker lens=model.lens(false);

		if ( lens != null ) {

			item.series(lens.getItem());
			group.series(lens.getGroup());

			point.series(lens.getPoint());
			value.series(lens.getValue());

			detail.series(lens.getDetails());

		} else {

			item.series((Path)null);
			group.series((Path)null);

			point.series((Path)null);
			value.series((Path)null);

			detail.series((Path)null);

		}

		return this;
	}
}
