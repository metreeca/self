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

package com.metreeca.self.client.faces.line;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.client.views.PathView;
import com.metreeca.self.client.views.TermInfo;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.forms.Tuples;

import java.util.List;

import static com.metreeca._tile.client.Tile.$;


final class LineView extends View {

	private LineModel model;


	private final Info info=Info.create("chart.line", LineModel.defs)

			.detail(new Info.Handler<Tile>() {
				@Override public Tile handle(final JSObject data, final int index) {
					return detail(data);
				}
			})

			.action(new Info.Handler<Boolean>() {
				@Override public Boolean handle(final JSObject data, final int index) {
					return action(data);
				}
			});


	public LineView() {
		root("<figure/>").is("placeholder line-chart", true)

				.drop(new PathDrop());
	}


	private Tile detail(final JSObject cell) {

		final Report.Line lens=model.lens(false);

		final TermInfo info=new TermInfo()

				.term(model.item(cell))

				.details(lens.getX(), model.x(cell));

		final List<Path> ys=lens.getYs();

		for (int j=0; j < ys.size(); j++) {
			info.details(ys.get(j), j == cell.integer("j"), model.y(j, cell));
		}

		return $(info);

	}

	private Boolean action(final JSObject cell) {
		return model != null && root().fire(model.item(cell)); // model may be null after first click on dblclik
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public LineModel model() {
		return model;
	}

	public LineView model(final LineModel model) {

		this.model=model;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void series(final Path path) { // !!! if date bind as x unconditionally (dates can't be used as ys)
		root().async(model.lens(true).removeHidden(path)); // let model auto-bind path
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private LineView render() {

		final Report report=model.report();
		final Tuples tuples=model.tuples();

		if ( report == null ) {

			root().clear();

		} else if ( tuples != null ) {

			info.setup(model.setup()).draw(root(), model.data());

		}

		return this;
	}


	private final class PathDrop extends Action.Drop {

		@Override protected void dragenter(final Event e) {
			if ( accept(path(e)) && e.cancel() ) { e.mode("move"); }
		}

		@Override protected void dragover(final Event e) {
			if ( accept(path(e)) && e.cancel() ) { e.mode("move"); }
		}

		@Override protected void drop(final Event e) {
			if ( accept(path(e)) && e.cancel() ) { series(path(e)); }
		}


		private boolean accept(final Path path) {
			return model.isItem(path) || model.isValue(path);
		}

		private Path path(final Event e) {
			return path(e.data(PathView.class));
		}

		private Path path(final PathView view) {
			return (view == null) ? null : view.path();
		}
	}

}
