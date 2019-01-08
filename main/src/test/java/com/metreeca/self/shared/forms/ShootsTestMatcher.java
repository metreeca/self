/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.Constraint.Range;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Path.Summary;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Term.plain;
import static com.metreeca.self.shared.beans.Term.typed;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


/**
 * outer/inner path (plain field sharing path with target path); plain filter; textual filter; aggregate filter
 */
public final class ShootsTestMatcher extends ShootsTest {

	@Test public void testOuter() {
		assertEquals("<Employee> [/, account/country, count(account)] >> account",

				"select ?root { ?employee a birt:Employee; birt:account ?root }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))));

	}

	@Test public void testOuterPlain() {
		assertEquals("<Employee> [/, account/country, count(account)] | title = 'President' >> account", // empty set

				"select ?root { filter ( false ) }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path(birt("title"))
										.setConstraint(new Options(plain("President"))))));

	}

	@Test public void testOuterTextual() {
		assertEquals("<Employee> [/, account/country, count(account)] | ~ 'Murphy' >> account", // empty set

				"select ?root { filter ( false ) }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.setPattern("murphy")));
	}

	@Test public void testOuterAggregate() {
		assertEquals("<Employee> [/, account/country, count(account)] | count(office/employee) >= 100 >> account", // empty set

				"select ?root { filter ( false ) }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account"))
										.setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path(birt("office"), birt("employee"))
										.setSummary(Summary.Count)
										.setConstraint(new Range(typed(10), null)))));

	}

	@Test public void testInner() {
		assertEquals("<Employee> [account/country, count(account)] >> account",

				"select ?root { ?employee a birt:Employee; birt:account ?root }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))));

	}

	@Test public void testInnerPlain() {
		assertEquals("<Employee> [account/country, count(account)] | title = 'President' >> account", // empty set

				"select ?root { filter ( false ) }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path(birt("title"))
										.setConstraint(new Options(plain("President"))))));

	}

	@Test public void testInnerTextual() {
		assertEquals("<Employee> [account/country, count(account)] | ~ 'Murphy' >> account", // empty set

				"select ?root { filter ( false ) }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account")).setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.setPattern("murphy")));

	}

	@Test public void testInnerAggregate() {
		assertEquals("<Employee> [account/country, count(account)] | count(account) >= 10 >> account", // empty set

				// aggregate filter not exposing target path or root >> ignore it

				"select ?root { ?employee a birt:Employee; birt:account ?root }",

				new Shoots()

						.setPath(birt("account"))
						.setSpecs(new Specs()
								.insertPath(new Path(birt("account"), birt("country")))
								.insertPath(new Path(birt("account"))
										.setSummary(Summary.Count))
								.insertPath(new Path(RDFType)
										.setConstraint(new Options(birt("Employee"))))
								.insertPath(new Path(birt("account"))
										.setSummary(Summary.Count)
										.setConstraint(new Range(typed(100), null)))));

	}

}
