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

import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.beans.schemas.Geo;
import com.metreeca.self.shared.sparql.Client;
import com.metreeca.self.shared.sparql.Query;
import com.metreeca.self.shared.sparql.Table;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.*;
import org.eclipse.rdf4j.query.resultio.text.BooleanTextParser;
import org.eclipse.rdf4j.repository.*;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

import static com.metreeca.self.shared.beans.Term.blank;
import static com.metreeca.self.shared.beans.Term.named;
import static com.metreeca.self.shared.beans.Term.typed;

import static java.lang.Float.parseFloat;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;


@SuppressWarnings("NonJREEmulationClassesInClientCode")
public abstract class ShapeTest {

	protected static String[] annotations() {
		return new String[] {
				"raw",
				"label",
				"image",
				"point",
				"label/image/point"
		};
	}

	private static Repository[] repositories() {
		return new Repository[] {

				local(), // embedded rdf4j memory store (reference)

				//remote("http://localhost:8080/rdf4j-server/repositories/birt"), // rdf4j native store
				//remote("http://localhost:8080/fuseki/birt/query"), // fuseki

				//remote("http://localhost:5820/birt/query", "admin", "admin"), // stardog
				//remote("http://localhost:7200/repositories/birt"), // graphdb
				//remote("http://localhost:9999/blazegraph/namespace/birt/sparql"), // blazegraph
				//remote("http://localhost:8890/sparql"), // virtuoso (activate static patch below)

				// cloud services / make public and upload test set manually / don't include update credentials!

				//remote("https://rdf.s4.ontotext.com/4830526919/work/repositories/birt"), // ontotext s4
				//remote("http://dydra.com/metreeca/birt/sparql"), // dydra

		};
	}


	private static Repository local() {
		return new SailRepository(new MemoryStore());
	}

	private static Repository remote(final String url) {
		return remote(url, null, null);
	}

	private static Repository remote(final String url, final String usr, final String pwd) {

		final SPARQLRepository repository=new SPARQLRepository(url);

		repository.setUsernameAndPassword(usr, pwd);

		repository.setAdditionalHttpHeaders(singletonMap("Accept", "*/*"));

		return repository;
	}


	static { // ;(virtuoso) broken content-type in ASK response (https://github.com/openlink/virtuoso-opensource/issues/464)

		if ( true ) {

			BooleanQueryResultParserRegistry.getInstance().add(new BooleanQueryResultParserFactory() {

				@Override public BooleanQueryResultFormat getBooleanQueryResultFormat() {
					return new BooleanQueryResultFormat("HTML", "text/html", Charset.forName("US-ASCII"), "html");
				}

				@Override public BooleanQueryResultParser getParser() {
					return new BooleanTextParser();
				}

			});

		}

	}

	static { // !!! ;(dydra) no quality in content negotiation: remove broken file formats (https://github.com/dydra/support/issues/27)

		if ( false ) {

			//final TupleQueryResultParserRegistry registry=TupleQueryResultParserRegistry.getInstance();
			//
			//for (final String extension : asList(".csv", ".tsv", ".json")) {
			//	registry.getFileFormatForFileName(extension)
			//			.ifPresent(format -> registry.get(format).ifPresent(registry::remove));
			//}

		}

	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected static final Term SanFrancisco=named("tag:metreeca.net;2013:birt/offices/1");

	protected static final ValueFactory factory=SimpleValueFactory.getInstance();


	private static final List<RepositoryConnection> connections=new ArrayList<>();


	@BeforeClass public static void open() {
		for (final Repository repository : repositories()) {
			try {

				repository.initialize();

				final RepositoryConnection connection=repository.getConnection();

				if ( connection.isEmpty() ) {
					connection.add(ShapeTest.class.getResourceAsStream(
							"ShapeTest.nt"), "tag:metreeca.net;2013:birt", RDFFormat.NTRIPLES);
				}

				connections.add(connection);

			} catch ( final RepositoryException
					//|MalformedQueryException
					//|QueryEvaluationException
					|RDFParseException
					|IOException
					e
					) {
				throw new RuntimeException("unable to open connection to "+repository, e);
			}
		}
	}

	@AfterClass public static void close() {
		try {

			for (final RepositoryConnection connection : connections) {
				try {

					connection.close();
					connection.getRepository().shutDown();

				} catch ( final RepositoryException e ) {
					throw new RuntimeException("unable to close connection to "+connection.getRepository(), e);
				}
			}

		} finally {
			connections.clear();
		}
	}


	protected static Client dummy() { // dummy client for manual query inspection
		return new Client() {
			@Override public Query evaluate(final Query query) {

				System.out.println(query.getSource());
				System.out.flush();

				return query.setResults(new Table() {

					@Override public int rows() { return 0; }

					@Override public Term term(final int row, final String key) { return null; }

				}).done();
			}
		};
	}

	protected static Client client(final RepositoryConnection connection) {
		return new Client() {
			@Override public Query evaluate(final Query query) {
				try {

					System.out.println();
					System.out.println(query.getSource());
					System.out.flush();

					final List<String> variables=new ArrayList<>();
					final List<Map<String, Term>> tuples=new ArrayList<>();

					connection.prepareTupleQuery(QueryLanguage.SPARQL, query.getSource()).evaluate(new AbstractTupleQueryResultHandler() {

						@Override public void startQueryResult(final List<String> vars) {
							variables.addAll(vars);
						}

						@Override public void handleSolution(final BindingSet solution) {

							final Map<String, Term> tuple=new LinkedHashMap<>();

							for (final String variable : variables) {
								tuple.put(variable, term(solution.getValue(variable)));
							}

							tuples.add(tuple);
						}

					});

					return query.setResults(new Table() {

						@Override public int rows() {
							return tuples.size();
						}

						@Override public Term term(final int row, final String key) {

							final Map<String, Term> record=tuples.get(row);

							final Term term=record.get(key);

							return term == null ? null : term

									.setLabel(text(record.get(key+"_label")))
									.setNotes(text(record.get(key+"_notes")))
									.setImage(text(record.get(key+"_image")))

									.setLat(number(record.get(key+"_lat")))
									.setLng(number(record.get(key+"_lng")));

						}


						private String text(final Term term) {
							return term != null ? term.getText() : "";
						}

						private Float number(final Term term) {
							return term != null ? Float.valueOf(term.getText()) : Float.NaN;
						}

					}).done();

				} catch ( final QueryEvaluationException
						|TupleQueryResultHandlerException
						|MalformedQueryException
						|RepositoryException e ) {
					throw new RuntimeException(e);
				}

			}
		};
	}


	protected static RepositoryConnection reference(final Iterable<RepositoryConnection> connections) {

		final Iterator<RepositoryConnection> iterator=connections.iterator();

		if ( !iterator.hasNext() ) {
			throw new IllegalStateException("no available connection");
		}

		return iterator.next();
	}

	protected static Collection<RepositoryConnection> connections() {
		return unmodifiableList(connections);
	}


	protected static <I> List<I> sort(final List<I> entries, final Comparator<I> comparator) {

		final List<I> sorted=new ArrayList<>(entries);

		Collections.sort(sorted, comparator);

		return sorted;
	}

	protected static <K, V> Map<K, V> sort(final Map<K, V> entries, final Comparator<Entry<K, V>> comparator) {

		final List<Entry<K, V>> items=new ArrayList<>(entries.entrySet());

		Collections.sort(items, comparator);

		final Map<K, V> sorted=new LinkedHashMap<>();

		for (final Entry<K, V> item : items) {
			sorted.put(item.getKey(), item.getValue());
		}

		return sorted;
	}


	//// Typed Terms ///////////////////////////////////////////////////////////////////////////////////////////////////

	protected static Term integer(final int value) {
		return typed(BigInteger.valueOf(value));
	}


	//// BIRT Terms ////////////////////////////////////////////////////////////////////////////////////////////////////

	protected static Term birt(final String term) {
		return birt(term, false);
	}

	protected static Term birt(final String term, final boolean inverse) {
		return inverse ? named("tag:metreeca.net;2013:birt#", term).reverse() : named("tag:metreeca.net;2013:birt#", term);
	}


	//// Value > Term //////////////////////////////////////////////////////////////////////////////////////////////////

	protected static Term term(final Value value) {
		return value instanceof IRI ? term((IRI)value)
				: value instanceof BNode ? term((BNode)value)
				: value instanceof Literal ? term((Literal)value)
				: null;
	}

	protected static Term term(final IRI iri) {
		return iri == null ? null : named(iri.stringValue());
	}

	protected static Term term(final BNode blank) {
		return blank == null ? null : blank(blank.getID());
	}

	protected static Term term(final Literal literal) {
		return literal == null ? null
				: typed(literal.stringValue(), literal.getDatatype().stringValue());
				/* !!! review: getDatatype is never null
				: literal.getDatatype() != null ? typed(literal.stringValue(), literal.getDatatype().stringValue())
				: literal.getLanguage().map(language -> plain(literal.stringValue(), language)).orElse(plain(literal.stringValue()));
				*/
	}


	protected static String label(final Resource value, final RepositoryConnection connection) { // !!! assumes a single optional plain label
		try {

			final RepositoryResult<Statement> statements=connection.getStatements(value, RDFS.LABEL, null, true);

			final String label=statements.hasNext() ? statements.next().getObject().stringValue() : "";

			statements.close();

			return label;

		} catch ( final RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}

	protected static String notes(final Resource value, final RepositoryConnection connection) { // !!! assumes a single optional plain note
		try {

			final RepositoryResult<Statement> statements=connection.getStatements(value, RDFS.COMMENT, null, true);

			final String notes=statements.hasNext() ? statements.next().getObject().stringValue() : "";

			statements.close();

			return notes;

		} catch ( final RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}

	protected static float lng(final Resource value, final RepositoryConnection connection) {
		try {

			final IRI predicate=connection.getValueFactory().createIRI(Geo.GeoLong.getText());
			final RepositoryResult<Statement> statements=connection.getStatements(value, predicate, null, true);

			final float lng=statements.hasNext() ? parseFloat(statements.next().getObject().stringValue()) : Float.NaN;

			statements.close();

			return lng;

		} catch ( final RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}

	protected static float lat(final Resource value, final RepositoryConnection connection) {
		try {

			final IRI predicate=connection.getValueFactory().createIRI(Geo.GeoLat.getText());
			final RepositoryResult<Statement> statements=connection.getStatements(value, predicate, null, true);

			final float lat=statements.hasNext() ? parseFloat(statements.next().getObject().stringValue()) : Float.NaN;

			statements.close();

			return lat;

		} catch ( final RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}

	protected static String image(final Resource value, final RepositoryConnection connection) {
		try {

			final Collection<IRI> predicates=new ArrayList<>();

			for (final Term term : Specs.Images) {
				predicates.add(connection.getValueFactory().createIRI(term.getText()));
			}

			for (final IRI predicate : predicates) {

				final RepositoryResult<Statement> statements=connection.getStatements(value, predicate, null, true);

				final String image=statements.hasNext() ? statements.next().getObject().stringValue() : "";

				statements.close();

				if ( !image.isEmpty() ) { return image; }
			}

			return "";

		} catch ( final RepositoryException e ) {
			throw new RuntimeException(e);
		}
	}


	//// Term > Value //////////////////////////////////////////////////////////////////////////////////////////////////

	protected static Value value(final Term term) {
		return term == null ? null
				: term.isBlank() ? factory.createBNode(term.getText())
				: term.isNamed() ? factory.createIRI(term.getText())
				: term.isSimple() ? factory.createLiteral(term.getText())
				: term.isTagged() ? factory.createLiteral(term.getText(), term.getLang())
				: factory.createLiteral(term.getText(), factory.createIRI(term.getType()));
	}


	//// Lenient Value-Based Equality //////////////////////////////////////////////////////////////////////////////////

	private static final float FloatTolerance=1e-5f;
	private static final double DoubleTolerance=1e-5d;
	private static final BigDecimal DecimalTolerance=new BigDecimal("1e-5");


	protected static boolean equals(final Term x, final Term y) { // lenient null-aware and value-based comparison
		return x == null ? y == null : x.equals(y) || x.getValue() != null && y != null && equals(x.getValue(), y.getValue());
	}


	private static boolean equals(final Object x, final Object y) {
		return x == null ? y == null : x.equals(y)
				|| x instanceof Float && y instanceof Number && equals((float)x, ((Number)y).floatValue())
				|| x instanceof Double && y instanceof Number && equals((double)x, ((Number)y).doubleValue())
				|| x instanceof BigDecimal && y instanceof BigDecimal && equals((BigDecimal)x, (BigDecimal)y)
				|| x instanceof BigDecimal && y instanceof BigInteger && equals((BigDecimal)x, new BigDecimal((BigInteger)y))
				|| x instanceof BigDecimal && y instanceof Number && equals((BigDecimal)x, BigDecimal.valueOf(((Number)y).doubleValue()));
	}

	private static boolean equals(final float x, final float y) {
		return Float.compare(x, y) == 0 || abs((x-y)/max(x, y)) <= FloatTolerance;
	}

	private static boolean equals(final double x, final double y) {
		return Double.compare(x, y) == 0 || abs((x-y)/max(x, y)) <= DoubleTolerance;
	}

	private static boolean equals(final BigDecimal x, final BigDecimal y) {
		return x.compareTo(y) == 0 || x.subtract(y).abs().divide(x.max(y), RoundingMode.UP).compareTo(DecimalTolerance) <= 0;
	}


	//// Auto-Closeable Memory Store ///////////////////////////////////////////////////////////////////////////////////

	protected static final class Store implements AutoCloseable {

		private RepositoryConnection connection;


		public RepositoryConnection open() throws RepositoryException {

			if ( connection == null ) {

				final Repository repository=new SailRepository(new MemoryStore());

				repository.initialize();

				connection=repository.getConnection();

			}

			return connection;
		}

		public RepositoryConnection open(final String turtle) throws RepositoryException, RDFParseException, IOException {

			final RepositoryConnection connection=open();

			connection.setNamespace("birt", "tag:metreeca.net;2013:birt#");
			connection.setNamespace("rdf", RDF.NAMESPACE);
			connection.setNamespace("rdfs", RDFS.NAMESPACE);
			connection.setNamespace("owl", OWL.NAMESPACE);

			connection.add(new StringReader(turtle), "http://example.org/", RDFFormat.TURTLE);

			return connection;
		}


		@Override public void close() throws RepositoryException {
			if ( connection != null ) {
				try {
					connection.close();
					connection.getRepository().shutDown();
				} finally {
					connection=null;
				}
			}
		}

	}

}
