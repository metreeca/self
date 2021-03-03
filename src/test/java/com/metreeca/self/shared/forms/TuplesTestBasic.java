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
import com.metreeca.self.shared.beans.Constraint.Exclude;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Constraint.Options;
import static com.metreeca.self.shared.beans.Term.*;
import static com.metreeca.self.shared.beans.schemas.OWL.OWLClass;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;
import static com.metreeca.self.shared.beans.schemas.RDFS.RDFSLabel;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


public final class TuplesTestBasic extends TuplesTest {

	//// Projection ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testNoFields() {
		assertEquals("<Employee> []",

				"select * { filter ( false ) }",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testRootField() {
		assertEquals("<Employee> [@]",

				"select distinct ?employee where { ?employee a birt:Employee. }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())));
	}

	@Test public void testSingleField() {
		assertEquals("<Employee> [office]",

				"select distinct ?office where { ?employee a birt:Employee; birt:office ?office. }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("office")))));
	}

	@Test public void testMultipleFields() {
		assertEquals("<Employee> [office, code]",

				"select distinct ?office ?code where { ?employee a birt:Employee; birt:office ?office; birt:code ?code. }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("office")))
						.insertPath(new Path(birt("code")))));
	}

	@Test public void testRepeatedField() {
		assertEquals("<Employee> [office, office]",

				"select distinct ?office (?office as ?office2) where { ?employee a birt:Employee; birt:office ?office. }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("office")))
						.insertPath(new Path(birt("office")))));
	}

	@Test public void testSharedFieldPaths() {
		assertEquals("<Employee> [account > city, account > country]",

				"select distinct ?city ?country {\n"
						+"\n"
						+"\t?employee a birt:Employee\n"
						+"\n"
						+"\toptional { ?employee birt:account [birt:city ?city; birt:country ?country] }\n"
						+"\n"
						+"}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("account"), birt("city")))
						.insertPath(new Path(birt("account"), birt("country")))));
	}

	@Test public void testSingleton() {
		assertEquals("<SF> [/]",

				"select distinct (<tag:metreeca.net;2013:birt/offices/1> as ?sf) {}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path())
						.insertPath(new Path().setConstraint(new Options(SanFrancisco)))));

	}


	//// Filtering /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testSingleFacetSelection() {
		assertEquals("<Employee> [@] | office = SF",

				"select ?employee where { ?employee a birt:Employee; birt:office [rdfs:label ?office] filter (?office = 'San Francisco (USA)' ) } ",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("office")).setConstraint(new Options(SanFrancisco)))));
	}

	@Test public void testSingleRootSelection() {
		assertEquals("<Office> [/] | SF",

				"select (<tag:metreeca.net;2013:birt/offices/1> as ?root) {}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path())
						.insertPath(new Path().setConstraint(new Options(SanFrancisco)))));
	}

	@Test public void testSingleFieldSelection() {
		assertEquals("<Employee> [/, title] | title = 'Sales Rep'",

				"select ?employee ?title where { ?employee a birt:Employee; birt:title ?title filter ( ?title = 'Sales Rep' ) }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("title")))
						.insertPath(new Path(birt("title"))
								.setConstraint(new Options(plain("Sales Rep"))))));
	}

	@Test public void testMultipleFieldSelection() {
		assertEquals("<Employee> [@, title] | title in {'VP Marketing', 'VP Sales'}",

				"select ?employee ?title where { ?employee a birt:Employee; birt:title ?title filter ( ?title in ('VP Marketing', 'VP Sales') ) }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("title")).setConstraint(new Options(plain("VP Marketing"), plain("VP Sales"))))
						.insertPath(new Path(birt("title")))));
	}

	@Test public void testRequiredFacet() {
		assertEquals("<Employee> [@, subordinate] | required(subordinate)",

				"select distinct ?employee ?subordinate where { ?employee a birt:Employee; birt:subordinate ?subordinate. }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("subordinate")))
						.insertPath(new Path(birt("subordinate"))
								.setExpanded(true)
								.setExisting(true))));
	}

	@Test public void testExcludedWellKnownNamespaces() {
		assertEquals("<owl:Class> [/] | exclude",

				"select ?type { ?type a owl:Class filter (!contains(str(?type), 'www.w3.org')) }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path())
						.insertPath(new Path().setConstraint(new Exclude()))
						.insertPath(new Path(RDFType).setConstraint(new Options(OWLClass)))));
	}

	@Test public void testOmitAnnotationsForFieldsWithRangeConstraints() {

		// ;(virtuoso) unable to retrieve missing labels after numerical filter (anyway, a good optimization)

		assertEquals("<Product> [/, buy] | buy >= 100",

				"select distinct ?product ?buy where { ?product a birt:Product; birt:buy ?buy filter ( ?buy >= 100 ) }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("buy")))
						.insertPath(new Path(birt("buy"))
								.setConstraint(new Constraint.Range(typed(100), null)))));
	}

	@Test public void testHandleMultiValuedOptionsOnRoot() {
		assertEquals("<Office> [/] | / in (office1, office2)",

				"select ?office where { ?office a birt:Office; birt:code ?code filter ( ?code in ('1', '2') ) }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path())
						.insertPath(new Path()
								.setConstraint(new Options(
										named("tag:metreeca.net;2013:birt/offices/1"),
										named("tag:metreeca.net;2013:birt/offices/2"))))));
	}

	@Test public void testFilteringOnRootWithMultipleFields() {
		assertEquals("<Employee> [/, code] | / = <tag:metreeca.net;2013:birt/employees/1611>",

				"select ?employee ?code { \n"
						+" \n"
						+"\tbind (<tag:metreeca.net;2013:birt/employees/1611> as ?employee)\n"
						+"\t\n"
						+"\t?employee birt:code ?code\n"
						+"\t\n"
						+" }",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Employee"))))

						.insertPath(new Path())
						.insertPath(new Path(birt("code")))

						.insertPath(new Path()
								.setConstraint(new Options(named("tag:metreeca.net;2013:birt/employees/1611"))))));

	}

	@Test public void testConstraintWithInverseTerm() {
		assertEquals("<Employee> [@, title] | @ = ^birt:Employee",

				"select ?0 ?0_label where { values ?0 { birt:Employee } optional { ?0 rdfs:label ?0_label } }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path())
						.insertPath(new Path(RDFSLabel))
						.insertPath(new Path().setConstraint(new Options(birt("Employee").reverse())))
				)

		);
	}


	//// Binding ///////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testFieldsOptionalByDefault() {
		assertEquals("<Employee> [@, supervisor]",

				"select distinct ?employee ?supervisor where { ?employee a birt:Employee\n\t \n\t optional { ?employee birt:supervisor ?supervisor }\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("supervisor")))));
	}

	@Test public void testNestedRequiredPaths() {
		assertEquals("<Employee> [@, account > name (required) , account > country]",

				"select distinct ?employee ?name ?country { ?employee a birt:Employee; birt:account [birt:name ?name; birt:country ?country]\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account"), birt("name")).setExisting(TRUE))
						.insertPath(new Path(birt("account"), birt("country")))));
	}

	@Test public void testBothRequiredAndForbidden() {
		assertEquals("<Employee> [@, supervisor (required), supervisor (forbidden)]",

				"select distinct ?employee ?supervisor_present ?supervisor_absent where { filter not exists {}\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("supervisor")).setExisting(TRUE))
						.insertPath(new Path(birt("supervisor")).setExisting(FALSE))));
	}

	@Test public void testPresentWithEmptyOptions() {
		assertEquals("<Customer> [@] | present(order)",

				"select distinct ?customer where { ?customer a birt:Customer filter exists { ?customer birt:order ?order }\n\t \t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Customer"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("order"))
								.setExisting(true)
								.setConstraint(new Options()))));
	}

	@Test public void testMissingWithEmptyOptions() {
		assertEquals("<Customer> [@] | missing(order)",

				"select distinct ?customer where { ?customer a birt:Customer filter not exists { ?customer birt:order ?order }\n\t \t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Customer"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("order"))
								.setExisting(false)
								.setConstraint(new Options()))));
	}


	//// Matching //////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testMatchRootLabel() {
		assertEquals("<Employee> [@]| 'firrelli'",

				"select distinct ?employee where { ?employee a birt:Employee; \n\t\trdfs:label ?employee_label filter contains(?employee_label, 'Firrelli').\n\t }",

				new Tuples().setSpecs(new Specs()
						.setPattern("firrelli")
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
				));
	}

	@Test public void testMatchFieldsLabel() {
		assertEquals("<Customer> [@, representative] | 'firrelli'",

				"select distinct ?customer ?representative where {\n"
						+"\n"
						+"\t?customer a birt:Customer; birt:representative ?representative.\n"
						+"\n"
						+"\t?representative\trdfs:label ?label filter contains(?label, 'Firrelli').\n"
						+"\n"
						+"}",

				new Tuples().setSpecs(new Specs()
						.setPattern("firrelli")
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Customer"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("representative")))));
	}

	@Test public void testMatchAfterNonWordCharacter() {
		assertEquals("<Product> [/, code] | '1949'",

				"select distinct ?product ?code where { \n"
						+"\n"
						+"\t?product a birt:Product; rdfs:label ?label; birt:code ?code.\n"
						+"\n"
						+"\tfilter ( contains(?label, '1949') || contains(?code, '1949') )\n"
						+"\n"
						+"}",

				new Tuples().setSpecs(new Specs()
						.setPattern("1949")
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("code")))));
	}


	//// Annotation ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testLabelsNotesAndImages() {
		assertEquals("<ProductLine> [@]",

				"select ?line { ?line a birt:ProductLine }",

				new Tuples()
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("ProductLine"))))));
	}

	@Test public void testCoords() {
		assertEquals("<Office> [city]",

				"select distinct ?city { ?office a birt:Office; birt:city ?city }",

				new Tuples()

						.setPoint(true)
						.setSpecs(new Specs()
								.insertPath(new Path(birt("city")))
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Office"))))));
	}


	//// Sorting ///////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testSortAscending() {
		assertEquals("<Employee> [/, office (asc)]",

				"select ?employee ?office { ?employee a birt:Employee; birt:office ?office }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("office")).setSort(1)))

		);
	}

	@Test public void testSortDescending() {
		assertEquals("<Employee> [/, office (desc)]",

				"select ?employee ?office where { ?employee a birt:Employee ; birt:office ?office }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("office")).setSort(-1)))

		);
	}

	@Test public void testSortDiacritics() {
		assertEquals("<Customer> [@]",

				"select ?customer where { ?customer a birt:Customer }",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path())
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Customer"))))));
	}

}
