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

package com.metreeca.self.client.faces.marker;

import com.metreeca._info.client.Info;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.client.views.PathView;
import com.metreeca.self.client.views.TermInfo;
import com.metreeca.self.shared.beans.Path;

import static com.metreeca._tile.client.Tile.$;


final class MarkerView extends View {

	private MarkerModel model;

	private boolean refractory; // ;( prevent redraw on user-triggered viewport change // !!! review


	private final Info info=Info.create("map.marker", MarkerModel.defaults)

			.detail(new Info.Handler<Tile>() {
				@Override public Tile handle(final JSObject data, final int index) { return detail(data); }
			})

			.action(new Info.Handler<Boolean>() {
				@Override public Boolean handle(final JSObject data, final int index) { return action(data); }
			})

			.handler("move", new Info.Handler<Boolean>() {
				@Override public Boolean handle(final JSObject data, final int index) { return move(data); }
			});


	MarkerView() {
		root("<figure/>").is("placeholder marker-map", true)

				.drop(new PathDrop());
	}


	private Tile detail(final JSObject cell) {

		final TermInfo info=new TermInfo()

				.term(model.item(cell))

				.details(model.group(), model.group(cell))

				.details(model.point().equals(model.item()) ? null : model.point(), model.point(cell))
				.details(model.value(), model.value(cell));

		for (final int detail : model.details()) {
			info.details(model.path(detail), model.term(cell.get("i", -1), detail));
		}

		return $(info);
	}

	private Boolean action(final JSObject cell) {
		return model != null && root().fire(model.item(cell)); // model may be null after first click on dblclik
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public MarkerModel model() {
		return model;
	}

	public MarkerView model(final MarkerModel model) {

		this.model=model;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void series(final Path path) {
		if ( model.isPoint(path) ) { // as first test: labels may have coordinates as well

			root().async(model.lens(true).setPoint(path)); // preferred role

		} else if ( model.isGroup(path) ) {

			root().async(model.lens(true).setGroup(path)); // preferred role

		} else {

			root().async(model.lens(true).removeHidden(path)); // let model auto-bind path

		}
	}

	private boolean move(final JSObject data) { // !!! refactor

		// move events may be fired on window resize before model is set during rendering

		if ( model != null && !model.isLocked() ) { // if locked ignore viewport changes on window resize

			final JSObject port=JSObject.object()
					.set("lat", data.get("lat", 0))
					.set("lng", data.get("lng", 0))
					.set("zoom", data.get("zoom", 0));

			if ( data.get("user", false) ) { // user generated viewport change >> update lens setup

				try {

					root().async(model.lens(true).setSetup(model.options().set("port", port).json()));

				} finally {
					refractory=true;
				}

			} else { // auto-adjusted viewport >> store for future use
				model.port(port);
			}
		}

		return true;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private MarkerView render() {

		if ( model == null ) {

			root().clear();

		} else {

			try {

				if ( !refractory ) {

					info.setup(model.setup()).draw(root(), model.data());

				}

			} finally {
				refractory=false;
			}

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
			return model.isItem(path) || model.isGroup(path) || model.isPoint(path) || model.isValue(path);
		}

		private Path path(final Event e) {
			return path(e.data(PathView.class));
		}

		private Path path(final PathView view) {
			return (view == null) ? null : view.path();
		}

	}

}
