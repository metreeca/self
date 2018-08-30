/*
 * Copyright © 2013-2018 Metreeca srl. All rights reserved.
 *
 * This file is part of Metreeca/Self.
 *
 * Metreeca/Self is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Metreeca/Self is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Metreeca/Self. If not, see <http://www.gnu.org/licenses/>.
 */

package com.metreeca.self.client.faces.line;

import com.metreeca._tile.client.js.JSArray;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.client.faces.Model;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Tuples;

import java.util.ArrayList;
import java.util.List;

import static com.metreeca._tile.client.js.JSArray.array;
import static com.metreeca._tile.client.js.JSObject.object;


final class LineModel extends Model<LineModel> {

	public static final int SeriesLimit=10; // !!! implement // !!! configure from line chart tool
	public static final int PointLimit=100; // the maximum number of handled points // !!! configure from line chart tool

	public static final JSObject defs=object("{}")

			.set("area", object()

					.set("top", 0.0f)
					.set("left", 0.125f)
					.set("bottom", 0.025f)
					.set("right", 0.075f)
					.set("aspect", 4/3.0f))

			.set("abscissa", object()

					.set("origin", false)
					.set("grid", false)
					.set("hair", true)
					.set("offset", 0))

			.set("ordinate", object()

					.set("origin", false)
					.set("grid", false)
					.set("hair", true)
					.set("offset", 0));


	private JSObject options; // user defined options

	private int item;
	private int x;
	private int[] ys;

	private JSObject setup; // options+series
	private JSArray<JSObject> data;


	public JSObject options() {

		if ( options == null ) {
			throw new IllegalStateException("undefined options");
		}

		return options; // !!! on demand
	}


	public JSObject setup() {

		if ( setup == null ) {
			throw new IllegalStateException("undefined setup");
		}

		return setup; // !!! on demand
	}

	public JSArray<JSObject> data() {

		if ( data == null ) {
			throw new IllegalStateException("undefined data");
		}

		return data; // !!! on demand
	}


	public boolean isValue(final Path field) {
		return isNumber(field);
	}

	public boolean isItem(final Path field) {
		return isLabel(field);
	}


	public Path item() {
		return path(item);
	}

	public Term item(final JSObject cell) {
		return term(cell, item);
	}


	public Path x() {
		return path(x);
	}

	public Term x(final JSObject cell) {
		return term(cell, x);
	}


	public Path y(final int index) {
		return path(ys[index]);
	}

	public Term y(final int index, final JSObject cell) {
		return term(cell, ys[index]);
	}


	@Override public Report.Line lens(final boolean writable) { // !!! review
		return (Report.Line)super.lens(writable);
	}

	@Override protected LineModel update() { // !!! refactor

		final Report report=report();
		final Tuples tuples=tuples();

		options=report == null ? null : object(lens(false).getSetup());

		if ( report != null && tuples != null ) {

			final Report.Line lens=lens(false);

			if ( !tuples.getEntries().isEmpty() ) { // only if real stats are available

				// classify fields

				final List<Path> fields=report.getSpecs().getFields();
				final List<Path> labels=new ArrayList<>();
				final List<Path> numbers=new ArrayList<>();

				for (final Path field : fields) {
					if ( isItem(field) ) { labels.add(field); }
					if ( isValue(field) ) { numbers.add(field); }
				}

				// check role compatibility

				for (final Path hidden : new ArrayList<>(lens.getHidden())) { // concurrent modification

					final boolean label=labels.remove(hidden);
					final boolean number=numbers.remove(hidden);

					if ( !label && !number ) {
						lens.removeHidden(hidden);
					}
				}

				if ( !labels.remove(lens.getItem()) ) { lens.setItem(null); }

				if ( !numbers.remove(lens.getX()) ) { lens.setX(null); }

				for (final Path value : new ArrayList<>(lens.getYs())) { // concurrent modification
					if ( !numbers.remove(value) ) { lens.removeY(value); }
				}

				// auto assign missing roles

				if ( lens.getItem() == null && !labels.isEmpty() ) { lens.setItem(labels.remove(0)); }

				if ( lens.getX() == null && !numbers.isEmpty() ) { lens.setX(numbers.remove(0)); }

				while ( !numbers.isEmpty() ) { lens.insertY(numbers.remove(0)); }
			}

			// record mapping

			this.item=series(lens.getItem());

			this.x=series(lens.getX());
			this.ys=series(lens.getYs());

			// create setup

			setup=object(lens.getSetup());

			setup.set("key", "@");
			setup.set("item", key(item));
			setup.set("x", key(x));
			setup.set("y", key(ys));

			setup.set("abscissa.label", label(path(x)));

			// create data

			data=array();

			for (final List<Term> tuple : tuples.getEntries()) {

				final JSObject record=object();

				if ( item >= 0 ) { record.set(key(item), label(tuple.get(item))); }

				if ( x >= 0 ) { record.set(key(x), label(tuple.get(x))); }

				for (final int y : ys) { record.set(key(y), number(tuple.get(y))); }

				data.push(record.set("@", key(tuple)));
			}

		} else {

			setup=null;
			data=null;

			item=-1;

			x=-1;
			ys=null; // !!! vs []
		}

		return this;
	}

}
