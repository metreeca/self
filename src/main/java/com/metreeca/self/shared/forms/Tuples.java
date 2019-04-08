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
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Client;
import com.metreeca.self.shared.sparql.Engine;
import com.metreeca.self.shared.sparql.Stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class Tuples extends Shape<Tuples, List<List<Term>>> {

	private final List<Stats> stats=new ArrayList<>();


	@Override protected Tuples self() {
		return this;
	}

	@Override public Promise<List<List<Term>>> process(final Engine engine, final Client client) {
		return engine.tuples(this, client);
	}


	public List<Stats> getStats() {

		if ( stats.isEmpty() ) {

			final int size=getSpecs().getFields().size();

			if ( getEntries().isEmpty() ) {

				for (int i=0; i < size; i++) { stats.add(Stats.Nil); }

			} else {

				final Stats.Builder[] builders=new Stats.Builder[size];

				for (int i=0; i < size; i++) {
					builders[i]=new Stats.Builder();
				}

				for (final List<Term> tuple : getEntries()) {
					for (int i=0; i < size; i++) {
						builders[i].sample(tuple.get(i));
					}
				}

				for (final Stats.Builder builder : builders) {
					stats.add(builder.stats());
				}
			}
		}

		return Collections.unmodifiableList(stats);
	}

}
