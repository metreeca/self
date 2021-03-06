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

package com.metreeca.self.client.faces.bubble;

import com.metreeca._tile.client.Info;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.client.views.PathView;
import com.metreeca.self.client.views.TermInfo;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.forms.Tuples;

import static com.metreeca._tile.client.Tile.$;


final class BubbleView extends View {

	private BubbleModel model;


	private final Info info=Info.create("chart.bubble", BubbleModel.defs)

			.detail(new Info.Handler<Tile>() {
				@Override public Tile handle(final JSObject data, final int index) { return detail(data); }
			})

			.action(new Info.Handler<Boolean>() {
				@Override public Boolean handle(final JSObject data, final int index) { return action(data); }
			});


	public BubbleView() {
		root("<figure/>").is("placeholder bubble-chart", true)

				.drop(new PathDrop());
	}


	private Tile detail(final JSObject cell) {
		return $(new TermInfo()

				.term(model.item(cell))

				.details(model.group(), model.group(cell))

				.details(model.x(), model.x(cell))
				.details(model.y(), model.y(cell))
				.details(model.z(), model.z(cell)));
	}

	private Boolean action(final JSObject cell) {
		return model != null && root().fire(model.item(cell)); // model may be null after first click on dblclik
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public BubbleModel model() {
		return model;
	}

	public BubbleView model(final BubbleModel model) {

		this.model=model;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void series(final Path path) {
		if ( model.isGroup(path) ) {
			root().async(model.lens(true).setGroup(path)); // preferred compatible role
		} else {
			root().async(model.lens(true).removeHidden(path)); // let model auto-bind path
		}
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private BubbleView render() {

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
			return model.isItem(path) || model.isGroup(path) || model.isValue(path);
		}

		private Path path(final Event e) {
			return path(e.data(PathView.class));
		}

		private Path path(final PathView view) {
			return (view == null) ? null : view.path();
		}

	}
}
