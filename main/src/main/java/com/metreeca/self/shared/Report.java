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

package com.metreeca.self.shared;

import com.metreeca._bean.shared.Bean;
import com.metreeca._tool.shared.Item;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;

import java.util.*;


@Bean("UUID") public final class Report extends Item<Report> {

	// version info derived from Maven metadata in GWT module

	public static final int Major=version(System.getProperty("version.major"));
	public static final int Minor=version(System.getProperty("version.minor"));
	public static final int Patch=version(System.getProperty("version.patch"));
	public static final int Build=version(System.getProperty("version.build"));


	private static int version(final String property) {
		try {
			return Integer.parseInt(property);
		} catch ( final NumberFormatException ignored ) {
			return 0; // ;( maven properties unavailable when running in SDM
		}
	}


	private String endpoint="";
	private Specs specs=new Specs();


	public Report() {}

	public Report(final String uuid) { super(uuid); }


	@Override protected Report self() {
		return this;
	}


	@Override public String getState() {
		return specs.label();
	}


	//// Content ///////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getEndpoint() {
		return endpoint;
	}

	public Report setEndpoint(final String endpoint) {

		if ( endpoint == null ) {
			throw new NullPointerException("null endpoint");
		}

		this.endpoint=endpoint;

		return this;
	}


	public Specs getSpecs() {
		return specs;
	}

	public Report setSpecs(final Specs specs) {

		if ( specs == null ) {
			throw new NullPointerException("null specs");
		}

		this.specs=specs;

		return this;
	}


	//// Lenses ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public abstract static class Lens<T extends Lens<T>> extends Item.Lens<Report> {

		private final List<Path> details=new ArrayList<>(); // paths included only for user reporting
		private final Set<Path> hidden=new HashSet<>(); // paths excluded from automatic role assignment


		protected abstract T self();


		public List<Path> getDetails() {
			return Collections.unmodifiableList(details);
		}

		public T setDetails(final Collection<Path> details) {

			if ( details == null ) {
				throw new NullPointerException("null details");
			}

			if ( details.contains(null) ) {
				throw new NullPointerException("null detail ["+details+"]");
			}

			this.details.clear();

			for (final Path path : details) {
				this.details.add(free(path));
			}

			return self();
		}

		public T insertDetail(final Path detail) {

			if ( detail == null ) {
				throw new NullPointerException("null detail");
			}

			details.add(free(detail));

			return self();
		}

		public T insertDetail(final Path detail, final int index) {

			if ( detail == null ) {
				throw new NullPointerException("null detail");
			}

			details.add(index, free(detail));

			return self();
		}

		public T removeDetail(final Path detail) {

			if ( detail == null ) {
				throw new NullPointerException("null detail");
			}

			details.remove(detail);

			return self();
		}


		public Set<Path> getHidden() {
			return Collections.unmodifiableSet(hidden);
		}

		public T setHidden(final Collection<Path> hidden) {

			if ( hidden == null ) {
				throw new NullPointerException("null hidden");
			}

			if ( hidden.contains(null) ) {
				throw new NullPointerException("null hidden ["+hidden+"]");
			}

			this.hidden.clear();

			for (final Path path : hidden) {
				this.hidden.add(free(path));
			}

			return self();
		}

		public T insertHidden(final Path hidden) {

			if ( hidden == null ) {
				throw new NullPointerException("null hidden");
			}

			this.hidden.add(free(hidden));

			return self();
		}

		public T removeHidden(final Path hidden) {

			if ( hidden == null ) {
				throw new NullPointerException("null hidden");
			}

			this.hidden.remove(hidden);

			return self();
		}


		protected Path free(final Path path) {

			details.remove(path);
			hidden.remove(path);

			return path;
		}
	}

	public abstract static class Info<T extends Info<T>> extends Lens<T> {

		private String setup="";

		private boolean series; // series panel shown?
		private boolean options; // options panel shown?


		public String getSetup() {
			return setup.isEmpty() ? "{}" : setup;
		}

		public T setSetup(final String setup) {

			if ( setup == null ) {
				throw new NullPointerException("null setup");
			}

			this.setup=setup;

			return self();
		}


		public boolean getSeries() {
			return series;
		}

		public T setSeries(final boolean series) {

			this.series=series;

			return self();
		}


		public boolean getOptions() {
			return options;
		}

		public T setOptions(final boolean options) {

			this.options=options;

			return self();
		}
	}


	@Bean public static final class Table extends Lens<Table> {

		@Override protected Table self() { return this; }

	}

	@Bean public static final class Board extends Lens<Board> {

		@Override protected Board self() { return this; }

	}

	@Bean public static final class Book extends Lens<Book> {

		@Override protected Book self() { return this; }

	}


	@Bean public static final class Pie extends Info<Pie> {

		private Path item;
		private Path value;


		@Override protected Pie self() {
			return this;
		}


		public Path getItem() {
			return item;
		}

		public Pie setItem(final Path item) {

			this.item=free(item);

			return this;
		}


		public Path getValue() {
			return value;
		}

		public Pie setValue(final Path value) {

			this.value=free(value);

			return this;
		}


		@Override protected Path free(final Path path) {

			if ( path != null ) {

				if ( path.equals(item) ) { item=null; }
				if ( path.equals(value) ) { value=null; }

			}

			return super.free(path);
		}
	}

	@Bean public static final class Bar extends Info<Bar> {

		private Path item;

		private final List<Path> values=new ArrayList<>();


		@Override protected Bar self() {
			return this;
		}


		public Path getItem() {
			return item;
		}

		public Bar setItem(final Path item) {

			this.item=free(item);

			return this;
		}


		public List<Path> getValues() {
			return Collections.unmodifiableList(values);
		}

		public Bar setValues(final Collection<Path> values) {

			if ( values == null ) {
				throw new NullPointerException("null values");
			}

			if ( values.contains(null) ) {
				throw new NullPointerException("null values ["+values+"]");
			}

			this.values.clear();

			for (final Path value : values) {
				this.values.add(free(value));
			}

			return this;
		}

		public Bar insertValue(final Path value) {

			if ( value == null ) {
				throw new NullPointerException("null value");
			}

			values.add(free(value));

			return self();
		}

		public Bar insertValue(final Path value, final int index) {

			if ( value == null ) {
				throw new NullPointerException("null value");
			}

			values.add(index, free(value));

			return self();
		}

		public Bar removeValue(final Path value) {

			if ( value == null ) {
				throw new NullPointerException("null value");
			}

			values.remove(value);

			return self();
		}


		@Override protected Path free(final Path path) {

			if ( path != null ) {

				if ( path.equals(item) ) { item=null; }

				values.remove(path);
			}

			return super.free(path);
		}

	}

	@Bean public static final class Line extends Info<Line> {

		private Path item;

		private Path x;

		private final List<Path> ys=new ArrayList<>();


		@Override protected Line self() {
			return this;
		}


		public Path getItem() {
			return item;
		}

		public Line setItem(final Path item) {

			this.item=free(item);

			return this;
		}


		public Path getX() {
			return x;
		}

		public Line setX(final Path x) {

			this.x=free(x);

			return this;
		}


		public List<Path> getYs() {
			return Collections.unmodifiableList(ys);
		}

		public Line setYs(final Collection<Path> ys) {

			if ( ys == null ) {
				throw new NullPointerException("null values");
			}

			if ( ys.contains(null) ) {
				throw new NullPointerException("null values ["+ys+"]");
			}

			this.ys.clear();

			for (final Path value : ys) {
				this.ys.add(free(value));
			}

			return this;
		}

		public Line insertY(final Path y) {

			if ( y == null ) {
				throw new NullPointerException("null value");
			}

			ys.add(free(y));

			return self();
		}

		public Line insertY(final Path y, final int index) {

			if ( y == null ) {
				throw new NullPointerException("null y");
			}

			ys.add(index, free(y));

			return self();
		}

		public Line removeY(final Path y) {

			if ( y == null ) {
				throw new NullPointerException("null y");
			}

			ys.remove(y);

			return self();
		}


		@Override protected Path free(final Path path) {

			if ( path != null ) {

				if ( path.equals(item) ) { item=null; }

				if ( path.equals(x) ) { x=null; }

				ys.remove(path);
			}

			return super.free(path);
		}

	}

	@Bean public static final class Bubble extends Info<Bubble> {

		private Path item;
		private Path group;

		private Path x;
		private Path y;
		private Path z;


		@Override protected Bubble self() {
			return this;
		}


		public Path getItem() {
			return item;
		}

		public Bubble setItem(final Path item) {

			this.item=free(item);

			return this;
		}


		public Path getGroup() {
			return group;
		}

		public Bubble setGroup(final Path group) {

			this.group=free(group);

			return this;
		}


		public Path getX() {
			return x;
		}

		public Bubble setX(final Path x) {

			this.x=free(x);

			return this;
		}


		public Path getY() {
			return y;
		}


		public Bubble setY(final Path y) {

			this.y=free(y);

			return this;
		}


		public Path getZ() {
			return z;
		}

		public Bubble setZ(final Path z) {

			this.z=free(z);

			return this;
		}


		@Override protected Path free(final Path path) {

			if ( path != null ) {

				if ( path.equals(item) ) { item=null; }
				if ( path.equals(group) ) { group=null; }

				if ( path.equals(x) ) { x=null; }
				if ( path.equals(y) ) { y=null; }
				if ( path.equals(z) ) { z=null; }

			}

			return super.free(path);
		}
	}


	@Bean public static final class Marker extends Info<Marker> {

		private Path item;
		private Path group;

		private Path point;
		private Path value;


		@Override protected Marker self() {
			return this;
		}


		public Path getItem() {
			return item;
		}

		public Marker setItem(final Path item) {

			this.item=free(item);

			return this;
		}


		public Path getGroup() {
			return group;
		}

		public Marker setGroup(final Path group) {

			this.group=free(group);

			return this;
		}

		public Path getPoint() {
			return point;
		}

		public Marker setPoint(final Path point) {

			this.point=free(point);

			return this;
		}


		public Path getValue() {
			return value;
		}

		public Marker setValue(final Path value) {

			this.value=free(value);

			return this;
		}


		@Override protected Path free(final Path path) {

			if ( path != null ) {
				if ( path.equals(item) ) { item=null; }
				if ( path.equals(group) ) { group=null; }
				if ( path.equals(point) ) { point=null; }
				if ( path.equals(value) ) { value=null; }
			}

			return super.free(path);
		}
	}


	//// Versioning ////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void encode(final Bean.Info info) {
		info.version(Build);
	}

	public static void decode(final Bean.Info info) {

		if ( info.version() < 20140724 ) { _20140724(info); } // 0.29
		if ( info.version() < 20170530 ) { _20170530(info); } // 0.42
		if ( info.version() < 20170826 ) { _20170826(info); } // 0.43

		info.version(Build);
	}

	private static void _20140724(final Bean.Info info) {
		for (final Bean.Info i : info) {
			if ( i.type().equals("net.metreeca.rover.shared.beans.Node") ) { // renamed Node to Term
				i.type("net.metreeca.rover.shared.beans.Term");
			}
		}
	}

	private static void _20170530(final Bean.Info info) {
		for (final Bean.Info i : info) {
			i.type(i.type().replace("net.metreeca.rover.", "net.metreeca.self.")); // migrated to net.metreeca.self pkg
		}
	}

	private static void _20170826(final Bean.Info info) {
		for (final Bean.Info i : info) {
			i.type(i.type().replace("net.metreeca.", "com.metreeca.")); // migrated to com.metreeca.self pkg
		}
	}

}
