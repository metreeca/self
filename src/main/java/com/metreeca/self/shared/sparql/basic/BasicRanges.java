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

import com.metreeca._jeep.shared.async.Morpher;
import com.metreeca._jeep.shared.async.Promise;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Ranges;
import com.metreeca.self.shared.sparql.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.metreeca._jeep.shared.async.Promises.promise;


public final class BasicRanges {

	public Promise<List<Stats>> entries(final Ranges ranges, final Client client) {

		if ( ranges == null ) {
			throw new NullPointerException("null ranges");
		}

		final Specs specs=ranges.getSpecs();
		final String pattern=specs.getPattern();
		final Path path=ranges.getPath();

		final int offset=ranges.getOffset();
		final int limit=ranges.getLimit();
		final int sample=ranges.getSample();

		return specs.isVoid() ? promise(Collections.<Stats>emptyList()) : client

				.evaluate(new Query()

						.setServer(ranges.getEndpoint())
						.setTimeout(ranges.getTimeout())

						.setSource(new Editor() {{

							final int term=id();
							final Flake flake=new Flake().specs(specs);

							info("ranges", ranges);

							text("select\f\t");

							text(" ?type\n");
							text(" (min(?(0)) as ?min)\n", term);
							text(" (max(?(0)) as ?max)\n", term);
							text(" (count(?(0)) as ?count)\n", term); // 'distinct' already included in inner 'core' select

							text("\b\fwhere {\f");

							hook(flake, term, true, path, sample, pattern);

							text("bind(datatype(?(0)) as ?type)\f", term);

							text("\f}\f");

							text("group by ?type\n");
							text("order by desc(?count) ?type\n");

							text("\f");

							offset(offset);
							limit(limit);

						}}.text()))

				.pipe(new Morpher<Query, List<Stats>>() {
					@Override public Promise<List<Stats>> value(final Query query) {

						final Table table=query.getResults();

						final List<Stats> entries=new ArrayList<>();

						for (int r=0, rows=table.rows(); r < rows; ++r) {

							final Term type=table.term(r, "type");
							final Term min=table.term(r, "min");
							final Term max=table.term(r, "max");
							final int count=((Number)table.term(r, "count").getValue()).intValue();

							entries.add(new Stats(type == null ? "" : type.getText(), count,
									min == null ? null : min.getValue(), max == null ? null : max.getValue()));
						}

						return promise(entries);
					}
				});
	}
}
