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

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.Constraint.Range;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Path.Summary.Count;
import static com.metreeca.self.shared.beans.Path.Summary.Min;
import static com.metreeca.self.shared.beans.Term.typed;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class ShootsTestSummary extends ShootsTest {

	//// Projecting ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testAggregateField() {
		assertEquals("<Employee> [min(account)] >> /",

				"select (?employee as ?root) { ?employee a birt:Employee }",

				new Shoots().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("account"))
								.setSummary(Min))));
	}


	//// Filtering /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testAggregateFilters() {
		assertEquals("<Office> [area] | count(employee) >= 100 >> employee", // empty set

				// aggregate filter not exposing target path or root >> ignore it

				"select ?root { ?office a birt:Office; birt:employee ?root }",

				new Shoots()

						.setPath(birt("employee"))

						.setSpecs(new Specs()

								.insertPath(new Path(birt("area")))

								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Office"))))
								.insertPath(new Path(birt("employee"))
										.setSummary(Count)
										.setConstraint(new Range(typed(100), null)))));
	}

}
