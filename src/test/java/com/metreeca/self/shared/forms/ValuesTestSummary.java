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
import static com.metreeca.self.shared.beans.Path.Summary.Sum;
import static com.metreeca.self.shared.beans.Term.typed;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class ValuesTestSummary extends ValuesTest {

	@Test public void testInnerAggregatePath() {
		assertEquals("<Employee> [office] >> count(account)",

				"select ?accounts (count(distinct ?office) as ?count) where {\n"
						+"\n"
						+"\t{ select ?office (count(distinct ?account) as ?accounts) {\n"
						+"\t\t?employee a birt:Employee; birt:office ?office; birt:account ?account.\n"
						+"\t} group by ?office }\n"
						+"\n"
						+"} group by ?accounts",

				new Values()
						.setPath(new Path(birt("account")).setSummary(Count))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("office")))
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))));
	}

	@Test public void testPlainCoreAggregatePath() {
		assertEquals("<Employee> [/] >> count(account)",

				"select ?accounts (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t{ select ?employee (count(distinct ?account) as ?accounts) where {\n"
						+"\t\n"
						+"\t\t?employee a birt:Employee optional { ?employee birt:account ?account }\n"
						+"\t\t\n"
						+"\t} group by ?employee }\n"
						+"\t\n"
						+"} group by ?accounts",

				new Values()
						.setPath(new Path(birt("account")).setSummary(Count))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())));
	}

	@Test public void testOuterAggregatePath() {
		assertEquals("<Employee> [/, account/country, count(account)] >> count(account)",

				"select ?accounts (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t{ select ?employee ?country (count(distinct ?account) as ?accounts) where {\n"
						+"\t\n"
						+"\t\t?employee a birt:Employee.\n"
						+"\t\t\n"
						+"\t\toptional { ?employee birt:account ?account optional { ?account birt:country ?country } }\n"
						+"\t\t\n"
						+"\t} group by ?employee ?country }\n"
						+"\t\n"
						+"} group by ?accounts",

				new Values()
						.setPath(new Path(birt("account")).setSummary(Count))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Count))));
	}

	@Test public void testInnerPathWithAggregateFilters() {
		assertEquals("<Employee> [/] | count(office/employee) >= 3 >> title",

				"select ?title (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t{ select ?employee (count(distinct ?_employee) as ?employees) where {\n"
						+"\n"
						+"\t\t?employee a birt:Employee; birt:office [birt:employee ?_employee]\n"
						+"\n"
						+"\t} group by ?employee having ( ?employees >= 3) }     \n"
						+"\n"
						+"\t?employee a birt:Employee; birt:title ?title\n"
						+"\n"
						+"} group by ?title",

				new Values()

						.setPath(new Path(birt("title")))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("office"), birt("employee"))
										.setSummary(Count)
										.setConstraint(new Range(typed(3), null)))));
	}

	@Test public void testInnerPathWithAggregateFiltersII() {
		assertEquals("<Product> [/] | sum(order/amount) > 1,000,000 >> customer/country",

				"select ?country (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t{ select ?product (sum(?amount) as ?sales) where {\n"
						+"\n"
						+"\t\t?product a birt:Product optional { ?product birt:order/birt:amount ?amount }\n"
						+"\n"
						+"\t} group by ?product having ( ?sales >= 1000000) }     \n"
						+"\n"
						+"\t{ select distinct ?product ?country {\n"
						+"\n"
						+"\t\t?product a birt:Product optional { ?product birt:customer/birt:country ?country }\n"
						+"\n"
						+"\t} }\n"
						+"\n"
						+"} group by ?country",

				new Values()

						.setPath(new Path(birt("customer"), birt("country")))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Product"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("order"), birt("amount"))
										.setSummary(Sum)
										.setConstraint(new Range(integer(1_000_000), null)))));
	}

	@Test public void testInnerPathWithAggregateFiltersAndNoExposedHook() {
		assertEquals("<Order> [sum(amount)] | sum(amount) > 1,000,000 >> customer/country",

				// no exposed hook > ignore aggregate filter

				"select ?country (count(*) as ?count) {\n"
						+"\n"
						+"\t{ select distinct ?country { ?order a birt:Order optional { ?order birt:customer/birt:country ?country } } }\n"
						+"\n"
						+"} group by ?country",

				new Values()

						.setPath(new Path(birt("customer"), birt("country")))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Order"))))
								.insertPath(new Path(birt("amount"))
										.setSummary(Sum))
								.insertPath(new Path(birt("amount"))
										.setSummary(Sum)
										.setConstraint(new Range(typed(1_000_000), null)))));
	}

	@Test public void testOuterPathWithAggregateFilters() {
		assertEquals("<Employee> [/, office] | count(office/employee) >= 3 >> office",

				"select ?office (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t{ select ?employee ?office (count(distinct ?_employee) as ?employees) where {\n"
						+"\n"
						+"\t\t?employee a birt:Employee; birt:office ?office .\n"
						+"\t\t?office birt:employee ?_employee .\n"
						+"\n"
						+"\t} group by ?employee ?office having ( ?employees >= 3) }      \n"
						+"\n"
						+"} group by ?office",

				new Values()

						.setPath(new Path(birt("office")))

						.setSpecs(new Specs()
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("office")))
								.insertPath(new Path(birt("office"), birt("employee"))
										.setSummary(Count)
										.setConstraint(new Range(typed(3), null)))));
	}

	@Test public void testOuterAggregatePathWithAggregateFilters() {
		assertEquals("<Employee> [/] | count(office/employee) >= 3 >> count(account)",

				"select ?accounts (count(distinct *) as ?count) where {\n"
						+"\n"
						+"\t{ select distinct ?employee (count(distinct ?account) as ?accounts) where {\n"
						+"\n"
						+"\t\t?employee a birt:Employee; birt:office ?office.\n"
						+"\n"
						+"\t\toptional {\n"
						+"\t\t\t?employee birt:account ?account\n"
						+"\t\t}\n"
						+"\n"
						+"\t\t{ select ?office (count(distinct ?employee) as ?employees) where {\n"
						+"\n"
						+"\t\t\t?office a birt:Office; birt:employee ?employee.\n"
						+"\n"
						+"\t\t} group by ?office having ( ?employees >= 3) }      \n"
						+"\n"
						+"    } group by ?employee }\n"
						+"\n"
						+"} \n"
						+"\n"
						+"group by ?accounts\n"
						+"having (?count > 0)\n",

				new Values()
						.setPath(new Path(birt("account")).setSummary(Count))
						.setSpecs(new Specs()
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path())
								.insertPath(new Path(birt("office"), birt("employee"))
										.setSummary(Count)
										.setConstraint(new Range(typed(3), null)))));
	}

}
