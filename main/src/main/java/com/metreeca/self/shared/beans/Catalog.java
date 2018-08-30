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

import com.metreeca._bean.shared.Bean;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.metreeca.self.shared.beans.schemas.DC.DC;
import static com.metreeca.self.shared.beans.schemas.DCAT.DCAT;
import static com.metreeca.self.shared.beans.schemas.DCTerms.DCTerms;
import static com.metreeca.self.shared.beans.schemas.FOAF.FOAF;
import static com.metreeca.self.shared.beans.schemas.Geo.Geo;
import static com.metreeca.self.shared.beans.schemas.OWL.OWL;
import static com.metreeca.self.shared.beans.schemas.Org.Org;
import static com.metreeca.self.shared.beans.schemas.Prov.Prov;
import static com.metreeca.self.shared.beans.schemas.QB.QB;
import static com.metreeca.self.shared.beans.schemas.RDF.RDF;
import static com.metreeca.self.shared.beans.schemas.RDFS.RDFS;
import static com.metreeca.self.shared.beans.schemas.SKOS.SKOS;
import static com.metreeca.self.shared.beans.schemas.Schema.Schema;
import static com.metreeca.self.shared.beans.schemas.VoID.VoID;
import static com.metreeca.self.shared.beans.schemas.XSD.XSD;

import static java.util.Collections.unmodifiableMap;


@Bean public final class Catalog {

	public static final Catalog Empty=new Catalog();

	public static final Catalog Std=new Catalog()

			// W3C standards

			.putPrefix("rdf", RDF)
			.putPrefix("rdfs", RDFS)
			.putPrefix("xsd", XSD)
			.putPrefix("owl", OWL)
			.putPrefix("skos", SKOS)
			.putPrefix("prov", Prov)
			.putPrefix("geo", Geo)
			.putPrefix("dcat", DCAT)
			.putPrefix("org", Org)
			.putPrefix("qb", QB)

			// de-facto standards

			.putPrefix("dc", DC)
			.putPrefix("dcterms", DCTerms)
			.putPrefix("void", VoID)
			.putPrefix("foaf", FOAF)
			.putPrefix("schema", Schema);


	private static final String PrefixPattern="(\\w+):?"; // well-formed prefix pattern
	private static final String IRIPattern="\\w+:.*"; // well-formed absolute IRI pattern


	private final Map<String, String> prefixes=new LinkedHashMap<>();


	public Map<String, String> getPrefixes() {
		return unmodifiableMap(prefixes);
	}

	public Catalog setPrefixes(final Map<String, String> prefixes) {

		if ( prefixes == null ) {
			throw new NullPointerException("null prefixes");
		}

		this.prefixes.clear();

		for (final Map.Entry<String, String> entry : prefixes.entrySet()) {
			putPrefix(entry.getKey(), entry.getValue());
		}

		return this;
	}


	public String getPrefix(final String iri) {
		return prefixes.get(prefix(iri));
	}

	public Catalog putPrefix(final String prefix, final String iri) {

		if ( prefix == null ) {
			throw new NullPointerException("null prefix");
		}

		if ( iri == null ) {
			throw new NullPointerException("null iri");
		}

		prefixes.put(prefix(prefix), iri(iri));

		return this;
	}

	public Catalog removePrefix(final String prefix) {

		if ( prefix == null ) {
			throw new NullPointerException("null prefix");
		}

		prefixes.remove(prefix(prefix));

		return this;
	}


	private String prefix(final String prefix) {

		if ( !prefix.matches(PrefixPattern) ) {
			throw new IllegalArgumentException("illegal prefix ["+prefix+"]");
		}

		return prefix.replaceFirst(PrefixPattern, "$1");
	}

	private String iri(final String iri) {

		if ( !iri.matches(IRIPattern) ) {
			throw new IllegalArgumentException("relative IRI ["+iri+"]");
		}

		return iri;
	}


	public String qualify(final String uri) {

		if ( uri == null ) {
			throw new NullPointerException("null uri");
		}

		for (final Map.Entry<String, String> entry : prefixes.entrySet()) {
			if ( uri.startsWith(entry.getValue()) ) {
				return entry.getKey()+':'+uri.substring(entry.getValue().length()); // !!! check name
			}
		}

		return '<'+uri+'>';
	}

}
