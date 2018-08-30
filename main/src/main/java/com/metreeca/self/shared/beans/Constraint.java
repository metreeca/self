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

package com.metreeca.self.shared.beans;

import com.metreeca._bean.shared.Bean;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;


public interface Constraint {

	public boolean isEmpty();

	public boolean isLiteral(); // true if this constraint is implicitly applied to a literal

	public Constraint merge(final Constraint constraint);


	@Bean("specs") public static final class Base implements Constraint {

		private final Specs specs;


		public Base(final Specs specs) {

			if ( specs == null ) {
				throw new NullPointerException("null specs");
			}

			this.specs=specs;
		}


		@Override public boolean isEmpty() {
			return false;
		}

		@Override public boolean isLiteral() {

			final Path axis=specs.getAxis();

			return axis != null && !axis.isPlain();
		}

		@Override public Constraint merge(final Constraint constraint) {
			return null;
		}


		public Specs getSpecs() {
			return specs;
		}


		@Override public boolean equals(final Object object) {
			return this == object || object instanceof Base && specs.equals(((Base)object).specs);
		}

		@Override public int hashCode() {
			return specs.hashCode();
		}

	}

	@Bean("values") public static final class Options implements Constraint {

		private final Set<Term> values;


		public Options() {
			values=new HashSet<>();
		}

		public Options(final Term... values) {
			this(new HashSet<>(asList(values)));
		}

		public Options(final Set<Term> values) {

			if ( values == null ) {
				throw new NullPointerException("null values");
			}

			if ( values.contains(null) ) {
				throw new NullPointerException("null value ["+values+"]");
			}

			this.values=values;
		}


		@Override public boolean isEmpty() {
			return values.isEmpty();
		}

		@Override public boolean isLiteral() {

			for (final Term value : values) {
				if ( !value.isLiteral() ) { return false; }
			}

			return !values.isEmpty();
		}

		@Override public Constraint merge(final Constraint constraint) {
			if ( constraint instanceof Options ) {

				final Options merged=new Options();

				merged.values.addAll(values);
				merged.values.retainAll(((Options)constraint).values);

				return merged;

			} else {
				return null;
			}
		}


		public Set<Term> getValues() {
			return unmodifiableSet(values);
		}


		@Override public boolean equals(final Object object) {
			return this == object || object instanceof Options && values.equals(((Options)object).values);
		}

		@Override public int hashCode() {
			return values.hashCode();
		}

	}

	@Bean({"lower", "upper"}) public static final class Range implements Constraint {

		private final Term lower;
		private final Term upper;


		public Range() {
			this(null, null);
		}

		public Range(final Term lower, final Term upper) {
			this.lower=lower;
			this.upper=upper;
		}


		@Override public boolean isEmpty() {
			return lower == null && upper == null;
		}

		@Override public boolean isLiteral() {
			return !isEmpty();
		}

		@Override public Constraint merge(final Constraint constraint) {
			return constraint instanceof Range ? new Range(
					merge(lower, ((Range)constraint).lower), merge(upper, ((Range)constraint).upper)
			) : null;
		}


		@SuppressWarnings("unchecked") private Term merge(final Term x, final Term y) { // !!! review/refactor
			return x == null ? y : y == null ? x : ((Comparable<Object>)x.getValue()).compareTo(y.getValue()) <= 0 ? x : y;
		}


		public Term getLower() {
			return lower;
		}

		public Term getUpper() {
			return upper;
		}


		@Override public boolean equals(final Object object) {
			return this == object || object instanceof Range
					&& equals(lower, ((Range)object).lower)
					&& equals(upper, ((Range)object).upper);
		}

		@Override public int hashCode() {
			return hashCode(lower)^hashCode(upper);
		}


		private int hashCode(final Object object) {
			return object == null ? 0 : object.hashCode();
		}

		private boolean equals(final Object object, final Object reference) {
			return object == null ? reference == null : object.equals(reference);
		}

	}

	/**
	 * Excludes resources in well-known namespaces.
	 */
	@Bean("namespaces") public static final class Exclude implements Constraint, Iterable<String> {

		private static final Set<String> defaults=unmodifiableSet(new TreeSet<>(asList(
				"http://www.w3.org/1999/02/22-rdf-syntax-ns",
				"http://www.w3.org/2000/01/rdf-schema",
				"http://www.w3.org/2002/07/owl",
				"http://www.w3.org/2001/XMLSchema"
		)));


		private final Set<String> namespaces;


		public Exclude() {
			this.namespaces=emptySet();
		}

		public Exclude(final Set<String> namespaces) {

			if ( namespaces == null ) {
				throw new NullPointerException("null namespaces");
			}

			this.namespaces=new TreeSet<>(namespaces);
		}


		private Exclude(final Collection<String> x, final Collection<String> y) {
			if ( x.isEmpty() && y.isEmpty() ) {

				namespaces=emptySet();

			} else {

				namespaces=new TreeSet<>();

				namespaces.addAll(x.isEmpty() ? defaults : x);
				namespaces.addAll(y.isEmpty() ? defaults : y);

			}
		}


		@Override public boolean isEmpty() {
			return false;
		}

		@Override public boolean isLiteral() {
			return false;
		}

		@Override public Constraint merge(final Constraint constraint) {
			return constraint instanceof Exclude ? new Exclude(namespaces, ((Exclude)constraint).namespaces) : null;
		}


		@Override public Iterator<String> iterator() {
			return (namespaces.isEmpty() ? defaults : namespaces).iterator();
		}


		public Set<String> getNamespaces() {
			return unmodifiableSet(namespaces);
		}


		@Override public boolean equals(final Object object) {
			return this == object || object instanceof Exclude && namespaces.equals(((Exclude)object).namespaces);
		}

		@Override public int hashCode() {
			return namespaces.hashCode();
		}

	}

}
