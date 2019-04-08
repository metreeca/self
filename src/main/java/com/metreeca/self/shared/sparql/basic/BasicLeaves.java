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

package com.metreeca.self.shared.sparql.basic;

import com.metreeca.self.shared.async.Morpher;
import com.metreeca.self.shared.async.Promise;
import com.metreeca.self.shared.async.Promises;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Leaves;
import com.metreeca.self.shared.sparql.*;

import java.util.*;
import java.util.Map.Entry;

import static com.metreeca.self.shared.async.Promises.all;
import static com.metreeca.self.shared.async.Promises.maps;
import static com.metreeca.self.shared.async.Promises.promise;

import static java.lang.Math.max;


public final class BasicLeaves {

	public static final int MaxDetail=100; // the maximum fan-in/out for properties to be included in omnibus batches


	public Promise<Map<Term, List<Term>>> entries(final Leaves leaves, final Client client) {
		return maps(spine(leaves, client, false), spine(leaves, client, true))

				.pipe(new Morpher<Map<Term, Integer>, List<Map<Term, Term>>>() {
					@Override public Promise<List<Map<Term, Term>>> value(final Map<Term, Integer> spine) { // define batches

						// store batches as auto-associative maps to support recovering of annotated terms during chunk assembly

						final List<Map<Term, Term>> batches=new ArrayList<>();

						final Map<Term, Term> recto=new HashMap<>(); // omnibus batch for direct predicates with low fan-out
						final Map<Term, Term> verso=new HashMap<>(); // omnibus batch for inverse predicates with low fan-in


						for (final Entry<Term, Integer> entry : spine.entrySet()) {

							final Term term=entry.getKey();
							final Integer count=entry.getValue();

							if ( count > max(MaxDetail, leaves.getDetail()) ) {

								batches.add(Collections.singletonMap(term, term)); // large fan-in/out: add to a dedicated batch

							} else if ( term.isRecto() ) { // small fan-out: add to the omnibus

								recto.put(term, term);

							} else if ( term.isVerso() ) { // small fan-in: add to the omnibus

								verso.put(term, term);

							}
						}

						if ( !recto.isEmpty() ) { batches.add(recto); }
						if ( !verso.isEmpty() ) { batches.add(verso); }

						return promise(batches);

					}
				})

				.pipe(new Morpher<List<Map<Term, Term>>, Map<Term, List<Term>>>() {
					@Override public Promise<Map<Term, List<Term>>> value(final List<Map<Term, Term>> batches) { // retrieve and assemble chunks

						final Collection<Promise<Map<Term, List<Term>>>> chunks=new ArrayList<>();

						for (final Map<Term, Term> batch : batches) {
							chunks.add(chunk(leaves, client, batch));
						}

						// merge chunks ordering links by human-readable representation

						return all(chunks).pipe(new Morpher<List<Map<Term, List<Term>>>, Map<Term, List<Term>>>() {
							@Override public Promise<Map<Term, List<Term>>> value(final List<Map<Term, List<Term>>> chunks) {

								final Map<Term, List<Term>> ordered=new TreeMap<>(Term.TextualOrder);

								for (final Map<Term, List<Term>> chunk : chunks) {
									ordered.putAll(chunk); // no need to merge leave lists: each chunk has a unique set of key links
								}

								// convert to linked hash to allow lookup by raw term while preserving order

								return Promises.<Map<Term, List<Term>>>promise(new LinkedHashMap<>(ordered));

							}
						});

					}
				});
	}


	private Promise<Map<Term, Integer>> spine(final Leaves leaves, final Client client, final boolean inverse) {

		final Specs specs=leaves.getSpecs();

		final int sample=leaves.getSample();

		final boolean label=leaves.getLabel();
		final boolean notes=leaves.getNotes();
		final boolean image=false; // properties are not pictured
		final boolean point=false; // properties are not georeferenced

		return client

				.evaluate(new Query()

						.setServer(leaves.getEndpoint())
						.setTimeout(leaves.getTimeout())

						.setSource(new Editor() {{

							info(inverse ? "leaves/spine/inverse" : "leaves/spine/direct", leaves);

							text("select\t\f");

							projection("link", label, notes, image, point);

							text(" ?count");

							text("\b\fwhere {\n\n");

							text("{ select ?link (count(distinct ?term) as ?count) {\f");

							final Flake flake=new Flake().specs(specs);
							final Term singleton=flake.term();

							if ( singleton != null ) { // optimize for singletons

								text(inverse ? "?term ?link (0) ." : "(0) ?link ?term .", singleton.format());

							} else {

								final Integer hook=hook(flake,
										null, false, new Path(), sample, specs.getPattern()
								);

								// hook directly: root terms are never unbound

								text(inverse ? "?term ?link ?(0) ." : "?(0) ?link ?term .", hook);

							}

							// aggregate filter guards against unbound ?link projected toward following optional clauses
							// ;(virtuoso) can't refer to the projected aggregate ?count in filter (correctly strictly standard-wise)

							text("\f} group by ?link having (count(distinct ?term) > 0) }\f");

							annotations("link", label, notes, image, point);

							text("\f}\f");

							text("order by\f\t");

							criterion("link", false, label, notes, image, point);

							text("\b\n");

						}}.text()))

				.pipe(new Morpher<Query, Map<Term, Integer>>() {
					@Override public Promise<Map<Term, Integer>> value(final Query query) {

						final Table table=query.getResults();
						final Map<Term, Integer> spine=new LinkedHashMap<>();

						for (int r=0, rows=table.rows(); r < rows; ++r) {

							final Term link=inverse ? table.term(r, "link").reverse() : table.term(r, "link");
							final Integer count=((Number)table.term(r, "count").getValue()).intValue();

							spine.put(link, count);
						}

						return promise(spine);

					}
				});
	}

	private Promise<Map<Term, List<Term>>> chunk(final Leaves leaves, final Client client, final Map<Term, Term> batch) {

		final Specs specs=leaves.getSpecs();
		final int detail=leaves.getDetail();

		final int sample=leaves.getSample();

		final boolean label=leaves.getLabel();
		final boolean notes=leaves.getNotes();
		final boolean image=leaves.getImage();
		final boolean point=leaves.getPoint();

		final Term first=batch.values().iterator().next();

		final boolean inverse=first.isVerso();
		final boolean dedicated=batch.size() == 1;
		final boolean sampling=dedicated && sample > 0;

		return client

				.evaluate(new Query()

						.setServer(leaves.getEndpoint())
						.setTimeout(leaves.getTimeout())

						.setSource(new Editor() {{

							info(inverse ? "leaves/chunk/inverse" : "leaves/chunk/direct", leaves);

							text("select distinct\t\f");

							if ( !dedicated ) { text("?link"); }

							projection("term", label, notes, image, point);

							text("\b\fwhere {\f");

							if ( sampling ) {
								text("{ select ");
								projection("term", label, notes, image, point);
								text(" {\f");
							}

							final Flake flake=new Flake().specs(specs);
							final Term singleton=flake.term();

							final String predicate=dedicated ? (inverse ? first.reverse() : first).format() : "?link";

							if ( singleton != null ) { // optimize for singletons

								text(inverse ? "?term (1) (0) ." : "(0) (1) ?term .", singleton.format(), predicate);

							} else {

								final Integer hook=hook(flake,
										null, false, new Path(), sample, specs.getPattern()
								);

								// hook directly: root terms are never unbound

								text(inverse ? "?term (1) ?(0) ." : "?(0) (1) ?term .", hook, predicate);

							}

							text("\f");

							annotations("term", label, notes, image, point);

							if ( sampling ) {
								text("\f} limit ");
								text(sample);
								text(" }\f");
							}

							text("\f}\f");

							text("order by\t\f");

							criterion("term", false, label, notes, image, point);

							// ;(virtuoso) not in criterion to preserve limited sort keys
							// ;(virtuoso) test datatype to prevent Virtuoso 22026 Error: Sorting key too long in order by key

							text(" asc(if(datatype(?term), str(?term), ''))"); // sort unsupported typed literals

							text("\b\f");

							if ( dedicated ) {

								limit(detail);

							} else {

								text("values ?link {\f");

								for (final Term term : batch.values()) {
									text((inverse ? term.reverse() : term).format());
									text('\n');
								}

								text("\n}\n");

							}

						}}.text()))

				.pipe(new Morpher<Query, Map<Term, List<Term>>>() {
					      @Override public Promise<Map<Term, List<Term>>> value(final Query query) {

						      final Table table=query.getResults();

						      final Map<Term, List<Term>> entries=new LinkedHashMap<>();

						      for (int r=0, rows=table.rows(); r < rows; ++r) {

							      final Term link=dedicated ? first // recover annotated links from batch
									      : batch.get(inverse ? table.term(r, "link").reverse() : table.term(r, "link"));

							      final Term term=table.term(r, "term");

							      List<Term> terms=entries.get(link);

							      if ( terms == null ) {
								      entries.put(link, terms=new ArrayList<>());
							      }

							      if ( terms.size() < detail ) {
								      terms.add(term);
							      }
						      }

						      return promise(entries);
					      }
				      }

				);
	}

}
