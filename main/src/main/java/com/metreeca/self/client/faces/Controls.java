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

package com.metreeca.self.client.faces;

import com.metreeca._tile.client.*;
import com.metreeca._tool.client.Tool.Setup;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.Self.Mode;
import com.metreeca.self.client.views.PathView;
import com.metreeca.self.shared.Report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import static com.metreeca._tile.client.Tile.$;


public final class Controls extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("Controls.css") TextResource skin();

	}


	private boolean visualizer;

	private Model<?> model;

	private final Tile hide;

	private final Tile bindings;
	private final Tile options;


	// visibility memos for series panel auto opening/closing

	private Boolean seriesMemo;
	private Boolean optionsMemo;


	public Controls(final Tile series, final Tile options) {

		if ( series == null ) {
			throw new NullPointerException("null series");
		}

		if ( options == null ) {
			throw new NullPointerException("null options");
		}

		root("<div/>").hide() // hidden until model is received

				.skin(resources.skin().getText())

				.append($("<header/>")

						.append(hide=$("<button/>").hide()

								.is("fa fa-chevron-right", true)
								.title("Hide panels")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { none(); }
								}))

						.append($("<button/>")

								.enabled(series.size() > 0).is("fa fa-list-ul", true)
								.title("Toggle series panel")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) {
										series(!series.visible());
									}
								}))

						.append($("<button/>")

								.enabled(options.size() > 0).is("fa fa-gear", true)
								.title("Toggle options panel")

								.click(new Action<Event>() {
									@Override public void execute(final Event e) {
										options(!options.visible());
									}
								})))

				.append(this.bindings=series.visible(false))
				.append(this.options=options.visible(false))

				// auto open/close series panel on path drag

				.bind("dragenter", new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( e.data(PathView.class) != null && open() ) {
							$("html").bind("dragend", new Action<Event>() {
								@Override public void execute(final Event e) {
									if ( close() ) { $("html").drop("dragend", this); }
								}
							});
						}
					}
				})

				.<Bus>as()

				.setup(new Action<Setup<Report>>() {
					@Override public void execute(final Setup<Report> setup) {
						visualizer=setup.token(Mode.Visualizer);
					}
				});
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Model<?> model() {
		return model;
	}

	public Controls model(final Model<?> model) {

		this.model=(model.lens(false) instanceof Report.Info) ? model : null;

		return render();
	}


	private Report.Info<?> lens(final boolean writable) {
		return model != null ? (Report.Info<?>)model.lens(writable) : null;
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void none() {
		root().async(lens(true)
				.setSeries(false)
				.setOptions(false));
	}

	private void series(final boolean series) {
		root().async(lens(true)
				.setSeries(series)
				.setOptions(false));
	}

	private void options(final boolean options) {
		root().async(lens(true)
				.setSeries(false)
				.setOptions(options));
	}


	private boolean open() {
		if ( seriesMemo == null && optionsMemo == null ) {

			seriesMemo=bindings.visible();
			optionsMemo=options.visible();

			hide.visible(true);
			bindings.visible(true);
			options.visible(false);

			freeze();

			return true;

		} else {

			return false;

		}
	}

	private boolean close() {
		if ( seriesMemo != null && optionsMemo != null ) {

			hide.visible(seriesMemo || optionsMemo);
			bindings.visible(seriesMemo);
			options.visible(optionsMemo);

			seriesMemo=null;
			optionsMemo=null;

			return true;

		} else {

			return false;

		}
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Controls render() {

		final Report.Info<?> lens=lens(false);

		if ( lens != null ) {

			final boolean series=lens.getSeries();
			final boolean options=lens.getOptions();

			root().visible(!visualizer && !model.isLocked());
			hide.visible(series || options);

			this.bindings.visible(series);
			this.options.visible(options);

			freeze();

		}

		return this;
	}

	private void freeze() { // freeze width to prevent accordion effects on dnd

		final Tile roles=root().find(".role.view");

		roles.style("width", "").style("width", roles.style("width", true));
	}
}
