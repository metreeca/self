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

package com.metreeca.self.shared.sparql;

import com.metreeca.self.shared.beans.*;
import com.metreeca.self.shared.beans.Path.Summary;
import com.metreeca.self.shared.beans.Path.Transform;

import java.util.*;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;


public final class Flake {

	private final Set<Probe> probes=new LinkedHashSet<>();

	private final Map<Term, Flake> slots=new LinkedHashMap<>();
	private final Map<Term, Flake> roles=new LinkedHashMap<>();


	public boolean isField() {

		for (final Probe probe : probes) {
			if ( probe.isField() ) { return true; }
		}

		for (final Flake slot : slots.values()) {
			if ( slot.isField() ) { return true; }
		}

		for (final Flake role : roles.values()) {
			if ( role.isField() ) { return true; }
		}

		return false;
	}

	public boolean isFilter() {

		for (final Probe probe : probes) {
			if ( probe.isFilter() ) { return true; }
		}

		for (final Flake slot : slots.values()) {
			if ( slot.isFilter() ) { return true; }
		}

		for (final Flake role : roles.values()) {
			if ( role.isFilter() ) { return true; }
		}

		return false;
	}

	public boolean isPresent() {

		for (final Probe probe : probes) {
			if ( probe.isPresent() ) { return true; }
		}

		for (final Flake slot : slots.values()) {
			if ( slot.isPresent() ) { return true; }
		}

		for (final Flake role : roles.values()) {
			if ( role.isPresent() ) { return true; }
		}

		return false;
	}

	public boolean isMissing() {

		for (final Probe probe : probes) {
			if ( probe.isMissing() ) { return true; }
		}

		return false;
	}


	public boolean isSimple() { // at least a simple field // !!! review/remove

		for (final Probe probe : probes) {
			if ( probe.isSimple() ) { return true; }
		}

		for (final Flake slot : slots.values()) {
			if ( slot.isSimple() ) { return true; }
		}

		for (final Flake role : roles.values()) {
			if ( role.isSimple() ) { return true; }
		}

		return false;
	}

	public boolean isAggregate() { // at least an aggregate field or filter // !!! review/remove

		for (final Probe probe : probes) {
			if ( probe.isAggregate() ) { return true; }
		}

		for (final Flake slot : slots.values()) {
			if ( slot.isAggregate() ) { return true; }
		}

		for (final Flake role : roles.values()) {
			if ( role.isAggregate() ) { return true; }
		}

		return false;
	}

	public boolean isExternal() { // at least an aggregate filter // !!! review/remove/rename

		for (final Probe probe : probes) {
			if ( probe.isExternal() ) { return true; }
		}

		for (final Flake slot : slots.values()) {
			if ( slot.isExternal() ) { return true; }
		}

		for (final Flake role : roles.values()) {
			if ( role.isExternal() ) { return true; }
		}

		return false;
	}


	public boolean isWild() { // no edge-based or otherwise matching constraint on the root resource

		for (final Probe probe : probes) {
			for (final Constraint constraint : probe.constraints()) {
				if ( constraint instanceof Constraint.Base
						|| constraint instanceof Constraint.Options && probe.isPlain() ) {
					return false;
				}
			}
		}

		return slots.isEmpty() && roles.isEmpty() && term() == null;

	}

	public boolean isLeaf() {
		return slots.isEmpty() && roles.isEmpty();
	}

	private boolean isDead() {
		return probes.isEmpty() && slots.isEmpty() && roles.isEmpty();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @return the constant term assigned to this flake; {@code null} if not a constant
	 */
	public Term term() {

		for (final Probe probe : probes) {

			final Term term=probe.term();

			if ( term != null ) {
				return term;
			}
		}

		return null;
	}


	public Iterable<Probe> probes() {
		return unmodifiableSet(probes);
	}

	public Map<Term, Flake> slots() {
		return unmodifiableMap(slots);
	}

	public Map<Term, Flake> roles() {
		return unmodifiableMap(roles);
	}


	public Flake hook(final Path path) {
		return hook(path.getSteps(), path.getTransform(), path.getSummary());
	}

	private Flake hook(final List<Term> steps, final Transform transform, final Summary summary) {
		if ( steps.isEmpty() ) {

			for (final Probe probe : probes) {
				if ( probe.isField() && probe.transform() == transform && probe.summary() == summary ) { return this; }
			}

			return null;

		} else {

			final Term step=steps.get(0);

			final Map<Term, Flake> links=step.isRecto() ? slots : step.isVerso() ? roles : null;

			if ( links == null ) {
				throw new IllegalArgumentException("illegal step type ["+step+"]");
			}

			final Flake flake=links.get(step);

			return flake == null ? null : flake.hook(steps.subList(1, steps.size()), transform, summary);
		}
	}


	public Probe probe(final Path path) {
		return probe(path.getSteps(), path.getTransform(), path.getSummary());
	}

	private Probe probe(final List<Term> steps, final Transform transform, final Summary summary) {
		if ( steps.isEmpty() ) {

			for (final Probe probe : probes) {
				if ( probe.isField() && probe.transform() == transform && probe.summary() == summary ) { return probe; }
			}

			return null;

		} else {

			final Term step=steps.get(0);

			final Map<Term, Flake> links=step.isRecto() ? slots : step.isVerso() ? roles : null;

			if ( links == null ) {
				throw new IllegalArgumentException("illegal step type ["+step+"]");
			}

			final Flake flake=links.get(step);

			return flake == null ? null : flake.probe(steps.subList(1, steps.size()), transform, summary);
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Flake specs(final Specs specs) {

		if ( specs == null ) {
			throw new NullPointerException("null specs");
		}

		for (final Path path : specs.getPaths()) {
			if ( path.isConstrained() ) { path(path); } // before fields to restrict search
		}

		for (final Path path : specs.getPaths()) {
			if ( path.isField() ) { path(path); }
		}

		return this;
	}

	public Flake path(final Path path) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		Flake flake=this;

		for (final Term step : path.getSteps()) {

			final Map<Term, Flake> links=step.isRecto() ? flake.slots : step.isVerso() ? flake.roles : null;

			if ( links == null ) {
				throw new IllegalArgumentException("illegal step type ["+step+"]");
			}

			flake=links.get(step);

			if ( flake == null ) {
				links.put(step, flake=new Flake());
			}
		}

		for (final Probe probe : flake.probes) {
			if ( probe.path(path) != null ) { return flake; }
		}

		flake.probes.add(new Probe().path(path));

		return flake;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Flake outer() { // retain only aggregate filters and non-aggregate paths (required for grouping)

		for (final Iterator<Flake> slots=this.slots.values().iterator(); slots.hasNext(); ) {
			if ( slots.next().outer().isDead() ) { slots.remove(); }
		}

		for (final Iterator<Flake> roles=this.roles.values().iterator(); roles.hasNext(); ) {
			if ( roles.next().outer().isDead() ) { roles.remove(); }
		}

		for (final Iterator<Probe> probes=this.probes.iterator(); probes.hasNext(); ) {

			final Probe probe=probes.next();

			if ( probe.isAggregate() && !probe.isFilter() ) {
				probes.remove();
			}
		}

		return this;
	}

	public Flake group() { // retain only non-aggregate fields and filters

		for (final Iterator<Flake> slots=this.slots.values().iterator(); slots.hasNext(); ) {
			if ( slots.next().group().isDead() ) { slots.remove(); }
		}

		for (final Iterator<Flake> roles=this.roles.values().iterator(); roles.hasNext(); ) {
			if ( roles.next().group().isDead() ) { roles.remove(); }
		}

		for (final Iterator<Probe> probes=this.probes.iterator(); probes.hasNext(); ) {

			final Probe probe=probes.next();

			if ( probe.isAggregate() ) {
				probes.remove();
			}
		}

		return this;
	}

	public Flake inner() { // retain only non-aggregate filters

		for (final Iterator<Flake> slots=this.slots.values().iterator(); slots.hasNext(); ) {
			if ( slots.next().inner().isDead() ) { slots.remove(); }
		}

		for (final Iterator<Flake> roles=this.roles.values().iterator(); roles.hasNext(); ) {
			if ( roles.next().inner().isDead() ) { roles.remove(); }
		}

		for (final Iterator<Probe> probes=this.probes.iterator(); probes.hasNext(); ) {

			final Probe probe=probes.next();

			if ( probe.isAggregate() || !probe.isFilter() ) {
				probes.remove();
			} else {
				probe.field(false); // clear field status (may be true if the probe is both a field and a filter)
			}
		}

		return this;
	}


	//// !!! special handling of Specs.Classes /////////////////////////////////////////////////////////////////////////

	public boolean matches(final Flake flake) {
		return flake != null
				&& matches(probes, flake.probes)
				&& matches(slots, flake.slots)
				&& matches(roles, flake.roles);
	}

	private boolean matches(final Collection<Probe> x, final Collection<Probe> y) {

		final Iterator<Probe> i=x.iterator();
		final Iterator<Probe> j=y.iterator();

		while ( i.hasNext() && j.hasNext() ) {
			if ( !i.next().matches(j.next()) ) { return false; }
		}

		return i.hasNext() == j.hasNext();
	}

	private boolean matches(final Map<Term, Flake> x, final Map<Term, Flake> y) {

		final Iterator<Map.Entry<Term, Flake>> i=x.entrySet().iterator();
		final Iterator<Map.Entry<Term, Flake>> j=y.entrySet().iterator();

		while ( i.hasNext() && j.hasNext() ) {
			if ( !matches(i.next(), j.next()) ) { return false; }
		}

		return i.hasNext() == j.hasNext();
	}

	private boolean matches(final Map.Entry<Term, Flake> x, final Map.Entry<Term, Flake> y) {
		return x.getKey().equals(y.getKey()) && x.getValue().matches(y.getValue());
	}

}
