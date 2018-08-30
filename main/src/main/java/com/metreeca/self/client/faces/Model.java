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

package com.metreeca.self.client.faces;

import com.metreeca._tile.client.js.JSArray;
import com.metreeca._tile.client.js.JSObject;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Tuples;
import com.metreeca.self.shared.sparql.Stats;

import java.util.List;

import static com.metreeca._tool.shared.Item.Lens;

import static java.lang.Float.NaN;


public abstract class Model<T extends Model<T>> {

	private Report report;
	private Tuples tuples;

	private List<Path> fields;
	private List<Stats> stats;

	private List<List<Term>> terms;


	public Report report() {
		return report;
	}

	public T report(final Report report) {

		this.report=report;
		this.tuples=null;

		this.fields=(report != null) ? report.getSpecs().getFields() : null;
		this.stats=null;
		this.terms=null;

		return update();
	}


	public Tuples tuples() {
		return tuples;
	}

	public T tuples(final Tuples tuples) {

		this.tuples=tuples;

		this.stats=(tuples != null) ? tuples.getStats() : null;
		this.terms=(tuples != null) ? tuples.getEntries() : null;

		return update();
	}


	public Specs specs() {
		return report != null ? report.getSpecs() : null;
	}

	public Lens<Report> lens(final boolean writable) {
		return report != null ? (writable ? report.arrayed() : report).getLens() : null;
	}


	public int series(final Path path) {
		return fields != null ? fields.indexOf(path) : -1;
	}

	public Path path(final int series) {
		return fields != null && series >= 0 ? fields.get(series) : null;
	}


	public int[] series(final List<Path> paths) {

		final int[] series=new int[paths.size()];

		for (int i=0; i < paths.size(); i++) {
			series[i]=series(paths.get(i));
		}

		return series;
	}

	public Term term(final JSObject cell, final int series) {

		if ( cell == null ) {
			throw new NullPointerException("null cell");
		}

		return term(cell.get("i", -1), series);
	}

	public Term term(final int index, final int series) {
		return terms != null && index >= 0 && series >= 0 ? terms.get(index).get(series) : null;
	}


	public boolean isLocked() {
		return report != null && report.isLocked();
	}


	public boolean isAnything(final Path path) {
		return path != null && !path.isMeta();
	}

	public boolean isLabel(final Path path) {
		return path != null && !path.isMeta() && stats(path).isOther();
	}

	public boolean isNumber(final Path path) {
		return path != null && stats(path).isNumeric();
	}

	public boolean isPoint(final Path path) {
		return path != null && stats(path).getPoints() > 0; // !!! review (minimum ratio?)
	}


	private Stats stats(final Path path) {

		final int index=(path != null && fields != null && stats != null) ? fields.indexOf(path) : -1;

		return index >= 0 ? stats.get(index) : Stats.Nil;
	}


	protected String label(final Path path) {
		return path == null ? "" : path.label();
	}

	protected String label(final Term term) {
		return term == null ? "" : term.label();
	}

	protected float lat(final Term term) {
		return term == null ? NaN : term.getLat();
	}

	protected float lng(final Term term) {
		return term == null ? NaN : term.getLng();
	}

	protected float number(final Term term) {
		return term == null ? NaN : term.getValue() instanceof Number ? ((Number)term.getValue()).floatValue() : NaN;
	}


	protected String key(final int series) {
		return series >= 0 ? String.valueOf(series) : null;
	}

	protected JSArray<String> key(final int[] series) {

		final JSArray<String> key=JSArray.array();

		if ( series != null ) {
			for (final int element : series) { key.push(key(element)); }
		}

		return key;
	}

	protected String key(final Iterable<Term> tuple) {

		final StringBuilder key=new StringBuilder();

		for (final Term term : tuple) {
			key.append(term != null ? term.format() : "").append('\0');
		}

		return key.toString(); // !!! hash (?)
	}


	protected abstract T update();

}
