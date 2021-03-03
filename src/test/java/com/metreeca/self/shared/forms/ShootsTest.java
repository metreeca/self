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

package com.metreeca.self.shared.forms;

import com.metreeca.self.shared.async.Handler;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Engine;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.ComparisonFailure;

import java.util.*;


public abstract class ShootsTest extends ShapeTest {

	protected static void assertEquals(final String message, final String expected, final Shoots actual) {

		final Engine engine=new Engine();
		final Iterable<RepositoryConnection> connections=connections();

		for (final String annotation : annotations()) {

			actual // test with different annotation settings

					.setLabel(annotation.contains("label"))
					.setNotes(annotation.contains("note"))
					.setImage(annotation.contains("image"))
					.setPoint(annotation.contains("point"));

			final List<Term> reference=shoots(expected, actual, reference(connections));

			for (final RepositoryConnection connection : connections) {

				final String endpoint=connection.getRepository().toString();
				final String setup=annotation+" @ "+endpoint;

				engine.shoots(actual.setEndpoint(endpoint), client(connection))

						.then(new Handler<List<Term>>() {

							@Override public void value(final List<Term> entries) {
								try {

									assertEquals(message, setup, reference, entries);

								} catch ( final RuntimeException e ) {
									throw new Error("test failure [ "+setup+" ]", e);
								}
							}

							@Override public void error(final Exception error) throws Exception {
								throw new AssertionError("engine failure [ "+setup+" ]", error);
							}

						});

			}
		}
	}

	private static void assertEquals(final String message, final String setup,
			final List<Term> expected, final List<Term> actual) {

		final int es=expected.size();
		final int as=actual.size();

		if ( es != as ) {
			throw new ComparisonFailure(message+": mismatched entry count ("+es+" != "+as+") [ "+setup+" ]",
					expected.toString(), actual.toString());
		}

		for (int i=0; i < es; ++i) {

			final Term ei=expected.get(i);
			final Term ai=actual.get(i);

			if ( !equals(ei, ai) ) {
				throw new ComparisonFailure(message+": mismatched term @"+i
						+" ("+ei+" != "+ai+") [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			if ( ei != null ) {

				if ( !ei.getLabel().equals(ai.getLabel()) ) {
					throw new ComparisonFailure(message+": mismatched term label @"+i
							+" ('"+ei.getLabel()+"' != '"+ai.getLabel()+"') [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( !ei.getNotes().equals(ai.getNotes()) ) {
					throw new ComparisonFailure(message+": mismatched term notes @"+i
							+" ('"+ei.getNotes()+"' != '"+ai.getNotes()+"') [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( !ei.getImage().equals(ai.getImage()) ) {
					throw new ComparisonFailure(message+": mismatched term image @"+i
							+" ('"+ei.getImage()+"' != '"+ai.getImage()+"') [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( Float.compare(ei.getLat(), ai.getLat()) != 0 ) {
					throw new ComparisonFailure(message+": mismatched term latitude @"+i
							+" ("+ei.getLat()+" != "+ai.getLat()+") [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( Float.compare(ei.getLng(), ai.getLng()) != 0 ) {
					throw new ComparisonFailure(message+": mismatched term longitude @"+i
							+" ("+ei.getLng()+" != "+ai.getLng()+") [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

			}
		}
	}

	private static List<Term> shoots(final String roots, final Shoots actual, final RepositoryConnection connection) {
		try {

			final boolean label=actual.getLabel();
			final boolean notes=actual.getNotes();
			final boolean image=actual.getImage();
			final boolean point=actual.getPoint();

			final List<Term> slots=new ArrayList<>();
			final List<Term> roles=new ArrayList<>();

			connection

					.prepareTupleQuery(QueryLanguage.SPARQL, "# shoots direct\n\n"

							+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
							+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"

							+"select distinct\n"
							+"\n"
							+"\t?slot ?slot_label\n"
							+"\t\n"
							+"where {\n"
							+"\n"
							+"\t{ "+roots+" }\n"
							+"\n"
							+"\t?root ?slot ?term .\n"
							+"\t\n"
							+"\toptional { ?slot rdfs:label ?slot_label }\n"
							+"\t\n"
							+"}\n")

					.evaluate(new AbstractTupleQueryResultHandler() {
						@Override public void handleSolution(final BindingSet solution) {

							final Value value=solution.getBinding("slot").getValue();
							final Term term=term(value);

							if ( value instanceof Resource ) {
								if ( label ) { term.setLabel(label((Resource)value, connection)); }
								if ( notes ) { term.setNotes(notes((Resource)value, connection)); }
								if ( image ) { term.setImage(image((Resource)value, connection)); }
								if ( point ) {
									term.setLat(lat((Resource)value, connection)).setLng(lng((Resource)value, connection));
								}
							}

							slots.add(term);

						}
					});

			connection

					.prepareTupleQuery(QueryLanguage.SPARQL, "# shoots inverse\n\n"

							+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
							+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"

							+"select distinct\n"
							+"\n"
							+"\t?role ?role_label\n"
							+"\t\n"
							+"where {\n"
							+"\n"
							+"\t{ "+roots+" }\n"
							+"\n"
							+"\t?term ?role ?root .\n"
							+"\t\n"
							+"\toptional { ?role rdfs:label ?role_label }\n"
							+"\t\n"
							+"}\n")

					.evaluate(new AbstractTupleQueryResultHandler() {
						@Override public void handleSolution(final BindingSet solution) {

							final Value value=solution.getBinding("role").getValue();
							final Term term=term(value).reverse();

							if ( value instanceof Resource ) {
								if ( label ) { term.setLabel(label((Resource)value, connection)); }
								if ( notes ) { term.setNotes(notes((Resource)value, connection)); }
								if ( image ) { term.setImage(image((Resource)value, connection)); }
								if ( point ) {
									term.setLat(lat((Resource)value, connection)).setLng(lng((Resource)value, connection));
								}
							}

							roles.add(term);

						}
					});

			// sort entries

			Collections.sort(slots, Term.TextualOrder);
			Collections.sort(roles, Term.TextualOrder);

			final List<Term> links=new ArrayList<>();

			links.addAll(slots);
			links.addAll(roles);

			// !!! handle offset/limit

			return links;

		} catch ( RepositoryException
				|TupleQueryResultHandlerException
				|MalformedQueryException
				|QueryEvaluationException e ) {
			throw new RuntimeException(e);
		}
	}
}
