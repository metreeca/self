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

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.beans.*;
import com.metreeca.self.shared.beans.Constraint.Options;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Constraint.Range;
import static com.metreeca.self.shared.beans.Term.*;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;
import static com.metreeca.self.shared.beans.schemas.RDFS.RDFSLabel;


public final class ShootsTestBasic extends ShootsTest {

	//// Projecting ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testNoFields() {
		assertEquals("<Employee> [] >> /",

				"select ?root { ?root a birt:Employee }",

				new Shoots().setSpecs(new Specs()

						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testRootField() {
		assertEquals("<Employee> [/, office, subordinate] >> /",

				"select ?root { ?root a birt:Employee }",

				new Shoots().setSpecs(new Specs()

						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("office")))
						.insertPath(new Path(birt("subordinate")))));
	}

	@Test public void testPlainFields() {
		assertEquals("<Employee> [office, subordinate] >> /",

				"select ?root { ?root a birt:Employee }",

				new Shoots().setSpecs(new Specs()

						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("office")))
						.insertPath(new Path(birt("subordinate")))));
	}


	@Test public void testSingletonNoSteps() {
		assertEquals("<SF> [/] >> /",

				"select (<tag:metreeca.net;2013:birt/offices/1> as ?root) {}",

				new Shoots()

						.setPath()
						.setSpecs(new Specs()
								.insertPath(new Path().setConstraint(new Options(named("tag:metreeca.net;2013:birt/offices/1"))))
								.insertPath(new Path())));
	}

	@Test public void testSingletonNoStepsII() {
		assertEquals("<Diane Murphy> [/] >> /", // president > no supervisor

				"select (<tag:metreeca.net;2013:birt/employees/1002> as ?root) {}",

				new Shoots()

						.setPath()
						.setSpecs(new Specs()
								.insertPath(new Path().setConstraint(new Options(named("tag:metreeca.net;2013:birt/employees/1002"))))
								.insertPath(new Path())));
	}

	@Test public void testSingletonSingleStep() {
		assertEquals("<SF> [/] >> employee",

				"select ?root { <tag:metreeca.net;2013:birt/offices/1> birt:employee ?root }",

				new Shoots()

						.setPath(birt("employee"))
						.setSpecs(new Specs()
								.insertPath(new Path().setConstraint(new Options(named("tag:metreeca.net;2013:birt/offices/1"))))
								.insertPath(new Path())));
	}

	@Test public void testSingletonSingleStepII() {
		assertEquals("<Diane Murphy> [/] >> supervisor", // president > no supervisor

				"select ?root { <tag:metreeca.net;2013:birt/employees/1002> birt:supervisor ?root }",

				new Shoots()

						.setPath(birt("supervisor"))
						.setSpecs(new Specs()
								.insertPath(new Path().setConstraint(new Options(named("tag:metreeca.net;2013:birt/employees/1002"))))
								.insertPath(new Path())));
	}

	@Test public void testSingletonMultipleSteps() {
		assertEquals("<SF> [/] >> employee/account",

				"select ?root { <tag:metreeca.net;2013:birt/offices/1> birt:employee/birt:account ?root }",

				new Shoots()

						.setPath(birt("employee"), birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path().setConstraint(new Options(named("tag:metreeca.net;2013:birt/offices/1"))))
								.insertPath(new Path())));
	}

	@Test public void testSingletonMultipleStepsII() {
		assertEquals("<Diane Murphy> [/] >> supervisor/account", // president > no supervisor

				"select ?root { <tag:metreeca.net;2013:birt/employees/1002> birt:supervisor/birt:account ?root }",

				new Shoots()

						.setPath(birt("supervisor"), birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path().setConstraint(new Options(named("tag:metreeca.net;2013:birt/employees/1002"))))
								.insertPath(new Path())));
	}


	@Test public void testCollectionNoSteps() {
		assertEquals("<Employee> [] >> /",

				"select ?root { ?root a birt:Employee }",

				new Shoots()

						.setPath()
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())));
	}

	@Test public void testCollectionSingleStep() {
		assertEquals("<Employee> [/] >> office",

				"select ?root { [birt:office ?root] a birt:Employee }",

				new Shoots()

						.setPath(birt("office"))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testCollectionMultipleSteps() {
		assertEquals("<Employee> [/] >> office/city",

				"select ?root { [birt:office/birt:city ?root] a birt:Employee }",

				new Shoots()

						.setPath(birt("office"), birt("city"))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}


	//// Filtering /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testFacetFilter() {
		assertEquals("<Order> [] | amount > 1.000.000 >> customer", // empty set

				"select ?root { ?order a birt:Order; birt:amount ?amount; birt:customer ?root filter ( ?amount > 1000000 ) }",

				new Shoots()

						.setPath(birt("customer"))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Order"))))
								.insertPath(new Path(birt("amount"))
										.setConstraint(new Range(typed(1_000_000), null)))));
	}

	@Test public void testTextualFilter() {
		assertEquals("<Employee> [/] | ~'Diane Murphy'>> /", // no supervisor

				"select ?root { ?root a birt:Employee; rdfs:label 'Diane Murphy' }",

				new Shoots()

						.setSpecs(new Specs()
								.setPattern("Diane Murphy")
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testInnerPathWithNonExistentialFilter() {
		assertEquals("<Customer> [/] | missing(representative) >> order", // empty set

				"select ?root { ?customer a birt:Customer; birt:order ?root filter not exists { ?customer birt:representative [] } }",

				new Shoots()

						.setPath(birt("order"))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(birt("representative"))
										.setExisting(false))));
	}

	@Test public void testFromRootWithExistentialFilter() { // !!! look for a set where present(something) actually alters shoots
		assertEquals("<Porto Imports Co.> [/] | present(order) >> country",

				"select ?root { ?customer a birt:Customer; rdfs:label 'Porto Imports Co.'; birt:country ?root filter exists { ?customer birt:order ?order } }",

				new Shoots()

						.setPath(birt("country"))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(RDFSLabel)
										.setConstraint(new Options(plain("Porto Imports Co."))))
								.insertPath(new Path(birt("order"))
										.setExisting(true))
								.insertPath(new Path())));
	}

	@Test public void testOmitAnnotationsForPathsWithRangeConstraints() {
		assertEquals("<Product> [/, buy] | buy >= 100  >> buy",

				"select (?buy as ?root) where {\n"
						+"\n"
						+"\t?product a birt:Product; birt:buy ?buy.\n"
						+"\n"
						+"\tfilter ( ?buy >= 100 )\n"
						+"\n"
						+"}",

				new Shoots()

						.setPath(birt("buy"))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Product"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("buy")))
								.insertPath(new Path(birt("buy"))
										.setConstraint(new Constraint.Range(typed(100), null)))));
	}

}
