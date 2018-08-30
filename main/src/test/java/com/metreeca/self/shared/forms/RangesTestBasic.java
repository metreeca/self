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
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class RangesTestBasic extends RangesTest {

	@Test public void testProjection() {
		assertEquals("<Product> [] >> sell",

				"select ?price where { [] a birt:Product; birt:sell ?price }",

				new Ranges()
						.setPath(new Path(birt("sell")))
						.setSpecs(new Specs()
								.insertPath(new Path())
								.insertPath(new Path(RDFType).setConstraint(new Constraint.Options(birt("Product"))))));

	}
}
