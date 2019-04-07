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

import com.metreeca._jeep.shared.async.Handler;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Engine;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.ComparisonFailure;

import java.util.*;

import static java.util.Collections.singletonList;


public abstract class TuplesTest extends ShapeTest {


	protected static void assertEquals(final String message,
			final String expected, final Tuples actual) {

		assertEquals(message, expected, actual, connections());

	}

	protected static void assertEquals(final String message,
			final String expected, final Tuples actual,
			final RepositoryConnection connection) {

		assertEquals(message, expected, actual, singletonList(connection));

	}

	protected static void assertEquals(final String message,
			final String expected, final Tuples actual,
			final Iterable<RepositoryConnection> connections) {

		final Engine engine=new Engine();

		for (final String annotation : annotations()) {

			actual // test with different annotation settings

					.setLabel(annotation.contains("label"))
					.setNotes(annotation.contains("note"))
					.setImage(annotation.contains("image"))
					.setPoint(annotation.contains("point"));

			final List<List<Term>> reference=tuples(expected, actual, reference(connections));

			for (final RepositoryConnection connection : connections) {

				final String endpoint=connection.getRepository().toString();
				final String setup=annotation+" @ "+endpoint;

				engine

						.tuples(actual.setEndpoint(endpoint), client(connection))

						.then(new Handler<List<List<Term>>>() {

							@Override public void value(final List<List<Term>> entries) {
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
			final List<List<Term>> expected, final List<List<Term>> actual) {

		final int es=expected.size();
		final int as=actual.size();

		if ( es != as ) {
			throw new ComparisonFailure(message+": mismatched rows ("+es+" != "+as+") [ "+setup+" ]",
					expected.toString(), actual.toString());
		}

		for (int i=0; i < es; i++) {

			final List<Term> ei=expected.get(i);
			final List<Term> ai=actual.get(i);

			final int eis=ei.size();
			final int ais=ai.size();

			if ( eis != ais ) {
				throw new ComparisonFailure(message+": mismatched row size @"+i
						+" ("+eis+" != "+ais+") [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			for (int j=0; j < eis; j++) {

				final Term eij=ei.get(j);
				final Term aij=ai.get(j);

				if ( !equals(eij, aij != null && aij.isVerso() ? aij.reverse() : aij) ) { // handle inverse collection links
					throw new ComparisonFailure(message+": mismatched term @"+i+","+j
							+" ("+eij+" != "+aij+") [ "+setup+" ]",
							expected.toString(), actual.toString());
				}

				if ( eij != null ) {

					if ( !eij.getLabel().equals(aij.getLabel()) ) {
						throw new ComparisonFailure(message+": mismatched term label @"+i+","+j
								+" ('"+eij.getLabel()+"' != '"+aij.getLabel()+"') [ "+setup+" ]",
								expected.toString(), actual.toString());
					}

					if ( !eij.getNotes().equals(aij.getNotes()) ) {
						throw new ComparisonFailure(message+": mismatched term notes @"+i+","+j
								+" ('"+eij.getNotes()+"' != '"+aij.getNotes()+"') [ "+setup+" ]",
								expected.toString(), actual.toString());
					}

					if ( !eij.getImage().equals(aij.getImage()) ) {
						throw new ComparisonFailure(message+": mismatched term image @"+i+","+j
								+" ('"+eij.getImage()+"' != '"+aij.getImage()+"') [ "+setup+" ]",
								expected.toString(), actual.toString());
					}

					if ( Float.compare(eij.getLat(), aij.getLat()) != 0 ) {
						throw new ComparisonFailure(message+": mismatched term latitude @"+i+","+j
								+" ("+eij.getLat()+" != "+aij.getLat()+") [ "+setup+" ]",
								expected.toString(), actual.toString());
					}

					if ( Float.compare(eij.getLng(), aij.getLng()) != 0 ) {
						throw new ComparisonFailure(message+": mismatched term longitude @"+i+","+j
								+" ("+eij.getLng()+" != "+aij.getLng()+") [ "+setup+" ]",
								expected.toString(), actual.toString());
					}

				}

			}
		}

	}


	private static List<List<Term>> tuples(final String reference, final Tuples tuples, final RepositoryConnection connection) {
		try {

			final boolean label=tuples.getLabel();
			final boolean notes=tuples.getNotes();
			final boolean image=tuples.getImage();
			final boolean point=tuples.getPoint();


			final List<List<Term>> entries=new ArrayList<>();

			connection.prepareTupleQuery(QueryLanguage.SPARQL, "# tuples\n\n"

					+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
					+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"

					+reference

			).evaluate(new AbstractTupleQueryResultHandler() {

				private List<String> variables;

				@Override public void startQueryResult(final List<String> variables) {
					this.variables=variables;
				}

				@Override public void handleSolution(final BindingSet solution) {

					final List<Term> entry=new ArrayList<>();

					for (final String variable : variables) {

						final Value value=solution.getValue(variable);
						final Term term=term(value);

						if ( value instanceof Resource ) {
							if ( label ) { term.setLabel(label((Resource)value, connection)); }
							if ( notes ) { term.setNotes(notes((Resource)value, connection)); }
							if ( image ) { term.setImage(image((Resource)value, connection)); }
							if ( point ) {
								term.setLat(lat((Resource)value, connection)).setLng(lng((Resource)value, connection));
							}
						}

						entry.add(term);
					}

					entries.add(entry);
				}

			});

			// sort entries

			final List<Path> fields=tuples.getSpecs().getFields();

			Collections.sort(entries, new Comparator<List<Term>>() {
				@Override public int compare(final List<Term> x, final List<Term> y) {

					if ( x.size() != y.size() ) {
						throw new IllegalArgumentException("incompatible lists");
					}

					final Comparator<Term> comparator=new Comparator<Term>() {

						// emulate generated SPARQL ordering (asc(str(ucase(?0))) asc(?0))

						private final Comparator<Value> comparator=new ValueComparator();

						@Override public int compare(final Term x, final Term y) {

							final int order=comparator.compare(label(x), label(y));

							return order == 0 ? comparator.compare(value(x), value(y)) : order;
						}

						private Value label(final Term term) {
							return term == null || term.getLabel().isEmpty() ?
									null : factory.createLiteral(term.getLabel().toUpperCase());
						}

					};

					for (int i=0; i < fields.size(); i++) { // !!! handle multiple sorting fields

						final int sort=fields.get(i).getSort();

						if ( sort != 0 ) {

							final int order=comparator.compare(x.get(i), y.get(i));

							if ( order != 0 ) { return sort > 0 ? order : -order; }

						}

					}

					for (int i=0; i < x.size(); i++) {

						final int order=comparator.compare(x.get(i), y.get(i));

						if ( order != 0 ) { return order; }

					}

					return 0;

				}
			});

			// !!! handle offset/limit

			return entries;

		} catch ( final QueryEvaluationException
				|TupleQueryResultHandlerException
				|MalformedQueryException
				|RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}

}
