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

import com.metreeca.self.shared.beans.Constraint;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import org.junit.Test;

import static com.metreeca.self.shared.beans.Path.Transform.Abs;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;


public final class ShootsTestTransform extends ShootsTest {

	//// Projecting ////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testTransformedTarget() {
		assertEquals("<Order> [abs(amount)] >> /",

				"select ?root { ?root a birt:Order }",

				new Shoots().setSpecs(new Specs()

						.insertPath(new Path(RDFType)
								.setConstraint(new Constraint.Options(birt("Order"))))
						.insertPath(new Path(birt("amount"))
								.setTransform(Abs))));
	}

}
