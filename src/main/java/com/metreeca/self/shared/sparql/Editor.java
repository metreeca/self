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
import com.metreeca.self.shared.beans.Constraint.*;
import com.metreeca.self.shared.beans.Path.Summary;
import com.metreeca.self.shared.beans.Path.Transform;
import com.metreeca.self.shared.beans.schemas.RDF;
import com.metreeca.self.shared.forms.Shape;

import java.util.*;
import java.util.Map.Entry;

import static com.metreeca.self.shared.beans.Specs.Images;
import static com.metreeca.self.shared.beans.Specs.Langs;


public abstract class Editor {

	private static final Flake Classes=new Flake().specs(Specs.Classes());


	private int indent;

	private final StringBuilder text=new StringBuilder(1000);


	private int next;

	private final Map<Flake, Map<Transform, Map<Summary, Integer>>> ids=new HashMap<>();


	//// Probes ////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected int id() {
		return next++;
	}

	protected Integer id(final Flake flake) {
		return id(flake, null);
	}

	protected Integer id(final Flake flake, final Transform transform) {
		return id(flake, transform, null);
	}

	protected Integer id(final Flake flake, final Transform transform, final Summary summary) {

		Map<Transform, Map<Summary, Integer>> transforms=ids.get(flake);

		if ( transforms == null ) {
			ids.put(flake, transforms=new HashMap<>()); // key may be null
		}

		Map<Summary, Integer> summaries=transforms.get(transform);

		if ( summaries == null ) {
			transforms.put(transform, summaries=new HashMap<>()); // key may be null
		}

		Integer id=summaries.get(summary);

		if ( id == null ) {
			summaries.put(summary, id=id());
		}

		return id;
	}

	protected Integer id(final Flake flake, final Transform transform, final Summary summary, final Integer alias) {

		Map<Transform, Map<Summary, Integer>> transforms=ids.get(flake);

		if ( transforms == null ) {
			ids.put(flake, transforms=new HashMap<>()); // !!! can't use EnumMap: key may be null
		}

		Map<Summary, Integer> summaries=transforms.get(transform);

		if ( summaries == null ) {
			transforms.put(transform, summaries=new HashMap<>()); // !!! can't use EnumMap: key may be null
		}

		final int id=(alias != null) ? alias : id();

		summaries.put(summary, id);

		return id;
	}


	//// Wrapper ///////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void info(final CharSequence label, final Shape<?, ?> shape) {

		final String endpoint=shape.getEndpoint();

		text("# ");
		text(label);

		if ( !endpoint.isEmpty() ) {
			text(" @ ");
			text(endpoint);
		}

		text("\f");

		// !!! if ( shape.getLabel() || shape.getNotes() || !shape.getSpecs().getPattern().isEmpty() ) {
		text("prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
		//}

		// !!! if ( ??? ) {
		text("prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n");
		//}

		// !!! if ( shape.getPoint() ) {
		text("prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n");
		//}

		text("\f");
	}


	protected void projection(final Object id,
			final boolean label, final boolean notes, final boolean image, final boolean point) {

		text(" ?(0)", id);

		if ( label ) { text(" ?(0)_label", id); }
		if ( notes ) { text(" ?(0)_notes", id); }
		if ( image ) { text(" ?(0)_image", id); }
		if ( point ) { text(" ?(0)_lat ?(0)_lng", id); }

	}

	protected void criterion(final Object id, final boolean reverse,
			final boolean label, final boolean notes, final boolean image, final boolean point) {

		// notes/image/point don't affect ordering (assumes a single projected value per term)

		// ;(virtuoso) multiple criteria eat up limited sorting keys (20 on dbpedia)
		// ;(virtuoso) coalesce() in sorting criteria usually behaves as if always returning unbound values

		final String order=reverse ? " desc" : " asc";

		if ( label ) { // as first criterion, to (roughly) drive presentation ordering

			// str(label) strips language tags to compare simple and tagged literals (see SPARQL §15.1)
			// ;(virtuoso) ucase before str prevents mis-sorting issues in tuples queries

			text("(0)(str(ucase(?(1)_label)))", order, id);

		}

		text("(0)(?(1))", order, id);

	}


	protected void offset(final int offset) {
		if ( offset > 0 ) {
			text("offset ");
			text(offset);
			text("\n");
		}
	}

	protected void limit(final int limit) {
		if ( limit > 0 ) {
			text("limit ");
			text(limit);
			text("\n");
		}
	}


	protected void var(final Object id) {
		text(" ?");
		text(id);
	}

	protected void exp(final Object exp) {
		if ( exp instanceof Integer ) {
			var(exp);
		} else {
			text(exp);
		}
	}


	//// Matching //////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void core(final Flake flake, final Integer target,
			final int sample, final String pattern) {

		text("\f{ select distinct ");

		final boolean matching=!pattern.isEmpty();

		projections(flake, target);

		text(" where {\f");

		if ( flake.matches(Classes) ) { // !!! special handling of Specs.Classes

			final Integer id=id(flake);

			// ;( no existential filtering on class instances: performance killer for fuseki and large datasets

			text("{\f"
					+"?(0) a <http://www.w3.org/2002/07/owl#Class> filter exists { [] a ?(0) }"
					+"\f} union {\f"
					+"?(0) a <http://www.w3.org/2000/01/rdf-schema#Class> filter exists { [] a ?(0) }"
					+"\f}\f", id);

			text("filter ( ");
			filter(id, new Exclude());
			text(" )\f");

		} else {

			root(flake);
			opts(flake);
			base(flake, sample);
			flake(flake, matching ? null : target);

		}

		pattern(flake, pattern);

		text("\f}");

		group(flake);
		having(flake);

		if ( sample > 0 ) {
			text(" limit ");
			text(sample);
		}

		text(" }\f");
	}

	protected Integer hook(final Flake flake, final Integer target,
			final boolean optional, final Path path,
			final int sample, final String pattern) {

		final List<Term> steps=path.getSteps();
		final Transform transform=path.getTransform();
		final Summary summary=path.getSummary();

		final Path field=new Path(steps) // copy as field (no expansion state) to project values
				.setTransform(transform)
				.setSummary(summary);

		final Path facet=new Path() // mark target path as required to ensure non-null hook values
				.setExisting(optional ? null : true)
				.setTransform(transform)
				.setSummary(summary);

		if ( field.isAggregate() ) {

			final Integer id=id(

					flake

							.outer() // retain only aggregate filters and non-aggregates (required for grouping)
							.path(field) // add target (additional aggregates won't alter grouping)
							.path(facet), // aggregate targets are used only for 'values' and are always optional

					transform,
					summary,
					target);

			// aggregates are assumed to be literals > no annotations
			// assumes unique labels as well, so that grouping is not altered by removing labels

			core(flake, null, sample, pattern);

			return id;

		} else if ( flake.isExternal() ) {

			flake.outer(); // retain only aggregate filters and non-aggregates (required for grouping)

			final Flake hook=flake.hook(field); // look for the target path among the fields
			final Flake root=flake.hook(new Path()); // ook for the root path among the fields

			// !!! look also for untransformed prefix path (may be optional!)

			if ( hook != null ) {

				final Integer id=id(hook.path(facet), transform, summary, target);

				core(flake, id, sample, pattern);

				return id;

			} else if ( root != null ) {

				final Flake tail=new Flake();

				final Integer id=id(tail

								.path(new Path()) // expose root as field
								.path(field)
								.path(facet),

						transform,
						summary,
						target);

				core(flake, null, sample, pattern);

				// join with tail path (use core() to 'select distinct', avoiding count(distinct *) in the wrapper)

				id(tail, null, null, id(root)); // bind tail path to root projection
				core(tail, id, sample, pattern);

				return id;

			} else { // !!! broken counts

				final Integer id=id(

						(optional

								? flake.group() // retain all non-aggregate fields for proper counting
								: flake.inner()) // retain only individual filters

								.path(field)
								.path(facet),

						transform,
						summary,
						target);

				core(flake, id, sample, pattern);

				return id;

			}

		} else {

			final Integer id=id(

					(optional

							? flake.group() // retain all non-aggregate fields for proper counting
							: flake.inner()) // retain only individual filters

							.path(field)
							.path(facet),

					transform,
					summary,
					target);

			core(flake, id, sample, pattern);

			return id;

		}
	}


	private void projections(final Flake flake, final Integer target) {

		for (final Probe probe : flake.probes()) {
			if ( probe.isField() || probe.isFilter() && probe.isAggregate() ) { // aggregate filters must be projected

				final Transform transform=probe.transform();
				final Summary summary=probe.summary();

				if ( summary != null ) { // assumed to be a literal > no annotations

					text(" (");
					summary(id(flake, transform), summary);
					text(" as ");
					var(id(flake, transform, summary));
					text(')');

				} else if ( probe.isLiteral() ) { // assumed to be a literal > no annotations

					var(id(flake, transform));

				} else if ( target == null || Objects.equals(id(flake), target) ) {

					var(id(flake));

				} else {

					var(id(flake, transform));

				}
			}
		}

		for (final Flake slot : flake.slots().values()) { projections(slot, target); }
		for (final Flake role : flake.roles().values()) { projections(role, target); }
	}


	private void group(final Flake flake) {
		if ( flake.isSimple() && flake.isAggregate() ) {
			text(" group by");
			groupings(flake);
		}
	}

	private void groupings(final Flake flake) {

		for (final Probe probe : flake.probes()) {
			if ( probe.isSimple() ) {

				final Transform transform=probe.transform();

				var(transform != null ? id(flake, transform) : id(flake));
			}
		}

		for (final Flake slot : flake.slots().values()) { groupings(slot); }
		for (final Flake role : flake.roles().values()) { groupings(role); }
	}


	private void having(final Flake flake) {
		if ( flake.isExternal() ) {
			text(" having");
			havings(flake);
		}
	}

	private void havings(final Flake flake) {

		for (final Probe probe : flake.probes()) {
			if ( probe.isAggregate() && probe.isFilter() ) {
				for (final Constraint constraint : probe.constraints()) {

					final Summary summary=probe.summary();
					final Transform transform=probe.transform();

					// ;(virtuoso) unable to refer to projected aggregate values in aggregate filters
					// (correctly strictly standard-wise, but commonly extended to work the other way)

					final String exp=summary.name().toLowerCase() // !!! factor with summary()
							+"("+(summary == Summary.Count ? "distinct ?" : "?")+id(flake, transform)+")";

					text(" (");
					filter(exp, constraint);
					text(" )");
				}
			}
		}

		for (final Flake slot : flake.slots().values()) { havings(slot); }
		for (final Flake role : flake.roles().values()) { havings(role); }

	}


	private void root(final Flake flake) {
		if ( flake.isWild() ) {
			text("?(0) ?p ?o .\f", id(flake));
		}
	}

	private void opts(final Flake flake) { // inline values for simple multi-valued options constraints
		for (final Probe probe : flake.probes()) {
			if ( probe.summary() == null && probe.transform() == null ) {
				for (final Constraint constraint : probe.constraints()) {
					if ( constraint instanceof Options ) {

						final Set<Term> values=((Options)constraint).getValues();

						if ( values.size() > 1 ) { // single-valued options inlined in term(Flake)

							text("values ?(0) {\n", id(flake));

							for (final Term term : values) {
								text(term.format());
								text('\n');
							}

							text("}\f");
						}
					}
				}
			}
		}
	}

	private void base(final Flake flake, final int sample) {

		for (final Probe probe : flake.probes()) {
			for (final Constraint constraint : probe.constraints()) {
				if ( constraint instanceof Base ) {

					if ( probe.transform() != null ) {
						throw new UnsupportedOperationException("transformed base constraint");
					}

					if ( probe.summary() != null ) {
						throw new UnsupportedOperationException("aggregate base constraint");
					}

					final Specs specs=((Base)constraint).getSpecs();
					final String pattern=specs.getPattern();
					final Path axis=specs.getAxis();

					if ( axis == null ) {
						throw new UnsupportedOperationException("no axis field on base constraint");
					}

					hook(new Flake().specs(specs), id(flake, probe.transform(), probe.summary()), false,
							axis, sample, pattern
					);

				}
			}
		}

		for (final Flake slot : flake.slots().values()) { base(slot, sample); }
		for (final Flake role : flake.roles().values()) { base(role, sample); }

		text("\f");
	}


	private void flake(final Flake flake, final Integer target) {

		text("\f");

		final List<Entry<Term, Flake>> entries=new ArrayList<>();

		entries.addAll(flake.slots().entrySet());
		entries.addAll(flake.roles().entrySet());

		Collections.sort(entries, new Comparator<Entry<Term, Flake>>() { // place optional flakes after required ones
			@Override public int compare(final Entry<Term, Flake> x, final Entry<Term, Flake> y) {

				final boolean a=x.getValue().isFilter() || x.getValue().isPresent();
				final boolean b=y.getValue().isFilter() || y.getValue().isPresent();

				return a && !b ? -1 : !a && b ? 1 : 0;

			}
		});

		trees(flake, target, entries);
		transforms(flake);
		filters(flake);

		text("\f");

	}

	private void trees(final Flake flake, final Integer target, final Iterable<Entry<Term, Flake>> entries) {

		for (final Entry<Term, Flake> entry : entries) {

			final Term link=entry.getKey();
			final Flake value=entry.getValue();

			if ( value.isPresent() && value.isMissing() ) {

				text("filter ( false )");

			} else if ( value.isMissing() ) {

				text("filter not exists { ");
				edge(flake, link, value);
				text(" }");

			} else if ( value.isPresent() && value.isLeaf() && !value.isField() ) {

				text("filter exists { ");
				edge(flake, link, value);
				text(" }");

			} else if ( value.isPresent() || value.isFilter() ) {

				opts(value);
				tree(flake, target, link, value);

			} else {

				text("optional {\f");
				opts(value);
				tree(flake, target, link, value);
				text("\f}");

			}

			text("\f");
		}
	}

	private void tree(final Flake flake, final Integer target, final Term edge, final Flake value) {

		edge(flake, edge, value);
		text(" .\f");
		flake(value, target);
		text("\f");
	}


	private void edge(final Flake flake, final Term edge, final Flake value) {
		if ( edge.isRecto() ) {

			term(flake);
			edge(edge);
			term(value);

		} else if ( edge.isVerso() ) {

			term(value);
			edge(edge.reverse());
			term(flake);

		}
	}

	private void edge(final Term edge) {
		if ( edge.equals(RDF.RDFType) ) { text(" a"); } else { term(edge); }
	}


	private void term(final Flake flake) {

		final Term term=flake.term();

		if ( term == null ) {

			var(id(flake));

		} else {

			term(term); // replace constant value

		}
	}

	private void term(final Term term) {
		text(' ');
		text(term.format());
	}


	//// Aggregates ////////////////////////////////////////////////////////////////////////////////////////////////////

	private void summary(final Integer id, final Summary summary) {
		text("(0)((1)?(2))",
				summary.name().toLowerCase(),
				summary == Summary.Count ? "distinct " : "",
				id);
	}


	//// Transforms ////////////////////////////////////////////////////////////////////////////////////////////////////

	private void transforms(final Flake flake) {

		for (final Probe probe : flake.probes()) {

			final Transform transform=probe.transform();

			if ( transform != null ) {

				text("bind (");
				transform(id(flake), transform);
				text(" as ");
				var(id(flake, transform));
				text(")\n");

			} else if ( probe.isField() && flake.term() != null ) { // project inlined constant

				text("bind (");
				term(flake.term());
				text(" as ");
				var(id(flake));
				text(")\n");

			}
		}

		text("\f");
	}

	private void transform(final Integer id, final Transform transform) {
		if ( transform == null ) {

			var(id);

		} else if ( transform == Transform.Quarter ) {

			quarter(id);

		} else if ( transform == Transform.Number ) {

			number(id);

		} else {
			function(id, transform);
		}
	}


	private void quarter(final Integer id) {

		// ;(fuseki) ceil returns xsd:decimal: xsd:integer required to cast to expected type
		// ;(virtuoso) integer division yields integer: 3.0 required to force decimal result

		text("xsd:integer(ceil(month(?(0))/3.0))", id);

	}

	private void number(final Integer id) {
		text("xsd:decimal(?(0))", id);
	}

	private void function(final Integer id, final Transform transform) {
		text(transform.name().toLowerCase());
		text('(');
		var(id);
		text(')');
	}


	//// Filters ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void filters(final Flake flake) {

		for (final Probe probe : flake.probes()) {

			final Transform transform=probe.transform();
			final Summary summary=probe.summary();

			if ( summary == null ) { // individual filter
				for (final Constraint constraint : probe.constraints()) {

					final boolean inlined=transform == null && constraint instanceof Options;
					final boolean base=constraint instanceof Base;

					if ( !(inlined || base) ) {
						text(" filter ( ");
						filter(id(flake, transform, null), constraint);
						text(" )\n");
					}
				}
			}
		}

		text("\f");
	}

	private void filter(final Object exp, final Constraint constraint) {
		if ( constraint instanceof Options ) {

			options(exp, (Options)constraint);

		} else if ( constraint instanceof Range ) {

			range(exp, (Range)constraint);

		} else if ( constraint instanceof Exclude ) {

			exclude(exp, (Exclude)constraint);

		} else {

			throw new UnsupportedOperationException("unknown constraint type ["+constraint.getClass().getName()+"]");

		}
	}

	private void options(final Object exp, final Options options) {

		final Set<Term> values=options.getValues();

		if ( values.size() == 1 ) {

			exp(exp);
			text(" = ");
			term(values.iterator().next());

		} else {

			exp(exp);

			text(" in (\n\t");

			for (final Iterator<Term> terms=values.iterator(); terms.hasNext(); ) {
				term(terms.next());
				text(terms.hasNext() ? ",\n" : "\n");
			}

			text("\b)");

		}

	}

	private void range(final Object exp, final Range range) {

		final boolean lower=range.getLower() != null;
		final boolean upper=range.getUpper() != null;

		if ( lower ) {
			exp(exp);
			text(" >= ");
			term(range.getLower());
		}

		if ( lower && upper ) {
			text(" && ");
		}

		if ( upper ) {
			exp(exp);
			text(" <= ");
			term(range.getUpper());
		}

	}

	private void exclude(final Object exp, final Exclude exclude) {

		text("!isBlank((0)) && strbefore(str((0)), '#') not in (\n\t", exp instanceof Integer ? "?"+exp : exp);

		for (final Iterator<String> iterator=exclude.iterator(); iterator.hasNext(); ) {
			text('\'');
			text(iterator.next());
			text(iterator.hasNext() ? "',\n" : "'\n");
		}

		text("\b)");
	}


	private void pattern(final Flake flake, final String pattern) {
		if ( !pattern.isEmpty() ) {

			annotations(flake, null, true, false, false, false);
			targets(flake);

			text("\ffilter (\t");
			regexs(flake, pattern.trim().split("\\W+"), false);
			text("\b)\f");

		}
	}

	private void targets(final Flake flake) { // !!! review interactions with name guessing

		for (final Probe probe : flake.probes()) {
			if ( probe.isField() ) {

				final Transform transform=probe.transform();
				final Summary summary=probe.summary();

				if ( transform == null && summary == null ) {
					// str() strips language tags to compare simple and tagged literals (see SPARQL §15.1)
					text("bind (ucase(coalesce(str(?(0)_label), str(?(0)))) as ?(0)_target)\n", id(flake));
				} else { // aggregates/derivates are assumed to be literals: match only the lexical form
					text("bind (ucase(str(?(0))) as ?(0)_target)\n", id(flake, transform, summary));
				}
			}
		}

		for (final Flake slot : flake.slots().values()) { targets(slot); }
		for (final Flake role : flake.roles().values()) { targets(role); }

	}

	private void regexs(final Flake flake, final String[] keywords, final boolean tail) {

		boolean head=tail;

		for (final Probe probe : flake.probes()) {
			if ( probe.isField() ) {

				final Summary summary=probe.summary();
				final Transform transform=probe.transform();

				final Integer id=id(flake, transform, summary);

				if ( tail ) {
					text("\n|| ");
				}

				for (int i=0, length=keywords.length; i < length; i++) {

					if ( i > 0 ) { text(" && "); }

					text("contains(?(0)_target, '(1)')", id, keywords[i].toUpperCase());
				}

				head=true;
			}
		}

		for (final Flake slot : flake.slots().values()) { regexs(slot, keywords, head); }
		for (final Flake role : flake.roles().values()) { regexs(role, keywords, head); }

	}


	//// Annotations ///////////////////////////////////////////////////////////////////////////////////////////////////

	protected void annotations(final Flake flake, final Integer target,
			final boolean label, final boolean notes, final boolean image, final boolean point) {

		// ;( assumes a single object for annotation property and language

		final Integer id=id(flake);

		for (final Probe probe : flake.probes()) {
			if ( probe.isField() ) {

				// aggregate and transformed values are assumed to be a literal > no annotations

				if ( probe.transform() == null && probe.summary() == null && !probe.isLiteral() ) {

					if ( target == null || Objects.equals(id, target) ) {
						annotations(id, label, notes, image, point);
					}

				}
			}
		}

		for (final Flake slot : flake.slots().values()) { annotations(slot, target, label, notes, image, point); }
		for (final Flake role : flake.roles().values()) { annotations(role, target, label, notes, image, point); }
	}

	protected void annotations(final Object id,
			final boolean label, final boolean notes, final boolean image, final boolean point) {

		final boolean lookup=label || notes || image || point;

		// ;( guard against left joins with undefined left value
		// expected behaviour unclear but plenty of faulty implementations in either case…

		if ( lookup ) {
			text("bind (coalesce(?(0), false) as ?(0)_)\f", id);
		}

		if ( label ) { label(id); }
		if ( notes ) { notes(id); }
		if ( image ) { image(id); }
		if ( point ) { point(id); }

		if ( lookup ) { text("\f"); }

	}


	private void label(final Object id) {
		textual(id, "rdfs:label", "label");
	}

	private void notes(final Object id) {
		textual(id, "rdfs:comment", "notes");
	}

	private void textual(final Object id, final String property, final String suffix) {

		for (int i=0; i < Langs.size(); ++i) {
			text("optional { ?(0)_ (2) ?(0)_(3)_(1) filter (lang(?(0)_(3)_(1)) = '(4)') }\n",
					id, i, property, suffix, Langs.get(i));
		}

		text('\n');

		bindings(id, suffix, Langs.size());

		// ;(virtuoso) selection and binding of actual value must be performed at top level
	}

	private void image(final Object id) {

		for (int i=0; i < Images.size(); i++) {
			text("optional { ?(0)_ <(2)> ?(0)_image_(1) }\n", id, i, Images.get(i).getText());
		}

		text('\n');

		bindings(id, "image", Images.size());

		// ;(virtuoso) selection and binding of actual value must be performed at top level
	}

	private void point(final Object id) {
		text("optional { ?(0)_ geo:lat ?(0)_lat; geo:long ?(0)_lng }\f", id);
	}


	private void bindings(final Object id, final String suffix, final int options) {

		text("bind (coalesce(");

		for (int i=0; i < options; ++i) {

			if ( i > 0 ) { text(", "); }

			text("?(0)_(2)_(1)", id, i, suffix);
		}

		text(") as ?(0)_(1))\n", id, suffix);
	}


	//// Text //////////////////////////////////////////////////////////////////////////////////////////////////////////

	public final String text() {
		try {

			return text.toString();

		} finally {
			indent=0;
			text.setLength(0);
			next=0;
			ids.clear();
		}
	}


	protected void text(final Object object) {
		text(String.valueOf(object));
	}

	protected void text(final String template, final Object... args) {

		String text=template;

		for (int i=0; i < args.length; ++i) {
			text=text.replace("("+i+")", String.valueOf(args[i]));
		}

		text(text);
	}

	protected void text(final CharSequence text) {
		for (int i=0, length=text.length(); i < length; ++i) { text(text.charAt(i)); }
	}

	protected void text(final char c) {

		final int length=text.length();

		final char last=length > 0 ? text.charAt(length-1) : (char)0;
		final char next=length > 1 ? text.charAt(length-2) : (char)0;

		if ( c == '\f' ) {

			if ( last == '{' ) { ++indent; }

			if ( last != '\n' ) {
				text.append("\n\n");
			} else if ( next != '\n' ) {
				text.append('\n');
			}

		} else if ( c == '\n' ) {

			if ( last == '{' ) { ++indent; }

			if ( last != '\n' || next != '\n' ) {
				text.append('\n');
			}

		} else if ( c == '\t' ) {

			++indent;

		} else if ( c == '\b' ) {

			--indent;

		} else if ( c == ' ' ) {

			if ( next == ' ' && last == '('
					|| length > 0 && last != ' ' && last != '\t' && last != '\n' && last != '(' && last != '[' ) {
				text.append(' ');
			}

		} else {

			if ( last == '\n' ) {

				if ( c == '}' ) { --indent; }

				for (int i=0; i < indent; ++i) { text.append("    "); }
			}

			text.append(c);
		}
	}

}
