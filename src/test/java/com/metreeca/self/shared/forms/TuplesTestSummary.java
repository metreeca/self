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
import static com.metreeca.self.shared.beans.Path.Summary.*;
import static com.metreeca.self.shared.beans.Path.Transform.Year;
import static com.metreeca.self.shared.beans.Term.typed;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class TuplesTestSummary extends TuplesTest {

	//// Aggregation ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testAggregatedField() {
		assertEquals("<Employee> [/, count(account)]",

				"select distinct ?employee (count(?account) as ?accounts) {\n\n\t?employee a birt:Employee optional { ?employee birt:account ?account }\n\t\n} group by ?employee",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account")).setSummary(Count))));
	}

	@Test public void testDisaggregatedField() {
		assertEquals("<Employee> [/, account > country, count(account)]",

				"select distinct ?employee ?country (count(?account) as ?accounts) {\n"
						+"\n"
						+"\t?employee a birt:Employee \n"
						+"\n"
						+"\toptional { ?employee birt:account ?account. ?account birt:country ?country }\n"
						+"\n"
						+"} group by ?employee ?country",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account"), birt("country")))
						.insertPath(new Path(birt("account")).setSummary(Count))));
	}

	@Test public void testTotalSummary() {
		assertEquals("<Employee> [count(account)]",

				"select distinct (count(?account) as ?accounts) {\n\n\t?employee a birt:Employee optional { ?employee birt:account ?account }\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("account")).setSummary(Count))));
	}

	@Test public void testTotalDisaggregatedSummary() {
		assertEquals("<Employee> [count(account), account > country]",

				"select distinct (count(?account) as ?accounts) ?country {\n\n\t?employee a birt:Employee \n\t\n\toptional { ?employee birt:account ?account. ?account birt:country ?country }\n\t\n} group by ?country",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path(birt("account")).setSummary(Count))
						.insertPath(new Path(birt("account"), birt("country")))));
	}


	//// Filtering /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testInnerFiltering() {
		assertEquals("<Employee> [Employee, count(account)] | year(account > order > received) = 2003", // active accounts in 2003

				"select ?employee (count(distinct ?account) as ?accounts) {\n\t\n\t?employee a birt:Employee; birt:account ?account.\n\t\n\t?account birt:order/birt:received ?received\n\t\n\tfilter ( year(?received) = 2003 )\n\t\t\n} \n\t\ngroup by ?employee",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account")).setSummary(Count))
						.insertPath(new Path(birt("account"), birt("order"), birt("received"))
								.setTransform(Year)
								.setConstraint(new Options(integer(2003))))));
	}

	@Test public void testOuterFiltering() {
		assertEquals("<Employee> [Employee, count(account)] | count(order) > 20",

				"select ?employee ?accounts where {\n\n\t{ select ?employee (count(distinct ?account) as ?accounts) (count(distinct ?order) as ?orders) {\n\t\n\t\t?employee a birt:Employee; birt:account ?account.\n\t\t\n\t\t?account birt:order ?order\n\t\t\n\t} \n\t\n\tgroup by ?employee having (?orders >= 20) }\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account")).setSummary(Count))
						.insertPath(new Path(birt("account"), birt("order"))
								.setSummary(Count)
								.setConstraint(new Range(integer(20), null)))));
	}

	@Test public void testDoubleFiltering() {
		assertEquals("<Employee> [Employee, count(account)] | year(order > received) = 2003 and count(order) > 10",

				"select ?employee ?accounts where {\n\n\t{ select ?employee (count(distinct ?account) as ?accounts) (count(distinct ?order) as ?orders) {\n\t\n\t\t?employee a birt:Employee; birt:account ?account.\n\t\t\n\t\t?account birt:order ?order.\n\t\t?order birt:received ?received.\n\t\t\n\t\tfilter ( year(?received) = 2003 )\n\t\t\n\t} \n\t\n\tgroup by ?employee having (?orders >= 10) }\n\t\n}",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account")).setSummary(Count))
						.insertPath(new Path(birt("account"), birt("order"), birt("received"))
								.setTransform(Year)
								.setConstraint(new Options(integer(2003))))
						.insertPath(new Path(birt("account"), birt("order"))
								.setSummary(Count)
								.setConstraint(new Range(typed(10), null)))));
	}


	//// Summaries /////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testCountDistinctSummary() {
		assertEquals("<Employee> [Employee, count(account > country)]",

				"select ?employee (count(distinct ?country) as ?countries) where {\n"
						+"\n"
						+"\t?employee a birt:Employee\n"
						+"\n"
						+"\toptional { ?employee birt:account [birt:country ?country] }\n"
						+"\n"
						+"} group by ?employee ",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("account"), birt("country")).setSummary(Count))));
	}

	@Test public void testSumNotDistinctSummary() {
		assertEquals("<ProductLine> [/, sum(buy)]",

				"select ?line (sum(?buy) as ?buys) where {\n"
						+"\n"
						+"\t?line a birt:ProductLine; birt:product/birt:buy ?buy\n"
						+"\n"
						+"} group by ?line",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("ProductLine"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("product"), birt("buy")).setSummary(Sum))));
	}

	@Test public void testAvgNotDistinctSummary() {
		assertEquals("<ProductLine> [/, avg(buy)]",

				"select ?line (avg(?buy) as ?buys) where {\n"
						+"\n"
						+"\t?line a birt:ProductLine; birt:product/birt:buy ?buy\n"
						+"\n"
						+"} group by ?line",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("ProductLine"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("product"), birt("buy")).setSummary(Avg))));
	}

	@Test public void testMinSummary() {
		assertEquals("<ProductLine> [/, min(buy)]",

				"select ?line (min(?buy) as ?buys) where {\n"
						+"\n"
						+"\t?line a birt:ProductLine; birt:product/birt:buy ?buy\n"
						+"\n"
						+"} group by ?line",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("ProductLine"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("product"), birt("buy")).setSummary(Min))));
	}

	@Test public void testMaxSummary() {
		assertEquals("<ProductLine> [/, max(buy)]",

				"select ?line (max(?buy) as ?buys) where {\n"
						+"\n"
						+"\t?line a birt:ProductLine; birt:product/birt:buy ?buy\n"
						+"\n"
						+"} group by ?line",

				new Tuples().setSpecs(new Specs()
						.insertPath(new Path(RDFType).setConstraint(new Options(birt("ProductLine"))))
						.insertPath(new Path())
						.insertPath(new Path(birt("product"), birt("buy")).setSummary(Max))));
	}


	@Test public void testSummaryWithInversePaths() {
		assertEquals("<Customer> [representative, count(/), representative/office]",

				"select ?representative ?office (count(?customer) as ?customers) {\n"
						+"\n"
						+"\t?representative a birt:Employee;\n"
						+"\t\tbirt:office ?office;\n"
						+" \t\tbirt:account ?customer.\n"
						+"\n"
						+"} group by ?representative ?office",

				new Tuples().setSpecs(new Specs()

						.insertPath(new Path())
						.insertPath(new Path(birt("office")))
						.insertPath(new Path(birt("representative", true))
								.setSummary(Path.Summary.Count))

						.insertPath(new Path(birt("representative", true), RDFType)
								.setConstraint(new Options(birt("Customer"))))));
	}

}
