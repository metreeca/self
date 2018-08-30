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

package com.metreeca.self.shared.forms;

import com.metreeca._jeep.shared.async.Promise;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Client;
import com.metreeca.self.shared.sparql.Engine;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;


public final class Leaves extends Shape<Leaves, Map<Term, List<Term>>> { // link > terms

	private int detail; // the maximum number of terms to retrieve for each link


	@Override protected Leaves self() {
		return this;
	}

	@Override public Promise<Map<Term, List<Term>>> process(final Engine engine, final Client client) {
		return engine.leaves(this, client);
	}


	public int getDetail() {
		return detail;
	}

	public Leaves setDetail(final int detail) {

		if ( detail < 0 ) {
			throw new IllegalArgumentException("illegal detail ["+detail+"]");
		}

		this.detail=detail;

		return this;
	}

	@Override public Object fingerprint() {
		return asList(
				super.fingerprint(),
				detail
		);
	}

}
