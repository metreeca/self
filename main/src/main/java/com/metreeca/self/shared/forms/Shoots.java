/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.shared.forms;

import com.metreeca._jeep.shared.async.Promise;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Client;
import com.metreeca.self.shared.sparql.Engine;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;


public final class Shoots extends Shape<Shoots, List<Term>> {

	private List<Term> path=emptyList();


	@Override protected Shoots self() {
		return this;
	}

	@Override public Promise<List<Term>> process(final Engine engine, final Client client) {
		return engine.shoots(this, client);
	}


	public List<Term> getPath() {
		return unmodifiableList(path);
	}

	public Shoots setPath(final Term... path) {
		return setPath(asList(path));
	}

	public Shoots setPath(final List<Term> path) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		this.path=new ArrayList<>(path);

		return this;
	}


	@Override public Object fingerprint() {
		return asList(
				super.fingerprint(),
				path
		);
	}
}
