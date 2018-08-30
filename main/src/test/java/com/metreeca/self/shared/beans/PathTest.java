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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.metreeca.self.shared.beans.Term.named;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;


public final class PathTest {

	@Test public void testPivotOnEmptyPath() {

		final Path path=path("x", "y", "z");
		final Path pivot=path.pivot(path());

		assertNotSame("create path", path, pivot);
		assertEquals("preserve steps", path.getSteps(), pivot.getSteps());
	}

	@Test public void testPivotOnSamePath() {

		final Path path=path("x", "y", "z");

		assertEquals("clear path", path().getSteps(), path.pivot(path).getSteps());
	}

	@Test public void testPivotOnPartialPath() {

		final Path path=path("x", "y", "z");

		assertEquals("partial path (I)", path("z").getSteps(), path.pivot(path("x", "y")).getSteps());
		assertEquals("partial path (II)", path("y", "z").getSteps(), path.pivot(path("x")).getSteps());
	}

	@Test public void testPivotOnDisjointPath() {

		final Path path=path("x", "y", "z");

		assertEquals("disjoint path",
				path("^y", "^x", "^w", "x", "y", "z").getSteps(),
				path.pivot(path("w", "x", "y")).getSteps());
	}

	@Test public void testPivotOnSharedPath() {

		final Path path=path("x", "y", "z");

		assertEquals("shared path (I)", path("^w", "z").getSteps(), path.pivot(path("x", "y", "w")).getSteps());
		assertEquals("shared path (II)", path("^w", "y", "z").getSteps(), path.pivot(path("x", "w")).getSteps());
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private Path path(final String... terms) {

		final List<Term> steps=new ArrayList<>();

		for (final String term : terms) {

			final boolean inverse=term.startsWith("^");
			final String text=inverse ? term.substring(1) : term;

			steps.add(inverse ? named(text).reverse() : named(text));
		}

		return new Path(steps);
	}
}
