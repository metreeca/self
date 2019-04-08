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


public abstract class ValuesTest extends ShapeTest {

	protected static void assertEquals(final String message, final String expected, final Values actual) {

		final Engine engine=new Engine();
		final Iterable<RepositoryConnection> connections=connections();

		for (final String annotation : annotations()) {

			actual // test with different annotation settings

					.setLabel(annotation.contains("label"))
					.setNotes(annotation.contains("note"))
					.setImage(annotation.contains("image"))
					.setPoint(annotation.contains("point"));

			final Map<Term, Integer> reference=values(expected, actual, reference(connections));

			for (final RepositoryConnection connection : connections) {

				final String endpoint=connection.getRepository().toString();
				final String setup=annotation+" @ "+endpoint;

				engine

						.values(actual.setEndpoint(endpoint), client(connection))

						.then(new Handler<Map<Term, Integer>>() {

							@Override public void value(final Map<Term, Integer> entries) {
								try {

									assertEquals(message, setup, reference, entries);

								} catch ( final RuntimeException e ) {
									throw new Error("test failure [ "+setup+" ]", e);
								}
							}

							@Override public void error(final Exception error) {
								throw new AssertionError("engine failure [ "+setup+" ]", error);
							}

						});

			}
		}
	}

	private static void assertEquals(final String message, final String setup,
			final Map<Term, Integer> expected, final Map<Term, Integer> actual) {

		final int es=expected.size();
		final int as=actual.size();

		if ( es != as ) {
			throw new ComparisonFailure(message+": mismatched entry count ("+es+" != "+as+") [ "+setup+" ]",
					expected.toString(), actual.toString());
		}

		final Iterator<Map.Entry<Term, Integer>> ei=expected.entrySet().iterator();
		final Iterator<Map.Entry<Term, Integer>> ai=actual.entrySet().iterator();

		for (int i=0; ei.hasNext(); ++i) {

			final Map.Entry<Term, Integer> ee=ei.next();
			final Map.Entry<Term, Integer> ae=ai.next();

			final Term ek=ee.getKey();
			final Term ak=ae.getKey();

			if ( !equals(ek, ak) ) {
				throw new ComparisonFailure(message+": mismatched term @ "+i
						+" ("+ek+" != "+ak+") [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			if ( ek != null ) {

				if ( !ek.getLabel().equals(ak.getLabel()) ) {
					throw new ComparisonFailure(message+": mismatched term label @ "+i+"/"+ek
							+" ('"+ek.getLabel()+"' != '"+ak.getLabel()+"') [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( !ek.getNotes().equals(ak.getNotes()) ) {
					throw new ComparisonFailure(message+": mismatched term notes @ "+i+"/"+ek
							+" ('"+ek.getNotes()+"' != '"+ak.getNotes()+"') [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( !ek.getImage().equals(ak.getImage()) ) {
					throw new ComparisonFailure(message+": mismatched term image @ "+i+"/"+ek
							+" ('"+ek.getImage()+"' != '"+ak.getImage()+"') [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( Float.compare(ek.getLat(), ak.getLat()) != 0 ) {
					throw new ComparisonFailure(message+": mismatched term latitude@ "+i+"/"+ek
							+" ("+ek.getLat()+" != "+ak.getLat()+") [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( Float.compare(ek.getLng(), ak.getLng()) != 0 ) {
					throw new ComparisonFailure(message+": mismatched term longitude @ "+i+"/"+ek
							+" ("+ek.getLng()+" != "+ak.getLng()+") [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

			}

			final Integer ev=ee.getValue();
			final Integer av=ae.getValue();

			if ( Integer.compare(ev, av) != 0 ) {
				throw new ComparisonFailure(message+": mismatched count @ "+i+"/"+ek
						+" ("+ev+" != "+av+") [ "+setup+" ]",
						expected.toString(), actual.toString());
			}
		}
	}

	private static Map<Term, Integer> values(final String reference, final Values values, final RepositoryConnection connection) {
		try {

			final boolean label=values.getLabel();
			final boolean notes=values.getNotes();
			final boolean image=values.getImage();
			final boolean point=values.getPoint();

			final Map<Term, Integer> entries=new HashMap<>();

			connection.prepareTupleQuery(QueryLanguage.SPARQL, "# values\n\n"

					+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
					+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"
					+"prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n\n"

					+reference

			).evaluate(new AbstractTupleQueryResultHandler() {

				private String value;
				private String count;

				@Override public void startQueryResult(final List<String> variables) {

					this.value=variables.get(0);
					this.count=variables.get(1);

				}

				@Override public void handleSolution(final BindingSet solution) {

					final Value value=solution.getValue(this.value);
					final Term term=term(value);

					if ( value instanceof Resource ) {
						if ( label ) { term.setLabel(label((Resource)value, connection)); }
						if ( notes ) { term.setNotes(notes((Resource)value, connection)); }
						if ( image ) { term.setImage(image((Resource)value, connection)); }
						if ( point ) {
							term.setLat(lat((Resource)value, connection)).setLng(lng((Resource)value, connection));
						}
					}

					entries.put(term, ((Number)term(solution.getValue(count)).getValue()).intValue());

				}

			});

			// sort entries by descending count / term

			// !!! handle offset/limit

			return sort(entries, new Comparator<Map.Entry<Term, Integer>>() {
				@Override public int compare(final Map.Entry<Term, Integer> x, final Map.Entry<Term, Integer> y) {

					final int order=-x.getValue().compareTo(y.getValue()); // by descending count

					return order == 0 ? Term.TextualOrder.compare(x.getKey(), y.getKey()) : order;
				}
			});

		} catch ( final QueryEvaluationException
				|TupleQueryResultHandlerException
				|MalformedQueryException
				|RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}

}
