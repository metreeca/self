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

package com.metreeca.self.shared.sparql;

import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;

import org.junit.Test;

import java.util.*;

import static com.metreeca.self.shared.beans.Term.named;

import static org.junit.Assert.*;


public final class FlakeTest {

	private final Term x=node("x");
	private final Term y=node("y");
	private final Term z=node("z");


	private Term node(final String name) {
		return named(name);
	}

	private Path direct(final Term... terms) {
		return new Path(terms);
	}

	private Path inverse(final Term... terms) {

		final List<Term> steps=new ArrayList<>();

		for (final Term term : terms) {
			steps.add(named(term.getText()).reverse());
		}

		return new Path(steps);
	}


	@Test public void testAddPath() {

		final Flake flake=new Flake();

		assertSame("empty direct path", flake, flake.path(direct()));
		assertSame("repeated direct path", flake.path(direct(x, y, z)), flake.path(direct(x, y, z)));
		assertSame("shared direct path", flake.path(direct(x, y, z)), flake.path(direct(x, y)).path(direct(z)));

		assertSame("empty inverse path", flake, flake.path(inverse()));
		assertSame("repeated inverse path", flake.path(inverse(x, y, z)), flake.path(inverse(x, y, z)));
		assertSame("shared inverse path", flake.path(inverse(x, y, z)), flake.path(inverse(x, y)).path(inverse(z)));

		assertNotSame("shared direct/inverse paths", flake.path(direct(x, y, z)), flake.path(inverse(x, y, z)));
	}


	@Test public void testManageRequiredFlagOnEmptyPaths() {

		final Flake flake=new Flake();

		flake.path(direct());

		assertFalse("default optional path", flake.isPresent());

		flake.path(direct().setExisting(true));

		assertTrue("sharing a required path", flake.isPresent());
	}

	@Test public void testManageRequiredFlagOnDirectPaths() {

		final Flake flake=new Flake();

		flake.path(direct(x, y));

		assertFalse("default optional path", flake.isPresent());

		flake.path(direct(x, y, z).setExisting(true));

		assertTrue("sharing a required path", flake.isPresent()
				&& flake.path(direct(x)).isPresent()
				&& flake.path(direct(x, y)).isPresent()
				&& flake.path(direct(x, y, z)).isPresent());
	}

	@Test public void testManageRequiredFlagOnInversePaths() {

		final Flake flake=new Flake();

		flake.path(inverse(x, y));

		assertFalse("default optional path", flake.isPresent());

		flake.path(inverse(x, y, z).setExisting(true));

		assertTrue("sharing a required path", flake.isPresent()
				&& flake.path(inverse(x)).isPresent()
				&& flake.path(inverse(x, y)).isPresent()
				&& flake.path(inverse(x, y, z)).isPresent());
	}


	@Test public void testIdentifyConstantRoots() {

		final Path path=direct();
		final Term term=z;

		final Flake flake=new Flake();

		flake.path(path.setConstraint(new Options(term)));

		assertEquals("constant root", term, flake.path(path).term());
	}

	@Test public void testIdentifyConstantPaths() {

		final Path path=direct(x, y);
		final Term term=z;

		final Flake flake=new Flake();
		flake.path(path.setConstraint(new Options(term)));

		assertEquals("constant path", term, flake.path(path).term());
	}


	//// Optimization //////////////////////////////////////////////////////////////////////////////////////////////////

	@Test public void testRemoveEmptyConstraints() {

		final Flake flake=new Flake();

		flake.path(direct().setConstraint(new Options()));

		assertTrue("remove null constraints", flake.probes().iterator().next().constraints().isEmpty());
	}

	@Test public void testMergeOptionsConstraints() {

		final Flake flake=new Flake();

		flake.path(direct().setConstraint(new Options(x, y)));
		flake.path(direct().setConstraint(new Options(y, z)));

		assertEquals("value intersection",
				Collections.singleton(y),
				((Options)flake.probes().iterator().next().constraints().iterator().next()).getValues());
	}
}
