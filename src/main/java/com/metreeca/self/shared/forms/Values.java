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

import com.metreeca.self.shared.async.Promise;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Client;
import com.metreeca.self.shared.sparql.Engine;
import com.metreeca.self.shared.sparql.Stats;

import java.util.Map;

import static java.util.Arrays.asList;


public final class Values extends Shape<Values, Map<Term, Integer>> { // term > count(node)

	private Path path=new Path();

	private Stats stats;
	private int matches=-1;


	@Override protected Values self() {
		return this;
	}

	@Override public Promise<Map<Term, Integer>> process(final Engine engine, final Client client) {
		return engine.values(this, client);
	}


	public Path getPath() {
		return path;
	}

	public Values setPath(final Path path) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		this.path=path;

		return this;
	}


	public Stats getStats() {

		if ( stats == null ) {

			final Stats.Builder builder=new Stats.Builder();

			for (final Term value : getEntries().keySet()) {
				builder.sample(value);
			}

			stats=builder.stats();
		}

		return stats;
	}

	public boolean isSampled() { // true if the result set is likely sampled

		if ( matches < 0 ) {

			matches=0;

			for (final Integer count : getEntries().values()) {
				matches+=count;
			}
		}

		return matches >= getSample();
	}


	@Override public Object fingerprint() {
		return asList(
				super.fingerprint(),
				path.fingerprint()
		);
	}
}
