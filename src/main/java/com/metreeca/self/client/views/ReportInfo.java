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

package com.metreeca.self.client.views;

import com.metreeca._tile.client.*;
import com.metreeca._tile.client.plugins.Form;
import com.metreeca.self.client.tiles.Endpoint;
import com.metreeca.self.shared.Command;
import com.metreeca.self.shared.Report;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;


public final class ReportInfo extends View {

	private static final DateTimeFormat DateFormat=DateTimeFormat.getFormat("yyyy-MM-dd HH:mm"); // !!! factor

	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("ReportInfo.xml") TextResource form();

	}


	private Report report;

	private final List<String> endpoints=new ArrayList<>();


	public ReportInfo() {
		root(resources.form().getText())

				.bind(Report.class, new Action<Event>() {
					@Override public void execute(final Event e) { report(e.<Report>data()); } // update if duplicating a locked report
				})

				.<Form>as().wire()

				.submit(new Action<Event>() {

					@Override public void execute(final Event e) {
						root().fire(new Command.Change<Report>(memo(new Report(), report), report()) {

							@Override protected boolean set(final Report value) {
								return root().fire(memo(report.updated(), value));
							}

							@Override public String label() {
								return "Edit report";
							}

						});
					}

					private Report memo(final Report target, final Report source) {
						return target

								.setLabel(source.getLabel())
								.setNotes(source.getNotes())

								.setLens(source.getLens())
								.setEndpoint(source.getEndpoint())
								.setSpecs(source.getSpecs());
					}
				});
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////


	public Report report() {
		return new Report()

				.setLabel(root().find(".label").value(true))
				.setNotes(root().find(".notes").value(true))

				.setLens(report.getLens())
				.setEndpoint(root().find(".endpoint").<Endpoint>as().endpoint())
				.setSpecs(report.getSpecs());
	}

	public ReportInfo report(final Report report) {

		if ( report == null ) {
			throw new NullPointerException("null report");
		}

		this.report=report;

		root().<Form>as().edited(false);

		root().find(".label").value(report.getLabel());
		root().find(".notes").value(report.getNotes());

		root().find(".created").text(date(report.getCreated()));
		root().find(".updated").text(date(report.getUpdated()));

		root().find(".endpoint").<Endpoint>as()

				.endpoint(report.getEndpoint())
				.endpoints(endpoints);

		return this;
	}


	public ReportInfo locked(final boolean locked) {

		root().<Form>as().locked(locked);

		return this;
	}


	public ReportInfo endpoints(final Collection<String> endpoints) {

		if ( endpoints == null ) {
			throw new NullPointerException("null endpoints");
		}

		this.endpoints.clear();
		this.endpoints.addAll(endpoints);

		return this;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private String date(final long time) {
		return DateFormat.format(new Date(time));
	}

	private String number(final int number) {
		return number == 0 ? "" : String.valueOf(number);
	}

	private String percentage(final int count, final int total) {
		return count > 0 && total > 0 ? String.valueOf(count*100/total) : "";
	}

}
