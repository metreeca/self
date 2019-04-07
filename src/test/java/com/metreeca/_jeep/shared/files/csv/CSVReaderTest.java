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

package com.metreeca._jeep.shared.files.csv;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;

import static java.util.Arrays.asList;


public final class CSVReaderTest {

	@Test public void testParse() throws IOException {
		assertEquals("parsing", asList(

				asList("start"),
				asList("table", null, null),
				asList("record", 0, asList("first", "second", "\"quoted\"")),
				asList("record", 1, asList("a0", "a1", "a2")),
				asList("record", 2, asList("b 0", "b\"1", "b\n2")),
				asList("size", 3),
				asList("end")

		), new com.metreeca._jeep.shared.files.csv.CSVReader()
				.read(source(), new TestHandler()).events);
	}

	@Test public void testLabel() throws IOException {
		assertEquals("labelling", asList(

				asList("start"),
				asList("table", null, asList("first", "second", "\"quoted\"")),
				asList("record", 0, asList("a0", "a1", "a2")),
				asList("record", 1, asList("b 0", "b\"1", "b\n2")),
				asList("size", 2),
				asList("end")

		), new com.metreeca._jeep.shared.files.csv.CSVReader().setLabeled(true)
				.read(source(), new TestHandler()).events);
	}

	@Test public void testSeparator() throws IOException {
		assertEquals("separator", asList(

				asList("start"),
				asList("table", null, asList("first", "second", "\"quoted\"")),
				asList("record", 0, asList("a0", "a1", "a2")),
				asList("record", 1, asList("b 0", "b\"1", "b\n2")),
				asList("size", 2),
				asList("end")

		), new com.metreeca._jeep.shared.files.csv.CSVReader().setLabeled(true).setSeparator('\t')
				.read(source().replace(',', '\t'), new TestHandler()).events);
	}


	private static class TestHandler extends com.metreeca._jeep.shared.files.csv.TableHandler {

		private final Collection<List<?>> events=new ArrayList<List<?>>();


		@Override public void start() {
			events.add(asList("start"));
		}

		@Override public void table(final String name, final List<String> fields) {
			events.add(asList("table", name, fields));
		}

		@Override public void record(final int index, final List<String> values) {
			events.add(asList("record", index, values));
		}

		@Override public void size(final int size) {
			events.add(asList("size", size));
		}

		@Override public void end() {
			events.add(asList("end"));
		}
	}


	//// Harness ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private String source() { return "first,second,\"\"\"quoted\"\"\"\n\na0,a1,a2\n\"b 0\",\"b\"\"1\",\"b\n2\"\n"; }
}
