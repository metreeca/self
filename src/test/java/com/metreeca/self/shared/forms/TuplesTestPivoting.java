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

import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Constraint.Range;
import static com.metreeca.self.shared.beans.Path.Summary.Count;
import static com.metreeca.self.shared.beans.Path.Summary.Sum;
import static com.metreeca.self.shared.beans.Path.Transform.Year;
import static com.metreeca.self.shared.beans.Term.*;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class TuplesTestPivoting extends TuplesTest {

	//// Anchors ///////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testTypeAnchor() {
		assertEquals("<Employee> [@]",

				"select ?employee where {\n\n\t?employee a birt:Employee.\n\t\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())));
	}

	@Test public void testTermAnchor() {
		assertEquals("<SF> [@]",

				"select distinct ?office where { \n\n\t?office a birt:Office; rdfs:label ?label filter (?label = 'San Francisco (USA)' )\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path().setConstraint(new Options(SanFrancisco)))
						.insertPath(new Path())));
	}

	@Test public void testLiteralAnchor() {
		assertEquals("<'San francisco (USA)'> [@]",

				"select distinct ('San Francisco (USA)' as ?city) where { }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path().setConstraint(new Options(plain("San Francisco (USA)"))))
						.insertPath(new Path())));
	}

	@Test public void testLinkAnchor() {
		assertEquals("<^employee = SF> [@]",

				"select distinct ?employee where { \n\n\t?office a birt:Office; birt:employee ?employee; rdfs:label ?label filter (?label = 'San Francisco (USA)' )\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(birt("employee", true)).setConstraint(new Options(SanFrancisco)))
						.insertPath(new Path())));
	}


	//// Chaining ///////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testFromTerm() {
		assertEquals("<SF> [@] >> employee [@]",

				"select distinct ?employee where { \n\n\t?office a birt:Office; birt:employee ?employee; rdfs:label ?label filter (?label = 'San Francisco (USA)' )\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path().setConstraint(new Options(SanFrancisco)))
						.insertPath(new Path())
						.open(new Path(birt("employee")))
						.insertPath(new Path())));
	}

	@Test public void testFromCollection() {
		assertEquals("<Office> [@] >> employee [@]",

				"select distinct ?employee where { \n\n\t?office a birt:Office; birt:employee ?employee\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Office"))))
						.insertPath(new Path())
						.open(new Path(birt("employee")))
						.insertPath(new Path())));
	}

	@Test public void testFromRootOnFilteredPath() {
		assertEquals("<Office> [@] | employee = 'Gerard Hernandez' >> employee [@]",

				"select distinct ?employee where { \n\n\t?office a birt:Office; birt:employee ?employee, [rdfs:label 'Gerard Hernandez'].\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Office"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("employee")).setConstraint(new Options(named("tag:metreeca.net;2013:birt/employees/1370"))))
						.open(new Path(birt("employee")))
						.insertPath(new Path())));
	}

	@Test public void testFromRootWithAggregate() {
		assertEquals("<Office> [@, count(employee)] >> employee [@]",

				"select distinct ?employee where { \n\n\t?office a birt:Office; birt:employee ?employee.\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Office"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("employee"))
								.setSummary(Count))
						.open(new Path(birt("employee")))
						.insertPath(new Path())));
	}

	@Test public void testFromRootWithFilteredAggregate() {
		assertEquals("<Office> [@] | count(employee) > 3 >> employee [@]",

				"select distinct ?employee where { \n\n\t{ select ?office (count(?employee) as ?employees) where {\n\t\t?office a birt:Office; birt:employee ?employee.\n\t} group by ?office having (?employees >= 3 ) }\n\n\t?office birt:employee ?employee\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Office"))))
						.insertPath(new Path(birt("employee"))
								.setSummary(Count)
								.setConstraint(new Range(typed(3), null)))
						.insertPath(new Path())
						.open(new Path(birt("employee")))
						.insertPath(new Path())));
	}

	@Test public void testFromRootWithFilteredAggregateAndFilteredTransform() {
		assertEquals("<Customer> [@] | sum(order/amount) >= 100K && year(order/received) == 2004 >> representative [@]",

				"select distinct ?representative where { \n\n\t{ select distinct ?customer (sum(?amount) as ?sales) where {\n\t\n\t\t?customer a birt:Customer;\n\t\t\tbirt:order [\n\t\t\t\tbirt:amount ?amount;\n\t\t\t\tbirt:received ?received].\n\t\t\t\t\n\t\tfilter ( year(?received) = 2004 )\n\n\t} group by ?customer having ( ?sales >= 100000 ) }\n\t\n\t?customer birt:representative ?representative.\n\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Customer"))))
						.insertPath(new Path(birt("order"), birt("amount"))
								.setSummary(Sum)
								.setConstraint(new Range(integer(100000), null)))
						.insertPath(new Path(birt("order"), birt("received"))
								.setTransform(Year)
								.setConstraint(new Range(integer(2004), integer(2004))))
						.insertPath(new Path())
						.open(new Path(birt("representative")))
						.insertPath(new Path())));
	}

	@Test public void testFromRootWithKeywords() {
		assertEquals("<Product> [@] | pattern(ferrari) >> customer [@]",

				"select distinct ?customer where {\n\n\t?product a birt:Product; rdfs:label ?label; birt:customer ?customer filter contains(?label, 'Ferrari')\n\n}",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Product"))))
						.setPattern("ferrari")
						.insertPath(new Path())
						.open(new Path(birt("customer")))
						.insertPath(new Path())));
	}

	@Test public void testFromAggregateField() {
		assertEquals("<Office> [count(employee), /] >> / [/]",

				"select distinct ?employees {\n"
						+"\n"
						+"\t{ select distinct ?office (count(distinct ?employee) as ?employees) where {\n"
						+"\n"
						+"\t\t?office a birt:Office; birt:employee ?employee.\n"
						+"\n"
						+"\t} group by ?office }\n"
						+"\n"
						+"}",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Office"))))
						.insertPath(new Path(birt("employee")).setSummary(Count))
						.insertPath(new Path())

						.open(new Path())

						.insertPath(new Path())));
	}

	@Test public void testFromDerivateField() {
		assertEquals("<Order> [year(received), /] >> / [/]",

				"select distinct (year(?received) as ?year) { ?order a birt:Order; birt:received ?received }",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Order"))))
						.insertPath(new Path(birt("received"))
								.setTransform(Year))
						.insertPath(new Path())

						.open(new Path())

						.insertPath(new Path())));
	}

	@Test public void testFromRootWithNonExistentialFilter() {
		assertEquals("<Customer> [@] | missing(order) >> representative [@]",

				"select distinct ?representative {\n\n\t?customer a birt:Customer; \n\t\tbirt:representative ?representative.\n\t\t\n\tfilter not exists { ?customer birt:order ?order }\n     \n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Customer"))))
						.insertPath(new Path(birt("order"))
								.setExisting(false))
						.insertPath(new Path())
						.open(new Path(birt("representative")))
						.insertPath(new Path())));
	}

	@Test public void testFromRootWithExistentialFilter() {
		assertEquals("<Customer> [@] | present(order) >> country [@]",

				"select distinct ?country {\n\n\t?customer a birt:Customer; \n\t\tbirt:country ?country.\n\t\t\n\tfilter exists { ?customer birt:order ?order }\n     \n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType)
								.setConstraint(new Options(birt("Customer"))))
						.insertPath(new Path(birt("order"))
								.setExisting(true))
						.insertPath(new Path())
						.open(new Path(birt("country")))
						.insertPath(new Path())));
	}

}
