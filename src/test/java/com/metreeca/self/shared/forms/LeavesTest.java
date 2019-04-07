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
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.sparql.Engine;

import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.ComparisonFailure;

import java.util.*;

import static java.lang.Math.min;


public abstract class LeavesTest extends ShapeTest {

	protected static void assertEquals(final String message, final String expected, final Leaves actual) {

		final Engine engine=new Engine();
		final Iterable<RepositoryConnection> connections=connections();

		for (final String annotation : annotations()) {

			actual // test with different annotation settings

					.setLabel(annotation.contains("label"))
					.setNotes(annotation.contains("note"))
					.setImage(annotation.contains("image"))
					.setPoint(annotation.contains("point"));

			final Map<Term, List<Term>> reference=leaves(expected, actual, reference(connections));

			for (final RepositoryConnection connection : connections) {

				final String endpoint=connection.getRepository().toString();
				final String setup=annotation+" @ "+endpoint;

				engine

						.leaves(actual.setEndpoint(endpoint), client(connection))

						.then(new Handler<Map<Term, List<Term>>>() {

							@Override public void value(final Map<Term, List<Term>> entries) {
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
			final Map<Term, List<Term>> expected, final Map<Term, List<Term>> actual) {

		final int es=expected.size();
		final int as=actual.size();

		if ( es != as ) {
			throw new ComparisonFailure(message+": mismatched entry count ("+es+" != "+as+") [ "+setup+" ]",
					expected.toString(), actual.toString());
		}

		final Iterator<Map.Entry<Term, List<Term>>> ei=expected.entrySet().iterator();
		final Iterator<Map.Entry<Term, List<Term>>> ai=actual.entrySet().iterator();

		for (int i=0; ei.hasNext(); ++i) {

			final Map.Entry<Term, List<Term>> ee=ei.next();
			final Map.Entry<Term, List<Term>> ae=ai.next();

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

			final List<Term> ev=ee.getValue();
			final List<Term> av=ae.getValue();

			final int evs=ev.size();
			final int avs=av.size();

			if ( evs != avs ) {
				throw new ComparisonFailure(message+": mismatched term list size @"+i
						+" ("+evs+" != "+avs+") [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			for (int j=0; j < evs; j++) {

				final Term eij=ev.get(j);
				final Term aij=av.get(j);

				if ( !equals(eij, aij) ) {
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

	private static Map<Term, List<Term>> leaves(final String roots, final Leaves actual, final RepositoryConnection connection) { // !!! refactor
		try {

			final boolean label=actual.getLabel();
			final boolean notes=actual.getNotes();
			final boolean image=actual.getImage();
			final boolean point=actual.getPoint();

			final Map<Term, List<Term>> slots=new TreeMap<>(Term.TextualOrder);
			final Map<Term, List<Term>> roles=new TreeMap<>(Term.TextualOrder);

			connection

					.prepareTupleQuery(QueryLanguage.SPARQL, ("# leaves direct\n\n"

							+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
							+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"

							+"select distinct\n"
							+"\n"
							+"\t?slot ?slot_label\n"
							+"\t?term ?term_label\n"
							+"\t\n"
							+"where {\n"
							+"\n"
							+"\t{ (roots) }\n"
							+"\n"
							+"\t?root ?slot ?term .\n"
							+"\t\n"
							+"\toptional { ?slot rdfs:label ?slot_label }\n"
							+"\toptional { ?term rdfs:label ?term_label }\n"
							+"\t\n"
							+"}\n")

							.replace("(roots)", roots))

					.evaluate(new AbstractTupleQueryResultHandler() {
						@Override public void handleSolution(final BindingSet solution) {

							final Value svalue=solution.getBinding("slot").getValue();
							final Value tvalue=solution.getBinding("term").getValue();

							final Term slot=term(svalue);
							final Term term=term(tvalue);

							if ( svalue instanceof Resource ) {
								if ( label ) { slot.setLabel(label((Resource)svalue, connection)); }
								if ( notes ) { slot.setNotes(notes((Resource)svalue, connection)); }
								if ( image ) { slot.setImage(image((Resource)svalue, connection)); }
								if ( point ) {
									slot.setLat(lat((Resource)svalue, connection)).setLng(lng((Resource)svalue, connection));
								}
							}

							if ( tvalue instanceof Resource ) {
								if ( label ) { term.setLabel(label((Resource)tvalue, connection)); }
								if ( notes ) { term.setNotes(notes((Resource)tvalue, connection)); }
								if ( image ) { term.setImage(image((Resource)tvalue, connection)); }
								if ( point ) {
									term.setLat(lat((Resource)tvalue, connection)).setLng(lng((Resource)tvalue, connection));
								}
							}

							List<Term> terms=slots.get(slot);

							if ( terms == null ) {
								slots.put(slot, terms=new ArrayList<>());
							}

							terms.add(term);

						}
					});

			connection

					.prepareTupleQuery(QueryLanguage.SPARQL, ("# leaves inverse\n\n"

							+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
							+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"

							+"select distinct\n"
							+"\n"
							+"\t?role ?role_label\n"
							+"\t?term ?term_label\n"
							+"\t\n"
							+"where {\n"

							+"\t{ (roots) }\n"
							+"\n"
							+"\t?term ?role ?root .\n"
							+"\t\n"
							+"\toptional { ?role rdfs:label ?role_label }\n"
							+"\toptional { ?term rdfs:label ?term_label }\n"
							+"\t\n"
							+"}\n")

							.replace("(roots)", roots))

					.evaluate(new AbstractTupleQueryResultHandler() {
						@Override public void handleSolution(final BindingSet solution) {

							final Value rvalue=solution.getBinding("role").getValue();
							final Value tvalue=solution.getBinding("term").getValue();

							final Term role=term(rvalue).reverse();
							final Term term=term(tvalue);

							if ( rvalue instanceof Resource ) {
								if ( label ) { role.setLabel(label((Resource)rvalue, connection)); }
								if ( notes ) { role.setNotes(notes((Resource)rvalue, connection)); }
								if ( image ) { role.setImage(image((Resource)rvalue, connection)); }
								if ( point ) {
									role.setLat(lat((Resource)rvalue, connection)).setLng(lng((Resource)rvalue, connection));
								}
							}

							if ( tvalue instanceof Resource ) {
								if ( label ) { term.setLabel(label((Resource)tvalue, connection)); }
								if ( notes ) { term.setNotes(notes((Resource)tvalue, connection)); }
								if ( image ) { term.setImage(image((Resource)tvalue, connection)); }
								if ( point ) {
									term.setLat(lat((Resource)tvalue, connection)).setLng(lng((Resource)tvalue, connection));
								}
							}

							List<Term> terms=roles.get(role);

							if ( terms == null ) {
								roles.put(role, terms=new ArrayList<>());
							}

							terms.add(term);

						}
					});


			// sort entries and clip lists

			for (final Map.Entry<Term, List<Term>> entry : slots.entrySet()) {

				final List<Term> terms=entry.getValue();

				Collections.sort(terms, Term.TextualOrder);

				entry.setValue(terms.subList(0, min(actual.getDetail(), terms.size())));
			}

			for (final Map.Entry<Term, List<Term>> entry : roles.entrySet()) {

				final List<Term> terms=entry.getValue();

				Collections.sort(terms, Term.TextualOrder);

				entry.setValue(terms.subList(0, min(actual.getDetail(), terms.size())));
			}

			final Map<Term, List<Term>> leaves=new LinkedHashMap<>();

			leaves.putAll(slots);
			leaves.putAll(roles);

			// !!! handle offset/limit

			return leaves;

		} catch ( RepositoryException
				|TupleQueryResultHandlerException
				|MalformedQueryException
				|QueryEvaluationException e ) {
			throw new RuntimeException(e);
		}
	}
}
