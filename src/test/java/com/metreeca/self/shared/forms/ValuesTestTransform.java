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
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Path.Transform.Year;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class ValuesTestTransform extends ValuesTest {

	@Test public void testTransformedPath() {
		assertEquals("<Order> [/] >> year(received)",

				"select ?year (count(distinct ?order) as ?count) where {\n"
						+"\n"
						+"\t?order a birt:Order optional { ?order birt:received ?received }\n"
						+"\n"
						+"\tbind (year(?received) as ?year)\n"
						+"\n"
						+"} group by ?year",

				new Values()
						.setPath(new Path(birt("received")).setTransform(Year))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Options(birt("Order"))))));
	}

}
