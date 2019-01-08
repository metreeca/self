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

package com.metreeca.self.shared.forms;

import com.metreeca._jeep.shared.async.Promise;
import com.metreeca._tool.shared.forms.Form;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.sparql.Client;
import com.metreeca.self.shared.sparql.Engine;

import static java.util.Arrays.asList;


public abstract class Shape<T extends Shape<T, E>, E> extends Form<T> {

	public static final int Limit=1000; // the default maximum number of items included in shapes
	public static final int Sample=100_000; // the default maximum number of matches to be considered in sampling
	public static final int Timeout=30*1000; // the default query timeout [ms]


	private String endpoint="";
	private Specs specs=new Specs();

	private int offset;
	private int limit=Limit;
	private int sample=Sample;
	private int timeout=Timeout;

	private boolean label; // true if labels are to be retrieved
	private boolean notes; // true if notes are to be retrieved
	private boolean image; // true if images are to be retrieved
	private boolean point; // true if points are to be retrieved


	private E entries;


	public abstract Promise<E> process(final Engine engine, final Client client);


	public String getEndpoint() {

		if ( endpoint == null ) {
			throw new IllegalStateException("undefined endpoint");
		}

		return endpoint;
	}

	public T setEndpoint(final String endpoint) {

		if ( endpoint == null ) {
			throw new NullPointerException("null endpoint");
		}

		this.endpoint=endpoint;

		return self();
	}


	public Specs getSpecs() {

		if ( specs == null ) {
			throw new IllegalStateException("undefined specs");
		}

		return specs;
	}

	public T setSpecs(final Specs specs) {

		if ( specs == null ) {
			throw new NullPointerException("null specs");
		}

		this.specs=specs;

		return self();
	}


	public int getOffset() {
		return offset;
	}

	public T setOffset(final int offset) {

		if ( offset < 0 ) {
			throw new IllegalArgumentException("illegal offset ["+offset+"]");
		}

		this.offset=offset;

		return self();
	}


	public int getLimit() {
		return limit;
	}

	public T setLimit(final int limit) {

		if ( limit < 0 ) {
			throw new IllegalArgumentException("illegal limit ["+limit+"]");
		}

		this.limit=limit;

		return self();
	}


	public int getSample() {
		return sample;
	}

	public T setSample(final int sample) {

		if ( sample < 0 ) {
			throw new IllegalArgumentException("illegal sample ["+sample+"]");
		}

		this.sample=sample;

		return self();
	}


	public int getTimeout() {
		return timeout;
	}

	public T setTimeout(final int timeout) {

		if ( timeout < 0 ) {
			throw new IllegalArgumentException("illegal timeout ["+timeout+"]");
		}

		this.timeout=timeout;

		return self();
	}


	public boolean getLabel() {
		return label;
	}

	public T setLabel(final boolean label) {

		this.label=label;

		return self();
	}


	public boolean getNotes() {
		return notes;
	}

	public T setNotes(final boolean notes) {

		this.notes=notes;

		return self();
	}


	public boolean getImage() {
		return image;
	}

	public T setImage(final boolean image) {

		this.image=image;

		return self();
	}


	public boolean getPoint() {
		return point;
	}

	public T setPoint(final boolean point) {

		this.point=point;

		return self();
	}


	public E getEntries() {

		if ( entries == null ) {
			throw new IllegalStateException("undefined entries");
		}

		return entries;
	}

	public T setEntries(final E entries) {

		if ( entries == null ) {
			throw new NullPointerException("null entries");
		}

		this.entries=entries;

		return self();
	}


	public Object fingerprint() { // Shape is mutable and strict equality is required for data management
		return asList(
				endpoint,
				specs.fingerprint(),
				offset,
				limit,
				sample,
				label,
				notes,
				image,
				point
		);
	}

}
