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

package com.metreeca.self.shared.sparql;

import com.metreeca.self.shared.beans.*;

import java.util.*;

import static com.metreeca.self.shared.beans.Path.Summary;
import static com.metreeca.self.shared.beans.Path.Transform;

import static java.util.Collections.unmodifiableCollection;


public final class Probe {

	private boolean initialized; // !!! remove

	private boolean field; // at least a field

	private boolean filter; // at least a non-empty constraint
	private boolean present; // required
	private boolean missing; // forbidden

	private boolean aggregate; // aggregate summary // !!! review/merge
	private boolean external; // aggregate facet // !!! review/merge

	private Transform transform;
	private Summary summary;

	private final Collection<Constraint> constraints=new ArrayList<>();


	public boolean isField() {
		return field;
	}

	public boolean isFilter() {
		return filter;
	}

	public boolean isPresent() {
		return present;
	}

	public boolean isMissing() {
		return missing;
	}


	public boolean isPlain() { // true if the probe is neither transformed nor aggregate
		return summary() == null && transform() == null;
	}

	public boolean isLiteral() { // true if the result of this probe can be assumed to be a literal

		for (final Constraint constraint : constraints) {
			if ( constraint.isLiteral() ) { return true; }
		}

		return transform != null || summary != null;
	}


	public boolean isSimple() {
		return field && !aggregate;
	}

	public boolean isAggregate() {
		return aggregate;
	}

	public boolean isExternal() {
		return external;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the constant term assigned to this probe; {@code null} if not a constant
	 */
	public Term term() {

		for (final Constraint constraint : constraints) {
			if ( transform == null && summary == null && constraint instanceof Constraint.Options ) {

				final Set<Term> values=((Constraint.Options)constraint).getValues();

				if ( values.size() == 1 ) {
					return values.iterator().next();
				}
			}
		}

		return null;
	}


	public Summary summary() {
		return summary;
	}

	public Transform transform() {
		return transform;
	}

	public Collection<Constraint> constraints() {
		return unmodifiableCollection(constraints);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Probe path(final Path path) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		final Summary summary=path.getSummary();
		final Transform transform=path.getTransform();
		final Constraint constraint=path.getConstraint();

		final Boolean required=path.getExisting();

		if ( !initialized || transform == this.transform && summary == this.summary ) {

			this.initialized=true;

			this.field|=path.isField();
			this.filter|=path.isConstrained();

			this.present|=required == Boolean.TRUE;
			this.missing|=required == Boolean.FALSE;

			this.aggregate|=summary != null;
			this.external|=path.isFilter() && path.isAggregate(); // don't report as external required paths

			this.transform=transform;
			this.summary=summary;

			if ( constraint != null && !constraint.isEmpty() ) {

				Constraint merged=null;

				for (final Iterator<Constraint> iterator=constraints.iterator(); merged == null && iterator.hasNext(); ) {
					if ( (merged=iterator.next().merge(constraint)) != null ) { iterator.remove(); }
				}

				constraints.add(merged != null ? merged : constraint);
			}

			return this;

		} else {
			return null;
		}
	}

	public Probe field(final boolean field) {

		this.field=field;

		return this;
	}


	//// !!! special handling of Specs.Classes /////////////////////////////////////////////////////////////////////////

	public boolean matches(final Probe probe) {
		return probe != null
				&& summary == probe.summary
				&& transform == probe.transform
				&& constraints.equals(probe.constraints);
	}

}
