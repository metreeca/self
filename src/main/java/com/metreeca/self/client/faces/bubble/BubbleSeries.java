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

package com.metreeca.self.client.faces.bubble;

import com.metreeca._tile.client.View;
import com.metreeca.self.client.faces.Role;
import com.metreeca.self.client.faces.Tray;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;

import static com.metreeca._tile.client.Tile.$;


final class BubbleSeries extends View {

	private BubbleModel model=new BubbleModel();

	private final Role item;
	private final Role group;

	private final Role x;
	private final Role y;
	private final Role z;


	public BubbleSeries() {
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

												.text("x"))

										.append($("<td/>")

												.append(x=new Role()

														.tray(new Tray<Path>() { // !!! factor

															@Override public boolean accepts(final Path path) {
																return model.isValue(path);
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

												.append(y=new Role()

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isNumber(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setY(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))

								.append($("<tr/>")

										.append($("<td/>")

												.text("z"))

										.append($("<td/>")

												.append(z=new Role()

														.tray(new Tray<Path>() {

															@Override public boolean accepts(final Path path) {
																return model.isNumber(path);
															}

															@Override public void insert(final Path path, final int index) {
																root().async(model.lens(true).setZ(path));
															}

															@Override public void remove(final Path path) {
																root().async(model.lens(true).insertHidden(path));
															}

														}))))));
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public BubbleModel model() {
		return model;
	}

	public BubbleSeries model(final BubbleModel model) {

		this.model=model;

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private BubbleSeries render() {

		final Report.Bubble lens=model.lens(false);

		item.series(lens == null ? null : lens.getItem());
		group.series(lens == null ? null : lens.getGroup());

		x.series(lens == null ? null : lens.getX());
		y.series(lens == null ? null : lens.getY());
		z.series(lens == null ? null : lens.getZ());

		return this;
	}
}
