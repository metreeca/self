/*
 * Copyright © 2013-2019 Metreeca srl. All rights reserved.
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

package com.metreeca.self.shared.beans;

import com.metreeca._bean.shared.Bean;

import java.util.*;

import static java.util.Arrays.asList;


@Bean public final class Path {

	@Bean public enum Summary { // !!! l10n

		Count,
		Sum, Avg("Average"), Min("Minimum"), Max("Maximum");


		private final String label;


		Summary() {
			label=name();
		}

		Summary(final String label) {

			if ( label == null ) {
				throw new NullPointerException("null label");
			}

			this.label=label;
		}


		public String label() {
			return label;
		}
	}

	@Bean public enum Transform { // !!! l10n

		Year, Quarter, Month, Day,
		Number, Abs("Absolute"), Round, Ceil("Round Up"), Floor("Round Down");


		private final String label;


		Transform() {
			label=name();
		}

		Transform(final String label) {

			if ( label == null ) {
				throw new NullPointerException("null label");
			}

			this.label=label;
		}


		public String label() {
			return label;
		}

	}


	private String label="";

	private int sort; // sort priority: 0 unsorted / > 0 ascending / < 0 descending

	private boolean collection; // if true, terms associated with this path must be opened as collections

	private Boolean expanded; // expansion flag: null > hidden /  true > expanded / false > collapsed
	private Boolean existing; // existence flag: null > immaterial / true > present / false > missing

	private Summary summary;
	private Transform transform;
	private Constraint constraint;

	private final List<Term> steps=new ArrayList<>();


	public Path() {}

	public Path(final Term... steps) { this(asList(steps)); }

	public Path(final List<Term> steps) { setSteps(steps); }


	public Path copy() {
		return new Path()
				.setLabel(label)
				.setSort(sort)
				.setCollection(collection)
				.setExpanded(expanded)
				.setExisting(existing)
				.setSummary(summary)
				.setTransform(transform)
				.setConstraint(constraint)
				.setSteps(steps);
	}


	public boolean isMeta() {
		return summary == null && transform == null && !steps.isEmpty() && steps.get(steps.size()-1).isMeta();
	}

	public boolean isRoot() {
		return summary == null && transform == null && steps.isEmpty();
	}

	public boolean isPlain() {
		return summary == null && transform == null;
	}

	public boolean isAggregate() {
		return summary != null;
	}

	public boolean isDerivate() {
		return transform != null;
	}

	public boolean isFilter() {
		return constraint != null && !constraint.isEmpty();
	}

	public boolean isConstrained() {
		return isFilter() || existing != null;
	}


	public boolean isField() {
		return expanded == null && !isConstrained();
	}

	public boolean isFacet() {
		return expanded != null;
	}


	public String getLabel() {
		return label;
	}

	public Path setLabel(final String label) {

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		this.label=label;

		return this;
	}


	public int getSort() {
		return sort;
	}

	public Path setSort(final int sort) {

		this.sort=sort;

		return this;
	}


	public boolean isCollection() {
		return collection;
	}

	public Path setCollection(final boolean collection) {

		this.collection=collection;

		return this;
	}


	public Boolean getExpanded() {
		return expanded;
	}

	public Path setExpanded(final Boolean expanded) {

		this.expanded=expanded;

		return this;
	}


	public Boolean getExisting() {
		return existing;
	}

	public Path setExisting(final Boolean existing) {

		this.existing=existing;

		return this;
	}


	public Summary getSummary() {
		return summary;
	}

	public Path setSummary(final Summary summary) {

		this.summary=summary;

		return this;
	}


	public Transform getTransform() {
		return transform;
	}

	public Path setTransform(final Transform transform) {

		this.transform=transform;

		return this;
	}


	public Constraint getConstraint() {
		return constraint;
	}

	public Path setConstraint(final Constraint constraint) {

		this.constraint=constraint;

		return this;
	}


	public List<Term> getSteps() {
		return Collections.unmodifiableList(steps);
	}

	public Path setSteps(final Collection<Term> steps) {

		if ( steps == null ) {
			throw new NullPointerException("null steps");
		}

		if ( steps.contains(null) ) {
			throw new NullPointerException("null step ["+steps+"]");
		}

		this.steps.clear();
		this.steps.addAll(steps);

		return this;
	}


	public Path reverse() {

		final List<Term> steps=new ArrayList<>();

		for (final Term step : this.steps) {
			steps.add(0, step.reverse());
		}

		return new Path(steps)
				.setExisting(existing)
				.setTransform(transform)
				.setConstraint(constraint);
	}

	public Path pivot(final Path axis) {

		if ( axis == null ) {
			throw new NullPointerException("null axis");
		}

		int prefix=0; // shared prefix length

		final int here=steps.size();
		final int there=axis.steps.size();

		while ( prefix < here && prefix < there && steps.get(prefix).equals(axis.steps.get(prefix)) ) {
			++prefix;
		}

		final List<Term> steps=new ArrayList<>();

		for (int i=there-1; i >= prefix; --i) { // inverted tail of axis
			steps.add(axis.steps.get(i).reverse());
		}

		for (int i=prefix; i < here; ++i) { // tail of this
			steps.add(this.steps.get(i));
		}

		return new Path(steps)
				.setLabel(steps.isEmpty() ? axis.label() : label)
				.setSort(sort)
				.setExpanded(expanded)
				.setExisting(existing)
				.setSummary(summary)
				.setTransform(transform)
				.setConstraint(constraint);
	}


	public String label() {
		if ( label.isEmpty() ) {

			final StringBuilder label=new StringBuilder();

			if ( summary != null ) {
				label.append(summary.label().toLowerCase()).append(" of "); // !!! l10n
			}

			if ( transform != null ) {
				label.append(transform.label().toLowerCase()).append(" of "); // !!! l10n
			}

			if ( steps.isEmpty() ) {

				label.append("@"); // !!! review (even if unexpected…)

			} else {

				final Term tail=steps.get(steps.size()-1);

				label.append(tail.isVerso() ? "‹ "+tail.label() : tail.label());
			}

			return label.toString();

		} else {
			return label;
		}
	}

	public String format() {

		final StringBuilder builder=new StringBuilder();

		for (final Term step : steps) {

			if ( builder.length() > 0 ) {
				builder.append('/');
			}

			builder.append(step.format());
		}

		return builder.toString();
	}

	@Override public String toString() {
		return steps.isEmpty() ? "@" : format();
	}


	public Object fingerprint() { // Path is mutable and strict equality is required for data management
		return asList(
				sort,
				existing,
				summary,
				transform,
				constraint,
				steps
		);
	}

}
