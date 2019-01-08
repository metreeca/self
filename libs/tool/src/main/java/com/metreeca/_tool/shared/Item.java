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

package com.metreeca._tool.shared;


import com.metreeca._bean.shared.Bean;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;


public abstract class Item<T extends Item<T>> {

	private static final Random random=new Random();


	public static int Build(final String build) { // semantic version strings like major.minor.patch.tag+/-YYYYMMDD
		try {
			return build == null || build.isEmpty() ? 0 : abs(Integer.parseInt(build.substring(max(0, build.length()-9))));
		} catch ( final NumberFormatException e ) {
			throw new IllegalArgumentException("illegal build number ["+build+"]", e);
		}
	}


	public static String UUID() {

		final char[] id="xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".toCharArray();

		for (int i=0; i < id.length; i++) {
			switch ( id[i] ) {

				case 'x':

					id[i]=Character.forDigit(random.nextInt(16), 16);
					break;

				case 'y':

					id[i]=Character.forDigit(8+random.nextInt(4), 16);
					break;

				default:

					break;
			}
		}

		return new String(id);
	}

	public static boolean UUID(final String uuid) {
		return uuid != null && uuid.matches("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
	}


	private final String uuid;

	private long created; // creation timestamp
	private long updated; // structural change timestamp (>= created)
	private long arrayed; // visualization change timestamp (>= updated)

	private boolean locked;

	private String label="";
	private String notes="";

	private Lens<T> lens=new Auto<>();

	private final Map<Class<?>, Lens<T>> lenses=new LinkedHashMap<>();


	protected Item() {
		this(UUID());
	}

	protected Item(final String uuid) {

		if ( uuid == null ) {
			throw new NullPointerException("null uuid");
		}

		if ( !UUID(uuid) ) {
			throw new IllegalArgumentException("illegal uuid ["+uuid+"]");
		}

		this.uuid=uuid.toLowerCase();
	}


	protected abstract T self();


	public boolean isAnonymous() {
		return label.isEmpty();
	}


	public String getType() { // !!! review/remove
		return self().getClass().getName();
	}

	public String getUUID() {
		return uuid;
	}

	public String getState() {
		return ""; // return a human-readable description of the state of the document (e.g. to build page titles)
	}


	public long getCreated() {
		return created;
	}

	public T setCreated(final long created) {

		if ( created < 0 ) {
			throw new IllegalArgumentException("illegal create timestamp ["+created+"]");
		}

		this.created=created;

		return self();
	}


	public long getUpdated() {
		return updated;
	}

	public T setUpdated(final long updated) {

		if ( updated < 0 ) {
			throw new IllegalArgumentException("illegal update timestamp ["+updated+"]");
		}

		this.updated=updated;

		return self();
	}


	public long getArrayed() {
		return arrayed;
	}

	public T setArrayed(final long arrayed) {

		if ( arrayed < 0 ) {
			throw new IllegalArgumentException("illegal array timestamp["+arrayed+"]");
		}

		this.arrayed=arrayed;

		return self();
	}


	public boolean isLocked() {
		return locked;
	}

	public T setLocked(final boolean locked) {

		this.locked=locked;

		return self();
	}


	public String getLabel() {
		return label;
	}

	public T setLabel(final String label) {

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		this.label=label;

		return self();
	}


	public String getNotes() {
		return notes;
	}

	public T setNotes(final String notes) {

		if ( notes == null ) {
			throw new NullPointerException("null notes");
		}

		this.notes=notes;

		return self();
	}


	public Lens<T> getLens() {
		return lens;
	}

	public T setLens(final Lens<T> lens) {

		if ( lens == null ) {
			throw new NullPointerException("null lens");
		}

		final Class<?> type=lens.getClass();

		lenses.remove(type);
		lenses.put(type, this.lens=lens); // reinsert as last element

		return self();
	}


	public Collection<Lens<T>> getLenses() {
		return Collections.unmodifiableCollection(lenses.values());
	}

	public T setLenses(final Collection<Lens<T>> lenses) {

		if ( lenses == null ) {
			throw new NullPointerException("null lenses");
		}

		this.lenses.clear();

		for (final Lens<T> lens : lenses) {

			if ( lens == null ) {
				throw new NullPointerException("null lens ["+lenses+"]");
			}

			this.lenses.put(lens.getClass(), this.lens=lens);
		}

		this.lenses.put(lens.getClass(), lens); // make sure the current lens is included

		return self();
	}


	//// State /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Lens<T> lens() {
		return lens;
	}

	public T lens(final Lens<T> lens) {

		if ( lens == null ) {
			throw new NullPointerException("null lens");
		}

		final Lens<T> current=lenses.get(lens.getClass());

		return setLens(current == null ? lens : current);
	}


	@SuppressWarnings("unchecked") public <M> M memo() {
		return (M)lens.memo();
	}

	public <M> T memo(final M memo) {

		lens.memo(memo);

		return self();
	}


	//// Timestamps ////////////////////////////////////////////////////////////////////////////////////////////////////

	public T created() {

		if ( locked ) {
			throw new ItemException();
		}

		created=updated=arrayed=currentTimeMillis();

		for (final Lens<T> lens : lenses.values()) { lens.memo(null); } // clear memos on structural change

		return self();
	}

	public T updated() {

		if ( locked ) {
			throw new ItemException();
		}

		updated=arrayed=max(currentTimeMillis(), updated+1);

		for (final Lens<T> lens : lenses.values()) { lens.memo(null); } // clear memos on structural change

		return self();
	}

	public T arrayed() {

		if ( locked ) {
			throw new ItemException();
		}

		arrayed=max(currentTimeMillis(), arrayed+1);

		return self();
	}


	//// Lenses ////////////////////////////////////////////////////////////////////////////////////////////////////////

	public abstract static class Lens<T extends Item<T>> {

		private Object memo; // lens state memo (to be stored in browser history)


		@SuppressWarnings("unchecked")
		public <M> M memo() {
			return (M)memo;
		}

		public <M> Lens<T> memo(final M memo) {

			this.memo=memo;

			return this;
		}

	}


	@Bean public static final class Auto<T extends Item<T>> extends Lens<T> {}

}
