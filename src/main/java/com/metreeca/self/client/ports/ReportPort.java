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

package com.metreeca.self.client.ports;

import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._jeep.shared.files.csv.CSVWriter;
import com.metreeca._jeep.shared.files.csv.Table;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;
import com.metreeca._tool.client.forms.Export;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Tuples;

import java.util.ArrayList;
import java.util.List;

import static com.metreeca._tool.client.ports.ItemPort.renameWarning;


public final class ReportPort extends View {

	public ReportPort() {
		root("<script/>")

				.bind(Export.class, new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( xport(e.<Export<?>>data()) ) { e.cancel(); }
					}
				});

	}

	private boolean xport(final Export<?> xport) {
		if ( !xport.fulfilled()
				&& xport.source().getClass().equals(Report.class)
				&& xport.type("text/csv") ) {

			final Report report=(Report)xport.source();
			final String label=report.getLabel().isEmpty() ? report.getUUID() : report.getLabel();

			root().async(new Tuples()

					.setEndpoint(report.getEndpoint())
					.setSpecs(report.getSpecs())
					.setLabel(true)

					.then(new Handler<Tuples>() {
						@Override public void value(final Tuples tuples) {
							csv(label, tuples);
						}
					}));

			renameWarning();

			return true;

		} else {
			return false;
		}
	}

	private void csv(final String label, final Tuples tuples) {

		root().window().save("text/csv;charset=utf-8", label, ".csv", new CSVWriter().write(new Table() {

			@Override public List<String> labels() {

				final List<String> labels=new ArrayList<>();

				for (final Path field : tuples.getSpecs().getFields()) {
					labels.add(field.label());
				}

				return labels;
			}

			@Override public Iterable<List<String>> records() {

				final List<List<String>> records=new ArrayList<>();

				for (final List<Term> tuple : tuples.getEntries()) {

					final List<String> record=new ArrayList<>();

					for (final Term term : tuple) {
						record.add(term == null ? null : term.label());
					}

					records.add(record);
				}

				return records;
			}

		}));
	}

}
