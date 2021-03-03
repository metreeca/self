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
import com.metreeca.self.shared.forms.Values;
import com.metreeca.self.shared.sparql.*;

import java.util.*;

import static com.metreeca.self.shared.async.Promises.promise;


public final class BasicValues {

	public Promise<Map<Term, Integer>> entries(final Values values, final Client client) {

		if ( values == null ) {
			throw new NullPointerException("null values");
		}

		final Specs specs=values.getSpecs();
		final String pattern=specs.getPattern();
		final Path path=values.getPath();

		final int offset=values.getOffset();
		final int limit=values.getLimit();
		final int sample=values.getSample();

		final List<String> vars=new ArrayList<>(); // record projected flake variable associated to target path

		return specs.isVoid() ? promise(Collections.<Term, Integer>emptyMap()) : client

				.evaluate(new Query()

						.setServer(values.getEndpoint())
						.setTimeout(values.getTimeout())

						.setSource(new Editor() {{

							final int term=id();
							final Flake flake=new Flake().specs(specs);
							final Probe probe=flake.probe(path);

							// aggregates and derivates are assumed to be literals > retrieve no annotations

							final boolean annotated=path.isPlain() && (probe == null || !probe.isLiteral());

							final boolean label=annotated && values.getLabel();
							final boolean notes=annotated && values.getNotes();
							final boolean image=annotated && values.getImage();
							final boolean point=annotated && values.getPoint();

							vars.add(String.valueOf(term));

							info("values", values);

							text("select\f\t");

							projection(term, label, notes, image, point);

							text(" (count(*) as ?count)"); // 'distinct' already included in inner 'core' select

							text("\b\fwhere {\f");

							hook(flake, term, true, path, sample, pattern);
							annotations(term, label, notes, image, point);

							text("\f}");

							text("\fgroup by\f\t");

							projection(term, label, notes, image, point);

							// ;(virtuoso) can't refer to the projected aggregate ?count in filter (correctly, strictly standard-wise)

							text("\b\fhaving\f\t(count(*) > 0)\b\f");

							text("\forder by\f\t");

							text("desc(?count) ");

							criterion(term, false, label, notes, image, point);

							text("\b\f");

							offset(offset);
							limit(limit);

						}}.text()))

				.pipe(new Morpher<Query, Map<Term, Integer>>() {

					@Override public Promise<Map<Term, Integer>> value(final Query query) {

						final Table table=query.getResults();

						final boolean collection=path.isCollection();
						final Map<Term, Integer> entries=new LinkedHashMap<>();

						// recover term variable from table to avoid issues with aliased variables and aggregates

						// ;(blazegraph) won't sort on aliased variables
						// ;(virtuoso) requires both original and aliased variables in 'group by' to sort on originals

						final String var=vars.get(0);

						for (int r=0, rows=table.rows(); r < rows; ++r) {

							final Term term=table.term(r, var);
							final int count=((Number)table.term(r, "count").getValue()).intValue();

							entries.put(collection ? term.reverse() : term, count);
						}

						return promise(entries);

					}

				});
	}

}
