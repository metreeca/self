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

package com.metreeca.self.client.faces.pie;


import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca.self.client.faces.Controls;
import com.metreeca.self.client.faces.Face;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.forms.Tuples;

import static com.metreeca._tile.client.Tile.$;


public final class PieChart extends Face<PieChart> {

	private Report report;
	private Tuples tuples;

	private final PieView view;

	private final Controls controls;
	private final PieSeries series;
	private final PieOptions options;


	public PieChart() {
		root().is("placeholder", true)

				.append(view=new PieView())
				.append(controls=new Controls(
						$(series=new PieSeries()),
						$(options=new PieOptions())))

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				});
	}


	@Override protected PieChart self() {
		return this;
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Report report() {
		return report != null && !report.getEndpoint().isEmpty() ? report : null;
	}

	private PieChart report(final Report report) {
		if ( report != null && this.report != null && !this.report.equals(report) ) { report(null); }

		this.report=(report != null && report.getLens() instanceof Report.Pie) ? report : null;
		this.tuples=null;

		return render();
	}


	private Tuples tuples() {

		if ( report() != null && tuples == null ) {
			root().async(this.tuples=new Tuples()

					.setEndpoint(report.getEndpoint())
					.setSpecs(report().getSpecs())
					.setLimit(PieModel.SliceLimit)
					.setLabel(true)
					.setImage(true)
					.setNotes(true)

					.then(new Handler<Tuples>() {
						@Override public void value(final Tuples tuples) { render(); }
					}));
		}

		return tuples != null && tuples.fulfilled() ? tuples : null;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected PieChart render() {

		final Report report=report();
		final Tuples tuples=tuples();

		if ( report == null ) {

			root().hide().clear();

		} else if ( tuples == null ) {

			root().show(); // show placeholder

		} else {

			final PieModel model=new PieModel()
					.report(report)
					.tuples(tuples);

			root()
					.append(view)
					.append(controls);

			view.model(model);
			series.model(model);
			options.model(model);
			controls.model(model); // after bindings/options to see sizes

		}

		return this;
	}
}
