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

package com.metreeca._jeep.shared.files.csv;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;


public final class CSVWriterTest {

	@Test public void testWriteRecords() {
		assertEquals("empty", "", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table()));
		assertEquals("singleton", "a\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table("a")));
		assertEquals("pair", "a,b\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table("a", "b")));
		assertEquals("regular", "a,b,c\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table("a", "b", "c")));
	}

	@Test public void testQuoteValues() {
		assertEquals("separator", "\",\"\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table(",")));
		assertEquals("delimiter", "\"\"\"\"\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table("\"")));
		assertEquals("newline", "\"\n\"\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table("\n")));
		assertEquals("return", "\"\r\"\n", new com.metreeca._jeep.shared.files.csv.CSVWriter().write(table("\r")));
	}


	//// Harness ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private Table table(final String... values) {
		return new Table() {
			@Override public List<String> labels() { return Arrays.asList(values); }
		};
	}
}
