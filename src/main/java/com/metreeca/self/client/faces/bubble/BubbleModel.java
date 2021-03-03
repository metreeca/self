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

package com.metreeca.self.client.faces.bubble;

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


final class BubbleModel extends Model<BubbleModel> {

	public static final int BubbleLimit=100; // the maximum number of handled bubbles // !!! configure from bubble chart tool

	public static final JSObject defs=object()

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
	private int group;

	private int x;
	private int y;
	private int z;

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


	public boolean isItem(final Path field) { return isLabel(field); }

	public boolean isGroup(final Path field) { return isLabel(field); }

	public boolean isValue(final Path field) { return isNumber(field); }


	public Path item() {
		return path(item);
	}

	public Term item(final JSObject cell) {
		return term(cell, item);
	}


	public Path group() {
		return path(group);
	}

	public Term group(final JSObject cell) {
		return term(cell, group);
	}


	public Path x() {
		return path(x);
	}

	public Term x(final JSObject cell) {
		return term(cell, x);
	}


	public Path y() {
		return path(y);
	}

	public Term y(final JSObject cell) {
		return term(cell, y);
	}


	public Path z() {
		return path(z);
	}

	public Term z(final JSObject cell) {
		return term(cell, z);
	}


	@Override public Report.Bubble lens(final boolean writable) { // !!! review
		return (Report.Bubble)super.lens(writable);
	}

	@Override protected BubbleModel update() { // !!! refactor

		final Report report=report();
		final Tuples tuples=tuples();

		options=report == null ? null : object(lens(false).getSetup());

		if ( report != null && tuples != null ) {

			final Report.Bubble lens=lens(false);

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
				if ( !labels.remove(lens.getGroup()) ) { lens.setGroup(null); }

				if ( !numbers.remove(lens.getX()) ) { lens.setX(null); }
				if ( !numbers.remove(lens.getY()) ) { lens.setY(null); }
				if ( !numbers.remove(lens.getZ()) ) { lens.setZ(null); }

				// auto assign missing roles

				if ( lens.getItem() == null && !labels.isEmpty() ) { lens.setItem(labels.remove(0)); }
				if ( lens.getGroup() == null && !labels.isEmpty() ) { lens.setGroup(labels.remove(0)); }

				if ( lens.getX() == null && !numbers.isEmpty() ) { lens.setX(numbers.remove(0)); }
				if ( lens.getY() == null && !numbers.isEmpty() ) { lens.setY(numbers.remove(0)); }
				if ( lens.getZ() == null && !numbers.isEmpty() ) { lens.setZ(numbers.remove(0)); }
			}

			// record mapping

			this.item=series(lens.getItem());
			this.group=series(lens.getGroup());

			this.x=series(lens.getX());
			this.y=series(lens.getY());
			this.z=series(lens.getZ());

			// create setup

			setup=object(lens.getSetup());

			setup.set("key", "@");

			setup.set("item", key(item));
			setup.set("group", key(group));

			setup.set("x", key(x));
			setup.set("y", key(y));
			setup.set("z", key(z));

			setup.set("abscissa.label", label(path(x)));
			setup.set("ordinate.label", label(path(y)));

			// create data

			data=array();

			for (final List<Term> tuple : tuples.getEntries()) {

				final JSObject record=object();

				if ( item >= 0 ) { record.set(key(item), label(tuple.get(item))); }
				if ( group >= 0 ) { record.set(key(group), label(tuple.get(group))); }

				if ( x >= 0 ) { record.set(key(x), number(tuple.get(x))); }
				if ( y >= 0 ) { record.set(key(y), number(tuple.get(y))); }
				if ( z >= 0 ) { record.set(key(z), number(tuple.get(z))); }

				data.push(record.set("@", key(tuple)));
			}

		} else {

			setup=null;
			data=null;

			item=-1;
			group=-1;

			x=-1;
			y=-1;
			z=-1;
		}

		return this;
	}

}
