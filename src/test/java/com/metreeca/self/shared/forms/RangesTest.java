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
import com.metreeca.self.shared.sparql.Engine;
import com.metreeca.self.shared.sparql.Stats;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.junit.ComparisonFailure;

import java.util.*;


public abstract class RangesTest extends ShapeTest {

	protected static void assertEquals(final String message, final String expected, final Ranges actual) {

		final Engine engine=new Engine();
		final Iterable<RepositoryConnection> connections=connections();

		for (final String annotation : annotations()) {

			actual // test with different annotation settings

					.setLabel(annotation.contains("label"))
					.setNotes(annotation.contains("note"))
					.setImage(annotation.contains("image"))
					.setPoint(annotation.contains("point"));

			final List<Stats> reference=ranges(expected, actual, reference(connections));

			for (final RepositoryConnection connection : connections) {

				final String endpoint=connection.getRepository().toString();
				final String setup=annotation+" @ "+endpoint;

				engine

						.ranges(actual.setEndpoint(endpoint), client(connection))

						.then(new Handler<List<Stats>>() {

							@Override public void value(final List<Stats> entries) {
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
			final List<Stats> expected, final List<Stats> actual) {

		final int es=expected.size();
		final int as=actual.size();

		if ( es != as ) {
			throw new ComparisonFailure(message+": mismatched entry count ("+es+" != "+as+") [ "+setup+" ]",
					expected.toString(), actual.toString());
		}

		final Iterator<Stats> ei=expected.iterator();
		final Iterator<Stats> ai=actual.iterator();

		for (int i=0; ei.hasNext(); ++i) {

			final Stats e=ei.next();
			final Stats a=ai.next();

			if ( !Objects.equals(e.getType(), a.getType()) ) {
				throw new ComparisonFailure(message+": mismatched stats type @ "+i
						+" ('"+e.getType()+"' != '"+a.getType()+"') [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			if ( e.getSamples() != a.getSamples() ) {
				throw new ComparisonFailure(message+": mismatched stats samples @ "+i
						+" ('"+e.getSamples()+"' != '"+a.getSamples()+"') [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			if ( !Objects.equals(e.getMin(), a.getMin()) ) {
				throw new ComparisonFailure(message+": mismatched stats minimum @ "+i
						+" ('"+e.getMin()+"' != '"+a.getMin()+"') [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

			if ( !Objects.equals(e.getMax(), a.getMax()) ) {
				throw new ComparisonFailure(message+": mismatched stats maximum @ "+i
						+" ('"+e.getMax()+"' != '"+a.getMax()+"') [ "+setup+" ]",
						expected.toString(), actual.toString());
			}

		}
	}

	private static List<Stats> ranges(final String reference, final Ranges ranges, final RepositoryConnection connection) { // !!! refactor
		try {

			final List<Stats> entries=new ArrayList<>();

			final String target=reference.replaceAll("\\s*select\\s+(\\?\\S+).*", "$1");

			connection.prepareTupleQuery(QueryLanguage.SPARQL, ("# values\n\n"

							+"prefix birt: <tag:metreeca.net;2013:birt#>\n"
							+"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n\n"
							+"prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n\n"

							+"select ?type (count((@)) as ?count) (min((@)) as ?min) (max((@)) as ?max) {"
							+" { "+reference+" }"
							+" bind(datatype((@)) as ?type)"
							+" }"
							+" group by ?type"
							+" order by desc(?count) ?type"

					).replace("(@)", target)

			).evaluate(new AbstractTupleQueryResultHandler() {
				@Override public void handleSolution(final BindingSet solution) {

					final String type=term(solution.getValue("type")).getText();
					final int count=((Number)term(solution.getValue("count")).getValue()).intValue();

					final Object min=term(solution.getValue("min")).getValue();
					final Object max=term(solution.getValue("max")).getValue();

					entries.add(new Stats(type, count, min, max));

				}
			});

			return entries;

		} catch ( final QueryEvaluationException
				|TupleQueryResultHandlerException
				|MalformedQueryException
				|RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}
}
