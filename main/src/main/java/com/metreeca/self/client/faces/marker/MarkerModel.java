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

package com.metreeca.self.client.faces.marker;

import com.metreeca._tile.client.js.JSArray;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.client.faces.Model;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Tuples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.metreeca._tile.client.js.JSArray.array;
import static com.metreeca._tile.client.js.JSObject.object;

import static java.util.Collections.singleton;


final class MarkerModel extends Model<MarkerModel> {

	public static final int MarkerLimit=100; // the maximum number of handled markers // !!! configure from options panel

	public static final JSObject defaults=object()

			.set("port", "auto");


	private JSObject options; // user defined options

	private int item;
	private int group;

	private int point;
	private int value;

	private int[] details;

	private JSObject setup; // tool setup (options+series)
	private JSArray<JSObject> data; // tool data

	private JSObject port=object(); // the current map viewport


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
		return isLabel(path);
	}

	public boolean isGroup(final Path path) {
		return isLabel(path);
	}

	@Override // override to centralize Marker-specific tests
	public boolean isPoint(final Path path) {
		return super.isPoint(path);
	}

	public boolean isValue(final Path path) {
		return isNumber(path);
	}

	public boolean isDetail(final Path path) {
		return isAnything(path);
	}


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


	public Path point() {
		return path(point);
	}

	public Term point(final JSObject cell) {
		return term(cell, point);
	}


	public Path value() {
		return path(value);
	}

	public Term value(final JSObject cell) {
		return term(cell, value);
	}


	public int[] details() {
		return details;
	}


	public JSObject port() {
		return port;
	}

	public MarkerModel port(final JSObject port) {

		if ( port == null ) {
			throw new NullPointerException("null port");
		}

		this.port=port;

		return this;
	}


	@Override public Report.Marker lens(final boolean writable) { // !!! review
		return (Report.Marker)super.lens(writable);
	}

	@Override protected MarkerModel update() { // !!! refactor

		final Report report=report();
		final Tuples tuples=tuples();

		options=report == null ? null : object(lens(false).getSetup());

		if ( report != null && tuples != null ) {

			final Report.Marker lens=lens(false);

			if ( !tuples.getEntries().isEmpty() ) { // only if real stats are available

				final Collection<Path> fields=new ArrayList<>(report.getSpecs().getFields());


				// check role compatibility

				for (final Path hidden : new ArrayList<>(lens.getHidden())) {
					if ( !fields.remove(hidden) ) { lens.removeHidden(hidden); }
				}

				for (final Path item : singleton(lens.getItem())) {
					if ( !isItem(item) || !fields.remove(item) ) { lens.setItem(null); }
				}

				for (final Path group : singleton(lens.getGroup())) {
					if ( !isGroup(group) || !fields.remove(group) ) { lens.setGroup(null); }
				}

				for (final Path point : singleton(lens.getPoint())) {
					if ( !isPoint(point) || !fields.remove(point) ) { lens.setPoint(null); }
				}

				for (final Path value : singleton(lens.getValue())) {
					if ( !isValue(value) || !fields.remove(value) ) { lens.setValue(null); }
				}

				for (final Path detail : lens.getDetails()) {
					if ( !isDetail(detail) || !fields.remove(detail) ) { lens.removeDetail(detail); }
				}


				// auto assign missing roles

				for (final Path field : new ArrayList<>(fields)) { // before point: if georeferenced will be used as point too
					if ( lens.getItem() == null && isItem(field) && fields.remove(field) ) { lens.setItem(field); }
				}

				for (final Path field : new ArrayList<>(fields)) { // before group: georeferenced resources may be labels too
					if ( !isPoint(lens.getItem()) // don't auto assign if item is already georeferenced
							&& lens.getPoint() == null && isPoint(field) && fields.remove(field) ) {
						lens.setPoint(field);
					}
				}

				for (final Path field : new ArrayList<>(fields)) {
					if ( lens.getGroup() == null && isGroup(field) && fields.remove(field) ) { lens.setGroup(field); }
				}

				for (final Path field : new ArrayList<>(fields)) {
					if ( lens.getValue() == null && isValue(field) && fields.remove(field) ) { lens.setValue(field); }
				}

			}

			// record mapping

			this.item=series(lens.getItem());
			this.group=series(lens.getGroup());

			this.point=series(lens.getPoint() != null ? lens.getPoint() : isPoint(lens.getItem()) ? lens.getItem() : null);
			this.value=series(lens.getValue());

			this.details=series(lens.getDetails());

			// setup keys

			final String _key="@";
			final String _item=key(item);
			final String _group=key(group);
			final String _point=key(point);
			final String _lat=(_point != null) ? _point+"_lat" : null;
			final String _lng=(_point != null) ? _point+"_lng" : null;
			final String _value=key(value);

			// create setup

			setup=object(lens.getSetup());

			setup.set("locked", report.isLocked());
			setup.set("port", setup.get("port", (JSObject)null)); // reset viewport, if not set by the user

			setup.set("key", _key);

			setup.set("item", _item);
			setup.set("group", _group);

			setup.set("lat", _lat);
			setup.set("lng", _lng);
			setup.set("value", _value);

			// create data

			data=array();

			for (final List<Term> tuple : tuples.getEntries()) {

				final JSObject record=object();

				if ( item >= 0 ) { record.set(_item, label(tuple.get(item))); }
				if ( group >= 0 ) { record.set(_group, label(tuple.get(group))); }

				if ( point >= 0 ) { record.set(_lat, lat(tuple.get(point))).set(_lng, lng(tuple.get(point))); }
				if ( value >= 0 ) { record.set(_value, number(tuple.get(value))); }

				data.push(record.set(_key, key(tuple)));
			}

		} else {

			setup=null;
			data=null;

			item=-1;
			group=-1;

			point=-1;
			value=-1;

			details=null; // !!! vs []
		}

		return this;
	}

}
