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

package com.metreeca.self.shared.sparql;

import com.metreeca._jeep.shared.async.Promise;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.*;
import com.metreeca.self.shared.sparql.basic.*;

import java.util.List;
import java.util.Map;


public final class Engine {

	public Promise<List<List<Term>>> tuples(final Tuples tuples, final Client client) {
		return new BasicTuples().entries(tuples, client);
	}

	public Promise<List<Term>> shoots(final Shoots shoots, final Client client) {
		return new BasicShoots().entries(shoots, client);
	}

	public Promise<Map<Term, List<Term>>> leaves(final Leaves leaves, final Client client) {
		return new BasicLeaves().entries(leaves, client);
	}

	public Promise<Map<Term, Integer>> values(final Values values, final Client client) {
		return new BasicValues().entries(values, client);
	}

	public Promise<List<Stats>> ranges(final Ranges ranges, final Client client) {
		return new BasicRanges().entries(ranges, client);
	}

}
