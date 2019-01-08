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

package com.metreeca.self.client.faces.marker;

import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca.self.client.faces.Controls;
import com.metreeca.self.client.faces.Face;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.forms.Tuples;

import static com.metreeca._tile.client.Tile.$;


public final class MarkerMap extends Face<MarkerMap> {

	private Report report;
	private Tuples tuples;

	private final MarkerView view;

	private final Controls controls;
	private final MarkerSeries series;
	private final MarkerOptions options;


	public MarkerMap() {
		root().is("placeholder", true)

				.append(view=new MarkerView())
				.append(controls=new Controls(
						$(series=new MarkerSeries()),
						$(options=new MarkerOptions())))

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				});
	}


	@Override protected MarkerMap self() {
		return this;
	}


	////// Model ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private Report report() {
		return report != null && !report.getEndpoint().isEmpty() ? report : null;
	}

	private MarkerMap report(final Report report) {

		if ( report != null && this.report != null && !this.report.equals(report) ) { report(null); }

		this.report=(report != null && report.getLens() instanceof Report.Marker) ? report : null;
		this.tuples=null;

		return render();
	}


	private Tuples tuples() {

		if ( report() != null && tuples == null ) {
			root().async(this.tuples=new Tuples()

					.setEndpoint(report.getEndpoint())
					.setSpecs(report().getSpecs())
					.setLimit(MarkerModel.MarkerLimit)
					.setLabel(true)
					.setImage(true)
					.setNotes(true)
					.setPoint(true)

					.then(new Handler<Tuples>() {
						@Override public void value(final Tuples tuples) { render(); }
					}));
		}

		return tuples != null && tuples.fulfilled() ? tuples : null;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected MarkerMap render() {

		final Report report=report();
		final Tuples tuples=tuples();

		if ( report == null ) {

			root().hide().clear();

			view.model(null); // don't render if model is not set
			// !!! controls.model(null);
			// !!! series.model(null);
			// !!! options.model(null);

		} else if ( tuples == null ) {

			root().append(view).append(controls).show(); // show placeholder

		} else {

			final MarkerModel model=new MarkerModel()
					.report(report)
					.tuples(tuples);

			view.model(model);
			series.model(model);
			options.model(model);
			controls.model(model); // after series/options to see sizes

		}

		return this;
	}
}

