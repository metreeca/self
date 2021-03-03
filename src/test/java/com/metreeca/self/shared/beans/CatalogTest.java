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

package com.metreeca.self.shared.beans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CatalogTest {

	private static final String Example="http://example.org/";


	@Test public void testTrailingColonIsIgnored() {


		final Catalog catalog=new Catalog()
				.putPrefix("one", Example)
				.putPrefix("two:", Example);

		assertEquals("one colon", Example, catalog.getPrefix("one:"));
		assertEquals("one no colon", Example, catalog.getPrefix("one"));

		assertEquals("two colon", Example, catalog.getPrefix("two:"));
		assertEquals("two no colon", Example, catalog.getPrefix("two"));

	}


	@Test(expected=IllegalArgumentException.class) public void testMalformedPrefixesAreRejected() {
		new Catalog().putPrefix("ill:formed", Example);
	}

	@Test(expected=IllegalArgumentException.class) public void testRelativeIRIsAreRejected() {
		new Catalog().putPrefix("ex", "/example");
	}

}
