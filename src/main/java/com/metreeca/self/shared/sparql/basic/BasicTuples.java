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

package com.metreeca.self.shared.sparql.basic;

import com.metreeca.self.shared.async.Morpher;
import com.metreeca.self.shared.async.Promise;
import com.metreeca.self.shared.beans.*;
import com.metreeca.self.shared.forms.Tuples;
import com.metreeca.self.shared.sparql.*;

import java.util.*;

import static com.metreeca.self.shared.async.Promises.promise;

import static java.lang.Float.isNaN;
import static java.util.Collections.singletonList;
import static java.util.Collections.sort;


public final class BasicTuples extends Editor {

	public Promise<List<List<Term>>> entries(final Tuples tuples, final Client client) {

		if ( tuples == null ) {
			throw new NullPointerException("null tuples");
		}

		final Specs specs=tuples.getSpecs();

		final String pattern=specs.getPattern();
		final List<Path> fields=specs.getFields();

		final int offset=tuples.getOffset();
		final int limit=tuples.getLimit();
		final int sample=tuples.getSample();

		final boolean label=tuples.getLabel();
		final boolean notes=tuples.getNotes();
		final boolean image=tuples.getImage();
		final boolean point=tuples.getPoint();

		final List<Integer> vars=new ArrayList<>(); // map field index to projected flake variable

		final Flake flake=new Flake().specs(specs);
		final Term singleton=flake.term();

		return specs.isVoid() ? promise(Collections.<List<Term>>emptyList())

				// optimize singletons // !!! broken src indentation

				: singleton != null && fields.size() == 1
				&& (!label || !singleton.getLabel().isEmpty())
				&& (!notes || !singleton.getNotes().isEmpty())
				&& (!image || !singleton.getImage().isEmpty())
				&& (!point || !isNaN(singleton.getLat()) && !isNaN(singleton.getLng()))

				? promise(singletonList(singletonList(singleton)))

				: client.evaluate(new Query()

				.setServer(tuples.getEndpoint())
				.setTimeout(tuples.getTimeout())

				.setSource(new Editor() {

					{

						info("tuples", tuples);

						text("select\f\t");

						fields(fields, flake);

						text("\b\fwhere {\f");

						core(flake, null, sample, pattern);
						annotations(flake, null, label, notes, image, point);

						text("\f}");

						text("\forder by\f\t");

						criteria(fields, flake);

						text("\b\f");

						offset(offset);
						limit(limit);

					}

					private void fields(final Iterable<Path> fields, final Flake flake) {

						for (final Path field : fields) {

							final Integer var=id(flake.path(field), field.getTransform(), field.getSummary());

							if ( !vars.contains(var) ) { // project only once

								if ( flake.probe(field).isLiteral() ) {
									var(var);
								} else {
									projection(var, label, notes, image, point);
								}

								text('\n');
							}

							vars.add(var);
						}
					}

					private void criteria(final List<Path> fields, final Flake flake) {

						final List<Path> order=new ArrayList<>(fields);

						// order fields by sorting priority (0 -> unsorted)

						sort(order, new Comparator<Path>() {
							@Override public int compare(final Path x, final Path y) {

								final int xSort=x.getSort();
								final int xKey=(xSort > 0) ? xSort : xSort < 0 ? -xSort : Integer.MAX_VALUE;

								final int ySort=y.getSort();
								final int yKey=(ySort > 0) ? ySort : ySort < 0 ? -ySort : Integer.MAX_VALUE;

								return Integer.compare(xKey, yKey);
							}
						});

						for (final Path field : order) {

							final int index=fields.indexOf(field); // get the index of the field in the specs
							final int var=vars.get(index); // map the field to a (possibly shared) var

							// ;(virtuoso) breaks on multiple sorting criteria on the same variable

							if ( vars.indexOf(var) == index ) { // prevent multiple criteria on the same variable

								// including only the first criterion won't alter sorting:
								// - if the first one is <> 0, following ones won't be considered
								// - if the first one is 0, following ones will be 0 as well and immaterial

								final boolean reverse=field.getSort() < 0;

								if ( flake.probe(field).isLiteral() ) {
									criterion(var, reverse, false, false, false, false);
								} else {
									criterion(var, reverse, label, notes, image, point);
								}

								text('\n');
							}

						}

					}

				}.text()))

				.pipe(new Morpher<Query, List<List<Term>>>() {

					@Override public Promise<List<List<Term>>> value(final Query query) {

						final Table table=query.getResults();
						final List<List<Term>> entries=new ArrayList<>();

						for (int r=0, rows=table.rows(), cols=fields.size(); r < rows; ++r) {

							final List<Term> tuple=new ArrayList<>();

							for (int c=0; c < cols; ++c) {

								final Term term=table.term(r, String.valueOf(vars.get(c)));

								tuple.add(fields.get(c).isCollection() ? term.reverse() : term);
							}

							entries.add(tuple);
						}

						return promise(entries);

					}

				});
	}

}
