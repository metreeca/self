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

package com.metreeca.self.shared.sparql;

import com.metreeca._tool.shared.forms.Form;


public final class Query extends Form<Query> {

	private String server="";
	private String source="";

	private int timeout;

	private Table results;


	@Override protected Query self() {
		return this;
	}


	public String getSource() {
		return source;
	}

	public Query setSource(final String source) {

		if ( source == null ) {
			throw new NullPointerException("null source");
		}

		this.source=source;

		return this;
	}


	public String getServer() {
		return server;
	}

	public Query setServer(final String server) {

		if ( server == null ) {
			throw new NullPointerException("null server");
		}

		this.server=server;

		return this;
	}


	public int getTimeout() {
		return timeout;
	}

	public Query setTimeout(final int timeout) {

		if ( timeout < 0 ) {
			throw new IllegalArgumentException("illegal timeout ["+timeout+"]");
		}

		this.timeout=timeout;

		return this;
	}


	public Table getResults() {
		return results;
	}

	public Query setResults(final Table results) {

		if ( results == null ) {
			throw new NullPointerException("null results");
		}

		this.results=results;

		return this;
	}

}
