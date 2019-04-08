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

package com.metreeca.self.client.faces.auto;

import com.metreeca.self.shared.async.Handler;
import com.metreeca.self.shared.async.Morpher;
import com.metreeca.self.shared.async.Promise;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca.self.shared.Item.Auto;
import com.metreeca.self.client.faces.Face;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.Report.Board;
import com.metreeca.self.shared.Report.Book;
import com.metreeca.self.shared.Report.Table;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.forms.Tuples;
import com.metreeca.self.shared.sparql.Flake;

import static com.metreeca.self.shared.async.Promises.promise;


public final class AutoFace extends Face<AutoFace> {

	private Report report;


	public AutoFace() {
		root()

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); }
				});
	}


	@Override protected AutoFace self() {
		return this;
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	private AutoFace report(final Report report) {

		this.report=(report.getLens() instanceof Auto && !report.getEndpoint().isEmpty()) ? report : null;

		return render();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override protected AutoFace render() {

		if ( report != null ) {
			root().async(new Action<Event>() {
				@Override public void execute(final Event event) { // prevent concurrent report modification
					promise(report)

							.pipe(new Morpher<Report, Report>() {
								@Override public Promise<Report> value(final Report report) throws Exception { // identify top-level resources
									return promise(report)

											.pipe(test(Specs.Collections()))
											.pipe(test(Specs.Classes()))
											.pipe(test(Specs.Types()))
											.pipe(test(Specs.Resources()));
								}
							})

							.pipe(new Morpher<Report, Report>() {
								@Override public Promise<Report> value(final Report report) { // insert default field

									final Specs specs=report.getSpecs();

									if ( specs.isVoid() ) {
										specs.insertPath(new Path().setLabel(specs.label()));
									}

									return promise(report);
								}
							})

							.pipe(new Morpher<Report, Report>() {
								@Override public Promise<Report> value(final Report report) { // choose default lens

									final Promise<Report> promise=promise();

									promise.value(report.setLens(collection(report) ? new Board()
											: singleton(report) ? new Book()
											: new Table()));


									return promise;
								}
							})

							.then(new Handler<Report>() {
								@Override public void value(final Report report) {
									root().fire(report);
								}
							});

				}
			});
		}

		return this;
	}


	private boolean collection(final Report report) {
		return report.getSpecs().getFields().get(0).isCollection();
	}

	private boolean singleton(final Report report) {
		return new Flake().specs(report.getSpecs()).term() != null;
	}


	private Morpher<Report, Report> test(final Specs specs) {
		return new Morpher<Report, Report>() {
			@Override public Promise<Report> value(final Report report) {

				final Promise<Report> promise=promise();

				if ( report.getSpecs().isEmpty() ) {

					root().fire(new Tuples()

							.setEndpoint(report.getEndpoint())
							.setSpecs(specs)

							.then(new Handler<Tuples>() {
								@Override public void value(final Tuples tuples) {
									promise.value(tuples.getEntries().isEmpty() ? report : report.setSpecs(specs));
								}
							}));

				} else {
					promise.value(report);
				}

				return promise;
			}
		};
	}

}
