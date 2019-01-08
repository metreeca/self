/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca.
 *
 * Metreeca is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or(at your option) any later version.
 *
 * Metreeca is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with Metreeca.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.shared.sparql.basic;

import com.metreeca._jeep.shared.async.Morpher;
import com.metreeca._jeep.shared.async.Promise;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Shoots;
import com.metreeca.self.shared.sparql.*;

import java.util.ArrayList;
import java.util.List;

import static com.metreeca._jeep.shared.async.Promises.lists;
import static com.metreeca._jeep.shared.async.Promises.promise;

import static java.lang.Math.min;


public class BasicShoots {

	public Promise<List<Term>> entries(final Shoots shoots, final Client client) {
		return lists(
				shoots(shoots, client, false),
				shoots(shoots, client, true));
	}

	private Promise<List<Term>> shoots(final Shoots shoots, final Client client, final boolean inverse) {

		final Specs specs=shoots.getSpecs();
		final List<Term> path=shoots.getPath();

		final int sample=min(shoots.getSample(), 1000); // ;(graphdb) linear elapsed > limit sampling

		final boolean label=shoots.getLabel();
		final boolean notes=shoots.getNotes();
		final boolean image=false; // properties are not pictured
		final boolean point=false; // properties are not georeferenced

		return client

				.evaluate(new Query()

						.setServer(shoots.getEndpoint())
						.setTimeout(shoots.getTimeout())

						.setSource(new Editor() {{

							info(inverse ? "shoots/inverse" : "shoots/direct", shoots);

							text("select distinct\t\f");

							projection("link", label, notes, image, point);

							text("\b\fwhere {\f");

							// select unique links before retrieving annotations (much faster at least on virtuoso)

							text("{ select distinct ?link where {\f");

							final Flake flake=new Flake().specs(specs);
							final Term singleton=flake.term();

							if ( path.isEmpty() && singleton != null ) { // optimize for singletons

								text(inverse
												? "?term ?link (0) .\f"
												: "(0) ?link ?term .\f"
										, singleton.format());

							} else {

								final Integer hook=hook(flake,
										null, false, new Path(path), sample, specs.getPattern()
								);

								text(inverse // hook is required > no guards needed
												? "?term ?link ?(0) .\f"
												: "?(0) ?link ?term .\f"
										, hook);

							}

							text("\f} }\f");

							annotations("link", label, notes, image, point);

							text("\f}\f");

							text("order by\f\t");

							criterion("link", false, label, notes, image, point);

							text("\f\b");

							offset(shoots.getOffset());
							limit(shoots.getLimit());

						}}.text()))

				.pipe(new Morpher<Query, List<Term>>() {
					@Override public Promise<List<Term>> value(final Query query) {

						final Table table=query.getResults();

						final List<Term> entries=new ArrayList<>();

						for (int r=0, rows=table.rows(); r < rows; ++r) {
							entries.add(inverse ? table.term(r, "link").reverse() : table.term(r, "link"));
						}

						return promise(entries);

					}
				});
	}

}
