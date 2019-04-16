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
import com.metreeca.self.shared.beans.Constraint.Options;
import com.metreeca.self.shared.beans.schemas.OWL;
import com.metreeca.self.shared.beans.schemas.RDFS;
import com.metreeca.self.shared.beans.schemas.VoID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.metreeca.self.shared.beans.Constraint.Base;
import static com.metreeca.self.shared.beans.Term.named;
import static com.metreeca.self.shared.beans.schemas.RDF.RDFType;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;


@Bean public final class Specs {

	public static final List<String> Langs=unmodifiableList(asList( // preferred languages
			"", "en"
	));

	public static final List<Term> Images=unmodifiableList(asList( // preferred image properties
			named("http://dbpedia.org/ontology/thumbnail"), // likely the smallest > use as first options
			named("http://schema.org/image"),
			named("http://xmlns.com/foaf/0.1/depiction")
	));


	public static Specs Collections() { // specs for top-level annotated collections
		return new Specs()
				.insertPath(new Path().setCollection(true).setLabel("Collection"))
				.insertPath(new Path().setConstraint(new Base(new Specs()
						.insertPath(new Path(VoID.VoIDRootResource)))));
	}

	public static Specs Classes() { // specs for owl/rdfs classes

		// !!! currently handled ad-hoc in Editor because of the following issues

		// !!! ;(fuseki) VALUES-based multiple options very slow
		// !!! ;(virtuoso) existential filtering returns no matches if images are retrieved

		return new Specs()
				.insertPath(new Path().setCollection(true).setLabel("Collection"))
				.insertPath(new Path(RDFType.reverse()).setExisting(true))
				.insertPath(new Path().setConstraint(new Constraint.Exclude()))
				.insertPath(new Path(RDFType).setConstraint(new Options(OWL.OWLClass, RDFS.RDFSClass)));
	}

	public static Specs Types() { // specs for introspected types
		return new Specs()
				.insertPath(new Path(RDFType).setCollection(true).setLabel("Collection"))
				.insertPath(new Path(RDFType).setConstraint(new Constraint.Exclude()))
				.insertPath(new Path(RDFType).setExisting(true));
	}

	public static Specs Resources() { // specs for all resources
		return new Specs()
				.insertPath(new Path().setLabel("Resource"));
	}


	public static Specs Springboard(final String label) { // specs for a named springboard

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		return new Specs()
				.insertPath(new Path().setLabel(label.isEmpty() ? "Collection" : label));
	}

	public static Specs Collection(final String collection, final String label) { // specs for a named collection

		if ( collection == null ) {
			throw new NullPointerException("null collection");
		}

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		final Term entity=named(collection);

		return new Specs()
				.insertPath(new Path().setLabel(label.isEmpty() ? entity.label() : label))
				.insertPath(new Path(RDFType).setConstraint(new Options(entity)));
	}

	public static Specs Resource(final String resource, final String label) { // specs for a named resource

		if ( resource == null ) {
			throw new NullPointerException("null resource");
		}

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		final Term entity=named(resource);

		return new Specs()
				.insertPath(new Path().setLabel(label.isEmpty() ? entity.label() : label))
				.insertPath(new Path().setConstraint(new Options(entity)));
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private String label="";
	private String pattern=""; // pattern for textual filtering

	private final List<Path> paths=new ArrayList<>();


	public Specs copy() {
		return copy(false);
	}

	public Specs copy(final boolean deep) {

		final Specs specs=new Specs()
				.setLabel(label)
				.setPattern(pattern);

		for (final Path path : paths) {
			specs.paths.add(deep ? path.copy() : path);
		}

		return specs;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isEmpty() { // pristine specs
		return paths.isEmpty() && pattern.isEmpty();
	}

	public boolean isVoid() { // no fields

		for (final Path path : paths) {
			if ( path.isField() ) { return false; }
		}

		return true;
	}

	public boolean isSliced() {

		for (final Path facet : getFacets()) {
			if ( facet.isAggregate() && facet.isFilter() ) { return true; }
		}

		return false;
	}


	private boolean isExternal() { // filtered aggregate

		for (final Path path : paths) {
			if ( path.isAggregate() && path.isConstrained() ) { return true; }
		}

		return false;
	}

	private boolean isFiltered(final List<Term> steps) {

		for (final Path path : paths) {
			if ( path.getSteps().equals(steps) && path.isConstrained() ) { return true; }
		}

		return false;
	}

	public boolean isProjected(final List<Term> steps) {

		if ( steps == null ) {
			throw new NullPointerException("null steps");
		}

		final List<Path> fields=getFields();

		for (final Path field : fields) {
			if ( field.getSteps().equals(steps) ) { return true; }
		}

		return false;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String getLabel() {
		return label;
	}

	public Specs setLabel(final String label) {

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		this.label=label;

		return this;
	}


	public String getPattern() {
		return pattern;
	}

	public Specs setPattern(final String pattern) {

		if ( pattern == null ) {
			throw new NullPointerException("null pattern");
		}

		this.pattern=pattern.trim();

		return this;
	}


	public Path getAxis() { // the first field

		for (final Path path : paths) {
			if ( path.isField() ) { return path; }
		}

		return null;
	}


	public List<Path> getFields() {

		final List<Path> fields=new ArrayList<>();

		for (final Path path : paths) {
			if ( path.isField() ) { fields.add(path); }
		}

		return fields;
	}

	public List<Path> getFacets() {

		final List<Path> facets=new ArrayList<>();

		for (final Path path : paths) {
			if ( path.isFacet() ) { facets.add(path); }
		}

		return facets;
	}


	public Specs insertField(final Path field, final int index) {

		if ( field == null ) {
			throw new NullPointerException("null field");
		}

		paths.remove(field);

		int count=-1;

		for (int i=0, size=paths.size(); i < size; i++) {
			if ( paths.get(i).isField() && ++count == index ) { paths.add(i, field); }
		}

		if ( index < 0 || count < index ) { paths.add(field); }

		return this;
	}

	public Specs insertFacet(final Path facet, final int index) {

		if ( facet == null ) {
			throw new NullPointerException("null facet");
		}

		paths.remove(facet);

		int count=-1;

		for (int i=0, size=paths.size(); i < size; i++) {
			if ( paths.get(i).isFacet() && ++count == index ) { paths.add(i, facet); }
		}

		if ( index < 0 || count < index ) { paths.add(facet); }

		return this;
	}


	public List<Path> getPaths() {
		return unmodifiableList(paths);
	}

	public Specs setPaths(final Iterable<Path> paths) {

		if ( paths == null ) {
			throw new NullPointerException("null paths");
		}

		this.paths.clear();

		for (final Path facet : paths) {

			if ( facet == null ) {
				throw new NullPointerException("null path");
			}

			this.paths.add(facet);
		}

		return this;
	}


	public Specs insertPath(final Path path) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		if ( !paths.contains(path) ) {
			paths.add(path);
		}

		return this;
	}

	public Specs insertPath(final Path path, final Path target) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		paths.remove(path);

		final int index=paths.indexOf(target);

		if ( index < 0 ) {

			paths.add(path);

		} else {

			paths.add(index+1, path);

		}

		return this;
	}

	public Specs removePath(final Path path) {

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		paths.remove(path);

		return this;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Specs center(final Path axis) {

		if ( axis == null ) {
			throw new NullPointerException("null axis");
		}

		final Collection<Path> paths=new ArrayList<>();

		for (final Path path : this.paths) {
			paths.add(path.pivot(axis));
		}

		this.paths.clear();
		this.paths.addAll(paths);

		return setLabel(axis.label());
	}


	public Specs open(final Term term) { // !!! review labelling

		if ( term == null ) {
			throw new NullPointerException("null term");
		}

		final Path path=term.isVerso() // !!! review
				? new Path(RDFType).setConstraint(new Options(term.reverse()))
				: new Path().setConstraint(new Options(term));

		return new Specs()
				.setLabel(term.label())
				.insertPath(path);
	}

	public Specs open(final Term term, final Term link) { // !!! review labelling

		if ( term == null ) {
			throw new NullPointerException("null term");
		}

		if ( link == null ) {
			throw new NullPointerException("null link");
		}

		final String label=link.isRecto() ? term.label()+" › "+link.label()
				: link.isVerso() ? link.label()+" › "+term.label()
				: link.label();

		return new Specs()
				.setLabel(label)
				.insertPath(new Path(link.reverse()).setConstraint(new Options(term)));
	}

	public Specs open(final Path path) { // !!! integrate with navigation // !!! review labelling // !!! refactor

		if ( path == null ) {
			throw new NullPointerException("null path");
		}

		final Path axis=getAxis();

		return path.isConstrained() ? anchor(path)
				: !pattern.isEmpty() || isExternal() || isFiltered(path.getSteps()) || axis != null && axis.isAggregate() ? base(path)
				: axis != null && axis.isDerivate() ? base(path) // !!! optimize
				: pivot(path);
	}


	private Specs anchor(final Path path) {
		return new Specs()
				.setLabel(path.label())
				.insertPath(path.reverse());
	}

	private Specs base(final Path path) {
		return new Specs()
				.setLabel(path.label())
				.insertPath(path.reverse().setConstraint(new Base(copy(true))));
	}

	private Specs pivot(final Path axis) {

		final Specs specs=new Specs().setLabel(axis.label());

		for (final Path path : paths) {
			if ( path.isConstrained() ) { specs.insertPath(path.pivot(axis).setExpanded(null)); } // filter > anchor
		}

		return specs;
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String label() {
		return label.isEmpty() ? getAxis() != null ? getAxis().label() : "Item" : label;
	}


	public Object fingerprint() { // Specs are mutable and strict equality is required for data management
		return asList(
				pattern,
				fingerprint(paths)
		);
	}

	private List<Object> fingerprint(final Iterable<Path> paths) {

		final List<Object> active=new ArrayList<>();

		for (final Path path : paths) {
			if ( path.isField() || path.isConstrained() ) { active.add(path.fingerprint()); }
		}

		return active;
	}

}
