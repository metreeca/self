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

package com.metreeca.self.shared.beans.schemas;

import com.metreeca.self.shared.beans.Term;

import static com.metreeca.self.shared.beans.Term.named;


public final class RDF {

	public static final String RDF="http://www.w3.org/1999/02/22-rdf-syntax-ns#";

	public static final String RDFLangString=RDF+"langString";

	public static final Term RDFType=named(RDF, "type").setLabel("collection"); // !!! move default naming to parsers


	private RDF() {}

}
