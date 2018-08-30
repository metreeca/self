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

import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Constraint.Options;
import static com.metreeca.self.shared.beans.Path.Transform.*;
import static com.metreeca.self.shared.beans.Path.Transform.Number;
import static com.metreeca.self.shared.beans.Term.typed;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;
import static com.metreeca.self.shared.beans.schemas.XSD.XSDInteger;


public final class TuplesTestTransform extends TuplesTest {

	//// Mapping ///////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testTransformedField() {
		assertEquals("<Order> [Order, year(received)]",

				"select distinct ?order (year(?received) as ?year) {\n\n\t?order a birt:Order; birt:received ?received\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")).setTransform(Year))));

	}

	@Test public void testPlainAndTransformedField() {
		assertEquals("<Order> [Order, received, year(received)]",

				"select distinct ?order ?received (year(?received) as ?year) { ?order a birt:Order; birt:received ?received }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")))
						.insertPath(new Path(birt("received")).setTransform(Year))));

	}

	@Test public void testTransformedAndFilteredField() {
		assertEquals("<Order> [Order, received | year(received) = 2003]",

				"select distinct ?order ?received  {\n\n\t?order a birt:Order; birt:received ?received\n\t\n\tfilter ( year(?received) = 2003 )\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")))
						.insertPath(new Path(birt("received"))
								.setTransform(Year)
								.setConstraint(new Options(typed("2003", XSDInteger))))));

	}

	@Test public void testTransformedFacet() {
		assertEquals("<Order> [Order] | year(received) = 2003",

				"select distinct ?order {\n\n\t?order a birt:Order; birt:received ?received\n\t\n\tfilter ( year(?received) = 2003 )\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received"))
								.setTransform(Year)
								.setConstraint(new Options(typed("2003", XSDInteger))))));

	}


	//// Matching //////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testMatchLexicalFormOfTransformedValue() {
		assertEquals("<Order> [year(received)] | ~'2004'",

				"select distinct ?year {\n"
						+"\n"
						+"\t?order a birt:Order; birt:received ?received\n"
						+"\n"
						+"\tbind (year(?received) as ?year)\n"
						+"\n"
						+"\tfilter (contains(str(?year), '2004'))\n"
						+"\n"
						+"}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path(birt("received")).setTransform(Year))
						.setPattern("2004")));
	}


	//// Temporal Transforms ///////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testYearTransform() {
		assertEquals("<Order> [/, year(received)]",

				"select distinct ?order (year(?received) as ?value) { ?order a birt:Order; birt:received ?received }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")).setTransform(Year))));

	}

	@Test public void testQuarterTransform() {
		assertEquals("<Order> [/, quarter(received)]",

				"select distinct ?order (xsd:integer(ceil(month(?received)/3.0)) as ?value) { ?order a birt:Order; birt:received ?received }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")).setTransform(Quarter))));

	}

	@Test public void testMonthTransform() {
		assertEquals("<Order> [/, month(received)]",

				"select distinct ?order (month(?received) as ?value) { ?order a birt:Order; birt:received ?received }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")).setTransform(Month))));

	}

	@Test public void testDayTransform() {
		assertEquals("<Order> [/, day(received)]",

				"select distinct ?order (day(?received) as ?value) { ?order a birt:Order; birt:received ?received }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("received")).setTransform(Day))));

	}

	//// Numeric Transforms ////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testNumberTransform() {
		assertEquals("<Product> [/, number(?buy)]",

				"select distinct ?product (xsd:decimal(?buy) as ?value) { ?product a birt:Product; birt:buy ?buy }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("buy")).setTransform(Number))));

	}

	@Test public void testAbsTransform() {
		assertEquals("<Product> [/, abs(?buy)]",

				"select distinct ?product (abs(?buy) as ?value) { ?product a birt:Product; birt:buy ?buy }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("buy")).setTransform(Abs))));

	}

	@Test public void testRoundTransform() {
		assertEquals("<Product> [/, round(?buy)]",

				"select distinct ?product (round(?buy) as ?value) { ?product a birt:Product; birt:buy ?buy }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("buy")).setTransform(Round))));

	}

	@Test public void testFloorTransform() {
		assertEquals("<Product> [/, floor(?buy)]",

				"select distinct ?product (floor(?buy) as ?value) { ?product a birt:Product; birt:buy ?buy }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("buy")).setTransform(Floor))));

	}

	@Test public void testCeilTransform() {
		assertEquals("<Product> [/, ceil(?buy)]",

				"select distinct ?product (ceil(?buy) as ?value) { ?product a birt:Product; birt:buy ?buy }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("buy")).setTransform(Ceil))));

	}


	@Test public void testTemporalTransformOnNonTemporalArgs() {
		assertEquals("<Product> [/, year(?line)]", // non-temporal args should be ignored

				"select distinct ?product (year(?line) as ?value) { ?product a birt:Product; birt:line ?line }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("line")).setTransform(Year))));

	}

	@Test public void testNumericTransformOnNonNumericArgs() {
		assertEquals("<Product> [/, abs(?line)]", // non-numeric args should be ignored

				"select distinct ?product (abs(?line) as ?value) { ?product a birt:Product; birt:line ?line }",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("line")).setTransform(Abs))));

	}

}
