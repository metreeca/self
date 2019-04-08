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

package com.metreeca.self.client.views;

import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Overlay;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Shoots;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.ArrayList;
import java.util.List;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.plugins.Overlay.Align.Center;


public final class PathPicker extends View {

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("PathPicker.css") TextResource skin();

	}


	private Report report;
	private List<Term> steps;
	private Shoots shoots;

	private String pattern;


	private final Input filter;

	private final Tile edges;
	private final Tile slots;
	private final Tile roles;


	public PathPicker() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append(roles=$("<div/>").is("roles lane", true))

				.append($("<div/>")

						.append($("<header/>")

								.append($(filter=new Input().immediate(true).search(true)).is("filter", true)

										.change(new Action<Event>() {
											@Override public void execute(final Event e) {
												if ( e.stop().cancel() ) { // stop propagation: will be refired by select()
													pattern(e.target().value());
												}
											}
										})))

						.append(edges=$("<div/>").is("edges lane", true))

						.append($("<footer/>")

								.append($("<button/>").is("fa fa-close", true).title("Cancel")

										.click(new Action<Event>() {
											@Override public void execute(final Event e) { cancel(); }
										}))

								.append($("<button/>").is("fa fa-check", true).title("Done")

										.click(new Action<Event>() {
											@Override public void execute(final Event e) { select(); }
										}))))

				.append(slots=$("<div/>").is("slots lane", true));
	}


	public PathPicker open(final Report report, final Path path) {

		if ( report == null ) {
			throw new NullPointerException("null report");
		}

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		root().<Overlay>as()
				.modal(true)
				.shading(true)
				.open().align(Center, Center);

		return report(report).steps(new ArrayList<>(path.getSteps())); // use a writable copy
	}

	public PathPicker close() {

		root().<Overlay>as().close();

		return this;
	}


	//// Model ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Report report() {
		return report;
	}

	private PathPicker report(final Report report) {

		this.report=report;
		this.steps=null;
		this.shoots=null;

		this.pattern=null;

		return render();
	}


	private List<Term> steps() {
		return steps;
	}

	private PathPicker steps(final List<Term> steps) {

		this.steps=steps;
		this.shoots=null;

		this.pattern=null;

		return render();
	}


	private Shoots shoots() {

		if ( report != null && steps != null && shoots == null ) {

			root().fire(this.shoots=new Shoots()

					.setEndpoint(report.getEndpoint())
					.setSpecs(report.getSpecs())
					.setPath(steps)
					.setLabel(true)

					.then(new Handler<Shoots>() {
						@Override public void value(final Shoots shoots) { shoots(shoots); }
					}));
		}

		return shoots != null && shoots.fulfilled() ? shoots : null;
	}

	private PathPicker shoots(final Shoots shoots) {

		this.shoots=shoots;
		this.pattern=null;

		return render();
	}


	private PathPicker pattern(final String pattern) {

		this.pattern=pattern.isEmpty() ? null : pattern;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private PathPicker clear() {
		return steps(new ArrayList<Term>());
	}

	private PathPicker clip(final Term step) {

		final int index=steps.indexOf(step);

		if ( index >= 0 ) {
			for (int i=steps.size()-1; i > index; --i) { steps.remove(i); }
		}

		return steps(steps);
	}

	private PathPicker push(final Term edge) {

		steps.add(edge);

		this.shoots=null;
		this.pattern=null;

		return steps(steps);
	}


	private void cancel() {
		try {

			root().<Bus>as().activity((Boolean)null); // cancel pending network requests

		} finally {
			close();
		}
	}

	private void select() {
		try {

			root().<Bus>as().activity((Boolean)null); // cancel pending network requests before signalling change
			root().fire("change", new Path(steps).setLabel(steps.isEmpty() ? report.getSpecs().label() : ""));

		} finally {
			close();
		}
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private PathPicker render() {

		final Report report=report();
		final List<Term> steps=steps();
		final Shoots shoots=shoots();

		edges.clear(".link");
		slots.clear(".link");
		roles.clear(".link");

		if ( report != null && steps != null ) {

			filter.value(pattern).focus();

			edges.append(link(report.getSpecs()) // item node

					.click(new Action<Event>() {
						@Override public void execute(final Event e) { clear(); }
					}));

			for (final Term step : this.steps) { // current steps
				edges.append(link(step, way(step))

						.click(new Action<Event>() {
							@Override public void execute(final Event e) {
								try { clip(step); } finally { e.cancel(); }
							}
						}));
			}

			edges.scroll(0, -1); // keep scrolled to bottom;

			if ( shoots != null ) {

				$(slots).is("busy", false);
				$(roles).is("busy", false);

				final String pattern=this.pattern == null ? null : this.pattern.toUpperCase();

				for (final Term edge : shoots.getEntries()) {
					if ( pattern == null || edge.label().toUpperCase().contains(pattern) ) {
						(edge.isRecto() ? slots : edge.isVerso() ? roles : $())

								.append(link(edge, way(edge))

										.click(new Action<Event>() {
											@Override public void execute(final Event e) {
												try { push(edge); } finally { e.cancel(); }
											}
										}));
					}
				}

			} else {
				$(slots).is("busy small", true);
				$(roles).is("busy small", true);
			}

		}

		return this;
	}


	private String way(final Term term) {
		return term.isRecto() ? "recto" : term.isVerso() ? "verso" : "";
	}


	private Tile link(final Specs specs) {
		return link(specs.label(), "", "");
	}

	private Tile link(final Term term, final String way) {
		return link(term.label(), term.getText(), way);
	}

	private Tile link(final String label, final String uri, final String way) {
		return $("<a/>")

				.is("link", true)
				.is(way, true)

				.attribute("href", uri) // report uri to disambiguate homonymous terms
				.text(label)

				.bind("mouseenter", new Action<Event>() {
					@Override public void execute(final Event e) {
						e.current().title(e.current().dw() <= e.current().width() ? "" : label);
					}
				});
	}

}
