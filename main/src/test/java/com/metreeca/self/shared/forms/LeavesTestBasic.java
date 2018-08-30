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

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.schemas.DC;
import com.metreeca.self.shared.beans.schemas.RDF;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Term.named;


public final class LeavesTestBasic extends LeavesTest {

	//// Projection ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testNoFields() {
		assertEquals("<San Francisco> [] >> / (detail = 3)",

				"select (<tag:metreeca.net;2013:birt/offices/1> as ?root) {}",

				new Leaves()

						.setDetail(3)
						.setSpecs(new Specs()
								.insertPath(new Path()
										.setConstraint(new Options(SanFrancisco)))));

	}


	@Test public void testSingleton() {
		assertEquals("<San Francisco> [/] >> / (detail = 3)",

				"select (<tag:metreeca.net;2013:birt/offices/1> as ?root) {}",

				new Leaves()

						.setDetail(3)
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path()
										.setConstraint(new Options(SanFrancisco)))));
	}

	@Test public void testSingletonLargeFanIn() {
		assertEquals("<OrderLine> [/] >> / (detail = 3)",

				"select (<tag:metreeca.net;2013:birt#OrderLine> as ?root) {}",

				new Leaves()

						.setDetail(3)
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path()
										.setConstraint(new Options(birt("OrderLine"))))));
	}

	@Test public void testCollection() {
		assertEquals("<Employee> [/] >> / (detail = 3)",

				"select ?root { ?root a birt:Employee }",

				new Leaves()

						.setDetail(3)
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDF.RDFType)
										.setConstraint(new Options(birt("Employee"))))));
	}


	@Test public void testIRIsInWellKnownNamespacesAreNotQualified() {
		assertEquals("dc:contributor [/] >> / (detail = 3)",

				"select ?root { filter ( false ) }",

				new Leaves()

						.setDetail(3)
						.setSpecs(new Specs()
								.insertPath(new Path()
										.setConstraint(new Options(named(DC.DC, "contributor"))))));
	}

	@Test public void testHandleMissingIncomingLinks() {
		assertEquals("<Havel & Zbyszek Co> [/] >> / (detail = 3)", // no incoming links

				"select (<tag:metreeca.net;2013:birt/customers/125> as ?root) {}",

				new Leaves()

						.setDetail(3)
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path()
										.setConstraint(new Options(named("tag:metreeca.net;2013:birt/customers/125"))))));

	}

}
