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

package com.metreeca.self.shared.beans;

import com.metreeca.self.shared.beans.schemas.RDF;
import com.metreeca.self.shared.beans.schemas.XSD;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.metreeca.self.shared.beans.Term.named;
import static com.metreeca.self.shared.beans.Term.plain;
import static com.metreeca.self.shared.beans.Term.typed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public final class TermTest {

	@Test public void testGetLang() {
		assertEquals("tagged literal", "en", plain("x", "en").getLang());
		assertEquals("simple literal", "", plain("x").getLang());
		assertEquals("not a plain literal", "", named("x").getLang());
	}


	@Test public void testGetStringValue() {
		assertEquals("string", "text", typed("text", XSD.XSDString).getValue());
		assertEquals("simple", "text", plain("text").getValue());
		assertEquals("tagged", "text", plain("text", "@en").getValue());
	}

	@SuppressWarnings("deprecation")
	// Date.UTC > Calendar not portable to GWT // !!! replace with client/server solution
	@Test public void testGetDateValue() {
		assertEquals("date+time", new Date(Date.UTC(70, 0, 1, 0, 0, 0)), typed("1970-01-01T00:00:00", XSD.XSDDateTime).getValue());
		assertEquals("date", new Date(Date.UTC(2005-1900, 0, 1, 0, 0, 0)), typed("2005-01-01", XSD.XSDDateTime).getValue());
		assertEquals("date+time+ms", new Date(Date.UTC(70, 0, 1, 0, 0, 0)), typed("1970-01-01T00:00:00.000Z", XSD.XSDDateTime).getValue());
	}


	@SuppressWarnings("deprecation")
	// Date.UTC > Calendar not portable to GWT // !!! replace with client/server solution
	@Test public void testConstructDateTime() {
		assertEquals("ac", typed("2015-03-26T12:06:01Z", XSD.XSDDateTime), typed(new Date(2015-1900, 3-1, 26, 12, 6, 1)));
		assertEquals("bc", typed("-2015-03-26T12:06:01Z", XSD.XSDDateTime), typed(new Date(-1899-2015, 3-1, 26, 12, 6, 1)));
		assertEquals("< 10", typed("0001-01-01T01:01:01Z", XSD.XSDDateTime), typed(new Date(1-1900, 1-1, 1, 1, 1, 1)));
	}

	@Test public void testEscapeLiterals() {
		assertEquals("escaped literal", "'escaped \\'\\\"\\n\\\\'", plain("escaped '\"\n\\").format());
	}

	@Test public void testFormatLiterals() {
		assertEquals("simple literal", "'test'", plain("test").format());
		assertEquals("tagged literal", "'test'@en", plain("test", "en").format());
	}

	@Test public void testToString() {

		assertEquals("use well-known prefixes", "rdf:type", RDF.RDFType.toString());

		assertEquals("always include fractional part for xsd:decimal (null scale)", "10.0",
				typed(BigDecimal.valueOf(10)).toString());
		assertEquals("always include fractional part for xsd:decimal (negative scale)", "130.0",
				typed(BigDecimal.valueOf(123).setScale(-1, RoundingMode.CEILING)).toString());
	}


	@Test public void testGuessNameFromURI() {

		assertEquals("slash uri", "name", named("http://example.com/schema/name").label());
		assertEquals("slash uri trailing slash", "name", named("http://example.com/schema/name/").label());
		assertEquals("slash uri trailing hash id", "name", named("http://example.com/schema/name#this").label());

		assertEquals("hash uri", "name", named("http://example.com/schema#name").label());
		assertEquals("urn", "name", named("urn:example:name").label());

		assertEquals("hyphenated name", "the name", named("http://example.com/schema/the-name").label());
		assertEquals("hyphenated name (singleton)", "a name", named("http://example.com/schema/a-name").label());

		assertEquals("underscored name", "the name", named("http://example.com/schema/the_name").label());
		assertEquals("underscored name (singleton)", "a name", named("http://example.com/schema/a_name").label());

		assertEquals("lowercase camel name", "the name", named("http://example.com/schema/theName").label());
		assertEquals("lowercase camel name (singleton)", "a name", named("http://example.com/schema/aName").label());

		assertEquals("uppercase camel name", "The Name", named("http://example.com/schema/TheName").label());
		assertEquals("uppercase camel name (singleton)", "A Name", named("http://example.com/schema/AName").label());

		assertEquals("leading acronym in camel name", "WWW Name", named("http://example.com/schema/WWWName").label());
		assertEquals("inset acronym in camel name", "a WWW name", named("http://example.com/schema/aWWWName").label());
		assertEquals("trailing acronym in camel name", "name WWW", named("http://example.com/schema/nameWWW").label());
		assertEquals("trailing uppercase char in camel name", "name X", named("http://example.com/schema/nameX").label());

		assertEquals("urlencoded characters", "name", named("http://example.com/schema/n%61me").label());
		assertEquals("UTF8 urlencoded spaces", "na me", named("http://example.com/schema/na+me").label());
		assertEquals("UTF8 urlencoded characters", "Sōtarō", named("http://example.com/schema/S%C5%8Dtar%C5%8D").label());
		assertEquals("special chars", "'na.me\"", named("http://example.com/schema/'na.me\"").label());

	}

	@Test public void testVisualPatternsShouldBeCaseInsensitive() {
		assertTrue("lower case", plain("http://example.com/visual.jpg").isVisual());
		assertTrue("upper case", plain("http://example.com/visual.JPG").isVisual());
	}

	//// xsd:year //////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testLenientHandlingOfBrokenVirtuosoGYearLiterals() {
		assertEquals("labelling", "2000", typed("2000-01-01+02:00", XSD.XSDGYear).label(Catalog.Std));
		assertEquals("formatting", "'2000'^^xsd:gYear", typed("2000-01-01+02:00", XSD.XSDGYear).format(Catalog.Std));
	}

}
