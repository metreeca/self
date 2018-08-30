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

package com.metreeca.self.client;

import com.metreeca._jeep.client.Option;
import com.metreeca._jeep.client.Options;
import com.metreeca._tile.client.Action;
import com.metreeca._tool.client.Tool;
import com.metreeca.self.client.ports.ReportPort;
import com.metreeca.self.client.ports.SPARQLPort;
import com.metreeca.self.client.ports.ShapePort;
import com.metreeca.self.client.views.ReportPage;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.sparql.Query;

import static com.metreeca._tile.client.Tile.$;
import static com.metreeca._tile.client.Window.window;


public final class Self extends Tool<Report> {

	public enum Mode {App, Catalog, Navigator, Visualizer}

	public enum Toolbar {Top, Bottom, None}

	public enum Fields {Top, None}

	public enum Facets {Right, Left, None}


	public static final Option<String> Endpoint=option("endpoint", "");
	public static final Option<String> Resource=option("resource", "");
	public static final Option<String> Collection=option("collection", "");
	public static final Option<String> Label=option("label", "");


	@Override protected int supported(final Browser browser) {
		return browser == Browser.Chrome ? 63
				: browser == Browser.Firefox ? 57
				: browser == Browser.Safari ? 10
				: browser == Browser.Edge ? 15
				: browser == Browser.Explorer ? 11
				: 0;
	}

	@Override protected Setup run(final Options options) {

		$("body")

				.append(new ReportPort()) // report codec
				.append(new ShapePort()) // query engine
				.append(new SPARQLPort()) // sparql client

				.append(new ReportPage());


		//// probes ////////////////////////////////////////////////////////////////////////////////////////////////////

		final boolean embedded=window().embedded();


		//// default options ///////////////////////////////////////////////////////////////////////////////////////////

		final Mode mode=options.token(Mode.class);

		final boolean catalog=mode == Mode.Catalog;
		final boolean navigator=mode == Mode.Navigator;
		final boolean visualizer=mode == Mode.Visualizer;

		final Options defaults=new Options()

				.token(Toolbar.class, embedded ? Toolbar.Bottom : null)

				.token(Fields.class, navigator || visualizer ? Fields.None : null)
				.token(Facets.class, navigator || visualizer ? Facets.None : null);


		//// system-controlled options /////////////////////////////////////////////////////////////////////////////////

		final Options system=new Options()

				.options(defaults)
				.options(options)

				.flag(Embedded, embedded);


		//// global css drivers ////////////////////////////////////////////////////////////////////////////////////////

		$("html").is("embedded", embedded)

				.is(Mode.class, false).is(system.token(Mode.class), true)
				.is(Toolbar.class, false).is(system.token(Toolbar.class), true)
				.is(Fields.class, false).is(system.token(Fields.class), true)
				.is(Facets.class, false).is(system.token(Facets.class), true);

		return new Setup(system);

	}


	public static final class Setup extends Tool.Setup<Report> {

		private Setup(final Options options) {
			super(Report.Major, Report.Minor, Report.Patch, Report.Build, options);
		}


		@Override public Class<Report> type() { return Report.class; }

		@Override public Report item(final Options options) {

			final String endpoint=options.string(Endpoint);

			final String resource=options.string(Resource);
			final String collection=options.string(Collection);
			final String label=options.string(Label);

			final Report report=new Report().setEndpoint(endpoint);

			if ( !resource.isEmpty() ) {
				report.setSpecs(Specs.Resource(resource, label));
			}

			if ( !collection.isEmpty() ) {
				report.setSpecs(Specs.Collection(collection, label));
			}

			return report;
		}
	}

	public static final class Bus extends Tool.Bus<Report, Bus> {

		protected Bus() {}


		public Bus query(final Query query) { return value("query", query, false); }

		public Bus query(final Action<Query> action) { return action("query", action); }


		public Bus sampling(final Boolean sampling) { return value("sampling", sampling, true); }

		public Bus sampling(final Action<Boolean> action) { return action("sampling", action); }


		public Bus slicing(final Boolean slicing) { return value("slicing", slicing, true); }

		public Bus slicing(final Action<Boolean> action) { return action("slicing", action); }

	}

}
