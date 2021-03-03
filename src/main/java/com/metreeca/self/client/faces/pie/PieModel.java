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

package com.metreeca.self.client.faces.pie;

import com.metreeca._tile.client.js.JSArray;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.client.faces.Model;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Tuples;

import java.util.*;

import static com.metreeca._tile.client.js.JSArray.array;
import static com.metreeca._tile.client.js.JSObject.object;

import static java.util.Collections.singleton;


final class PieModel extends Model<PieModel> {

	public static final int SliceLimit=20; // the maximum number of handled slices // !!! configure from pie chart tool

	public static final JSObject defs=object("{}")

			.set("area", object()

					.set("top", 0.0f)
					.set("left", 0.1f)
					.set("bottom", 0.025f)
					.set("right", 0.075f)
					.set("aspect", 4/3.0f))

			.set("inner", "0.5");


	private JSObject options; // user defined options

	private int item;
	private int value;

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


	public boolean isItem(final Path path) {
		return isAnything(path);
	}

	public boolean isValue(final Path path) {
		return isNumber(path);
	}


	public Path item() {
		return path(item);
	}

	public Term item(final JSObject cell) {
		return term(cell, item);
	}


	public Path value() {
		return path(value);
	}

	public Term value(final JSObject cell) {
		return term(cell, value);
	}


	@Override public Report.Pie lens(final boolean writable) { // !!! review
		return (Report.Pie)super.lens(writable);
	}

	@Override protected PieModel update() { // !!! refactor

		final Report report=report();
		final Tuples tuples=tuples();

		options=report == null ? null : object(lens(false).getSetup());

		if ( report != null && tuples != null ) {

			final Report.Pie lens=lens(false);

			if ( !tuples.getEntries().isEmpty() ) { // only if real stats are available

				final Collection<Path> fields=new ArrayList<>(report.getSpecs().getFields());


				// check role compatibility

				for (final Path hidden : new ArrayList<>(lens.getHidden())) {
					if ( !fields.remove(hidden) ) { lens.removeHidden(hidden); }
				}

				for (final Path item : singleton(lens.getItem())) {
					if ( !isItem(item) || !fields.remove(item) ) { lens.setItem(null); }
				}

				for (final Path item : singleton(lens.getValue())) {
					if ( !isValue(item) || !fields.remove(item) ) { lens.setValue(null); }
				}


				// auto assign missing roles

				for (final Path field : new ArrayList<>(fields)) { // before item to give priority over numeric fields
					if ( lens.getValue() == null && isValue(field) && fields.remove(field) ) { lens.setValue(field); }
				}

				for (final Path field : new ArrayList<>(fields)) {
					if ( lens.getItem() == null && isItem(field) && fields.remove(field) ) { lens.setItem(field); }
				}

			}

			// record mapping

			item=series(lens.getItem());
			value=series(lens.getValue());

			// create setup

			setup=object(lens.getSetup());

			setup.set("key", "@");
			setup.set("item", key(item));
			setup.set("value", key(value));

			// create data

			data=array();

			for (final List<Term> tuple : tuples.getEntries()) {

				final JSObject record=object();

				if ( item >= 0 ) { record.set(key(item), label(tuple.get(item))); }
				if ( value >= 0 ) { record.set(key(value), number(tuple.get(value))); }

				data.push(record.set("@", key(tuple)));
			}

		} else {

			setup=null;
			data=null;

			item=-1;
			value=-1;
		}

		return this;
	}

}
