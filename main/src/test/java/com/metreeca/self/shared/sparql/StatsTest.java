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

package com.metreeca.self.shared.sparql;

import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.beans.schemas.XSD;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.metreeca.self.shared.beans.Term.plain;
import static com.metreeca.self.shared.beans.Term.typed;

import static org.junit.Assert.assertEquals;


public class StatsTest {

	private static Stats stats(final Term... terms) {

		final Stats.Builder builder=new Stats.Builder();

		for (final Term term : terms) {
			builder.sample(term);
		}

		return builder.stats();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testIntegersArePromotedToDecimals() {

		final Stats stats=stats(typed(BigDecimal.ZERO), typed(BigInteger.ONE));

		assertEquals("type", XSD.XSDDecimal, stats.getType());
		assertEquals("min", BigDecimal.ZERO, stats.getMin());
		assertEquals("max", BigDecimal.ONE, stats.getMax());

	}

	@Test public void testRealsArePromotedToDecimals() {

		final Stats stats=stats(typed(BigDecimal.ZERO), typed(1F));

		assertEquals("type", XSD.XSDDecimal, stats.getType());
		assertEquals("min", BigDecimal.ZERO, stats.getMin());
		assertEquals("max", BigDecimal.ONE, stats.getMax());

	}

	@Test public void testNaturalsArePromotedToDecimals() {

		final Stats stats=stats(typed(BigDecimal.ZERO), typed(1));

		assertEquals("type", XSD.XSDDecimal, stats.getType());
		assertEquals("min", BigDecimal.ZERO, stats.getMin());
		assertEquals("max", BigDecimal.ONE, stats.getMax());

	}


	@Test public void testNaturalsArePromotedToIntegers() {

		final Stats stats=stats(typed(BigInteger.ZERO), typed(1));

		assertEquals("type", XSD.XSDInteger, stats.getType());
		assertEquals("min", BigInteger.ZERO, stats.getMin());
		assertEquals("max", BigInteger.ONE, stats.getMax());

	}

	@Test public void testNaturalsArePromotedToLongs() {

		final Stats stats=stats(typed(0L), typed(1));

		assertEquals("type", XSD.XSDLong, stats.getType());
		assertEquals("min", 0L, stats.getMin());
		assertEquals("max", 1L, stats.getMax());

	}

	@Test public void testNaturalsArePromotedToInts() {

		final Stats stats=stats(typed(0), typed((short)1));

		assertEquals("type", XSD.XSDInt, stats.getType());
		assertEquals("min", 0, stats.getMin());
		assertEquals("max", 1, stats.getMax());

	}

	@Test public void testNaturalsArePromotedToShorts() {

		final Stats stats=stats(typed((short)0), typed((byte)1));

		assertEquals("type", XSD.XSDShort, stats.getType());
		assertEquals("min", (short)0, stats.getMin());
		assertEquals("max", (short)1, stats.getMax());

	}


	@Test public void testFloatsArePromotedToDoubles() {

		final Stats stats=stats(typed(0d), typed(1f));

		assertEquals("type", XSD.XSDDouble, stats.getType());
		assertEquals("min", 0d, stats.getMin());
		assertEquals("max", 1d, stats.getMax());

	}

	@Test public void testNaturalsArePromotedToDoubles() {

		final Stats stats=stats(typed(0d), typed(1));

		assertEquals("type", XSD.XSDDouble, stats.getType());
		assertEquals("min", 0d, stats.getMin());
		assertEquals("max", 1d, stats.getMax());

	}


	@Test public void testNaturalsArePromotedToFloats() {

		final Stats stats=stats(typed(0f), typed(1));

		assertEquals("type", XSD.XSDFloat, stats.getType());
		assertEquals("min", 0f, stats.getMin());
		assertEquals("max", 1f, stats.getMax());

	}


	@Test public void testSimpleLiteralsArePromotedToStrings() {

		final Stats stats=stats(typed("a"), plain("z"));

		assertEquals("type", XSD.XSDString, stats.getType());
		assertEquals("min", "a", stats.getMin());
		assertEquals("max", "z", stats.getMax());

	}

	@Test public void testStringsLiteralsArePromotedToSimpleLiterals() {

		final Stats stats=stats(plain("a"), typed("z"));

		assertEquals("type", Term.Simple, stats.getType());
		assertEquals("min", "a", stats.getMin());
		assertEquals("max", "z", stats.getMax());

	}

}
