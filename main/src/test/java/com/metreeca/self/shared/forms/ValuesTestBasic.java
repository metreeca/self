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

import com.metreeca.self.shared.beans.Constraint;
import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Path.Transform;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.schemas.XSD;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Term.typed;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class ValuesTestBasic extends ValuesTest {

	//// Projection ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testNoFieldsNoSteps() {
		assertEquals("<Employee> [] >> /",

				"select ?employee ?count where { filter ( false ) }",

				new Values()
						.setPath(new Path())
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testNoFieldsSingleStep() {
		assertEquals("<Employee> [] >> office",

				"select ?employee ?count where { filter ( false ) }",

				new Values()
						.setPath(new Path(birt("office")))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}


	@Test public void testRootFieldNoSteps() {
		assertEquals("<Employee> [@] >> /",

				"select ?employee (count(distinct ?employee) as ?count) where { ?employee a birt:Employee } group by ?employee ",

				new Values()
						.setPath(new Path())
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testRootFieldSingleStep() {
		assertEquals("<Employee> [@] >> office",

				"select ?office (count(distinct ?employee) as ?count) where { ?employee a birt:Employee; birt:office ?office } group by ?office",

				new Values()
						.setPath(new Path(birt("office")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testRootFieldMultipleSteps() {
		assertEquals("<Employee> [/] >> account/country",

				"select ?country (count(distinct ?employee) as ?count) {\n"
						+"\t?employee a birt:Employee optional { ?employee birt:account/birt:country ?country }\n"
						+"} group by ?country",

				new Values()
						.setPath(new Path(birt("account"), birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testRootFieldInverseSteps() {
		assertEquals("<Employee> [/] >> ^representative/country ",

				"select ?country (count(distinct ?employee) as ?count) where {\n"
						+"\n"
						+"\t?employee a birt:Employee \n"
						+"\n"
						+"\toptional { ?employee ^birt:representative/birt:country ?country }\n"
						+"\t\t\n"
						+"} group by ?country",

				new Values()
						.setPath(new Path(birt("representative", true), birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testRootFieldUnknownPath() {
		assertEquals("<Employee> [@] >> nil ",

				"select ?value (count(distinct ?employee) as ?count) where {\n\n\t?employee a <tag:metreeca.net;2013:birt#Employee> .\n\n} \n\ngroup by ?value\n",

				new Values()
						.setPath(new Path(birt("nil")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}


	@Test public void testMultipleFieldsNoSteps() {
		assertEquals("<Employee> [/, account/country] >> /", // account should be traversed but not counted

				"select ?employee (count(*) as ?count) {\n"
						+"\n"
						+"\t{ select distinct ?employee ?country {\n"
						+"\t\t?employee a birt:Employee\n"
						+"\t\toptional { ?employee birt:account/birt:country ?country }\n"
						+"\t} }\n"
						+"\t\t\n"
						+"} group by ?employee",

				new Values()
						.setPath(new Path())
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))));
	}

	@Test public void testMultipleFieldsSingleStep() {
		assertEquals("<Employee> [/, account/country] >> office", // account should be traversed but not counted

				"select ?office (count(*) as ?count) {\n"
						+"\n"
						+"\t{ select distinct ?employee ?country {\n"
						+"\t\t?employee a birt:Employee\n"
						+"\t\toptional { ?employee birt:account/birt:country ?country }\n"
						+"\t} }\n"
						+"\t\n"
						+"\toptional { ?employee birt:office ?office }\n"
						+"\t\n"
						+"} group by ?office",

				new Values()
						.setPath(new Path(birt("office")))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))));
	}


	@Test public void testOuterPath() {
		assertEquals("<Employee> [/, office] >> office",

				"select ?office (count(distinct ?employee) as ?count) where {\n"
						+"\n"
						+"\t?employee a birt:Employee; birt:office ?office.\n"
						+"\t\t\t\n"
						+"} group by ?office ",

				new Values()
						.setPath(new Path(birt("office")))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("office")))));
	}

	@Test public void testInnerPath() {
		assertEquals("<Employee> [/] >> office",

				"select ?office (count(distinct *) as ?count) where {\n\n\t?employee a birt:Employee; birt:office ?office.\n\t\t\t\n} \n\ngroup by ?office\n",

				new Values()
						.setPath(new Path(birt("office")))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())));
	}


	//// Filtering /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testLabelFiltering() {
		assertEquals("<Product> [@] | label(ferrari) >> customer/country",

				"select ?country (count(distinct ?product) as ?count) where {\n\n\t?product a birt:Product; rdfs:label ?label filter contains(?label, 'Ferrari')\n\t\n\toptional {\n\t\t?product birt:customer/birt:country ?country\n\t}\n\t\t\n} \n\ngroup by ?country\n",

				new Values()
						.setPath(new Path(birt("customer"), birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Product"))))
								.setPattern("ferrari")
								.insertPath(new Path())));
	}

	@Test public void testOuterPathWithExistentialFilter() {
		assertEquals("<Customer> [/] | present(order) >> country",

				"select ?country (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t?customer a birt:Customer\n"
						+"\n"
						+"\tfilter exists { ?customer birt:order ?order }\n"
						+"\n"
						+"\toptional { ?customer birt:country ?country }\n"
						+"\t\n"
						+"} group by ?country\n",

				new Values()
						.setPath(new Path(birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(birt("order"))
										.setExisting(true))));
	}

	@Test public void testOuterPathWithNonExistentialFilter() {
		assertEquals("<Customer> [/] | missing(order) >> representative",

				"select ?representative (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t?customer a birt:Customer\n"
						+"\n"
						+"\tfilter not exists { ?customer birt:order ?order }\n"
						+"\n"
						+"\toptional { ?customer birt:representative ?representative }\n"
						+"\n"
						+"} group by ?representative\n",

				new Values()
						.setPath(new Path(birt("representative")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(birt("order"))
										.setExisting(false))));
	}

	@Test public void testInnerPathWithExistentialFilter() {
		assertEquals("<Customer> [representative] | present(order) >> country",

				"select ?country (count(*) as ?count) where {\n"
						+"\n"
						+"\t{ select distinct ?representative ?country {\n"
						+"\n"
						+"\t\t?customer a birt:Customer\n"
						+"\n"
						+"\t\tfilter exists { ?customer birt:order ?order }\n"
						+"\n"
						+"\t\toptional { ?customer birt:representative ?representative }\n"
						+"\t\toptional { ?customer birt:country ?country }\n"
						+"\n"
						+"\t} }\n"
						+"\n"
						+"} group by ?country\n",

				new Values()
						.setPath(new Path(birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("representative")))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(birt("order"))
										.setExisting(true))));
	}

	@Test public void testInnerPathWithNonExistentialFilter() {
		assertEquals("<Customer> [representative] | present(order) >> country",

				"select ?country (count(*) as ?count) where {\n"
						+"\n"
						+"\t{ select distinct ?representative ?country {\n"
						+"\n"
						+"\t\t?customer a birt:Customer\n"
						+"\n"
						+"\t\tfilter not exists { ?customer birt:order ?order }\n"
						+"\n"
						+"\t\toptional { ?customer birt:representative ?representative }\n"
						+"\t\toptional { ?customer birt:country ?country }\n"
						+"\n"
						+"\t} }\n"
						+"\n"
						+"} group by ?country\n",

				new Values()
						.setPath(new Path(birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("representative")))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(birt("order"))
										.setExisting(false))));
	}

	@Test public void testFromRootWithExistentialFilter() {
		assertEquals("<Customer> [@] | present(order) >> country",

				"select ?country (count(distinct *) as ?count) where {\n\n\t?customer a birt:Customer; \n\t\tbirt:country ?country.\n\t\n\tfilter exists { ?customer birt:order ?order }\n\t\t\n} \n\ngroup by ?country\n",

				new Values()
						.setPath(new Path(birt("country")))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Customer"))))
								.insertPath(new Path(birt("order"))
										.setExisting(true))
								.insertPath(new Path())));
	}

	@Test public void testValuesForTransformedFacetWithFilterOnRawFacet() {
		assertEquals("<Order> [/] | received >= '2004-01-01' >> year(received)",

				"select ?year (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t?order a birt:Order; birt:received ?received.\n"
						+"\n"
						+"\tbind (year(?received) as ?year)\n"
						+"\n"
						+"\tfilter ( ?received >= '2004-01-01T00:00:00Z'^^xsd:dateTime)\n"
						+"\n"
						+"} group by ?year",

				new Values()

						.setPath(new Path(birt("received")).setTransform(Transform.Year))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Order"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("received"))
										.setConstraint(new Constraint.Range(typed("2004-01-01T00:00:00Z", XSD.XSDDateTime), null)))));
	}

	@Test public void testOmitAnnotationsForPathsWithRangeConstraints() {
		assertEquals("<Product> [/, buy] | buy >= 100  >> buy",

				"select ?buy (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t?product a birt:Product; birt:buy ?buy.\n"
						+"\n"
						+"\tfilter ( ?buy >= 100 )\n"
						+"\n"
						+"} group by ?buy",

				new Values()

						.setPath(new Path(birt("buy")))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Product"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("buy")))
								.insertPath(new Path(birt("buy"))
										.setConstraint(new Constraint.Range(typed(100), null)))));
	}

	@Test public void testEmptyGroupsAreNotReported() {
		assertEquals("<Product> [/] | buy >= 1000  >> /", // empty set

				"select ?product (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t?product a birt:Product; birt:buy ?buy.\n"
						+"\n"
						+"\tfilter ( ?buy >= 1000 )\n"
						+"\n"
						+"} group by ?product having (count(distinct *) > 0)",

				new Values()

						.setPath(new Path())

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Product"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("buy"))
										.setConstraint(new Constraint.Range(typed(1000), null)))));
	}

}
