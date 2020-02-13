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

package com.metreeca.self.client.faces.book;

import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;

import java.util.*;

import static com.metreeca.self.shared.beans.schemas.XSD.XSDString;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;


final class BookData {

	private final Map<Term, Term> links=new LinkedHashMap<>();
	private final Map<Term, List<Term>> edges=new LinkedHashMap<>();


	BookData(final Map<Term, List<Term>> entries) {

		if ( entries == null ) {
			throw new NullPointerException("null entries");
		}

		for (final Map.Entry<Term, List<Term>> entry : entries.entrySet()) { // clone entries to allow progressive removal

			final Term link=entry.getKey();
			final List<Term> terms=entry.getValue();

			links.put(link, link); // create auto-associative map to enable retrieval of labelled links
			edges.put(link, new ArrayList<>(terms)); // clone terms to allow progressive removal

		}
	}


	//// Specs /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public BookData hidden(final Term hidden) {

		if ( hidden == null ) {
			throw new NullPointerException("null hidden");
		}

		edges.remove(hidden);
		edges.remove(hidden.reverse());

		for (final List<Term> terms : edges.values()) {
			terms.remove(hidden);
		}

		return this;
	}

	public BookData symmetric(final Term symmetric) {

		if ( symmetric == null ) {
			throw new NullPointerException("null symmetric");
		}

		return inverse(symmetric, symmetric);
	}

	public BookData inverse(final Term direct, final Term inverse) {

		if ( direct == null ) {
			throw new NullPointerException("null direct");
		}

		if ( inverse == null ) {
			throw new NullPointerException("null inverse");
		}

		final List<Term> sources=edges.remove(inverse.reverse());

		if ( sources != null ) {

			final List<Term> targets=edges.remove(direct);

			if ( targets != null ) { // merge, deduplicating and preserving order

				final Collection<Term> merged=new LinkedHashSet<>();

				merged.addAll(targets);
				merged.addAll(sources);

				edges.put(direct, new ArrayList<>(merged));

			} else {

				links.put(direct, direct); // insert missing labelling information
				edges.put(direct, sources);

			}

		}

		return this;
	}


	//// Accessors /////////////////////////////////////////////////////////////////////////////////////////////////////

	public String text(final Term link) {

		if ( link == null ) {
			throw new NullPointerException("null link");
		}

		final List<Term> terms=edges.get(link);

		if ( terms != null ) {

			for (final String lang : Specs.Langs) { // look for preferred language localizations
				for (final Term term : terms) {
					if ( term.isPlain(lang) ) {
						try { return term.getText(); } finally { terms.remove(term); }
					}
				}
			}

			for (final Term term : terms) { // fall back to the first plain term
				if ( term.isPlain() || term.isTyped(XSDString)) {
					try { return term.getText(); } finally { terms.remove(term); }
				}
			}

		}

		return "";
	}

	public String data(final Iterable<Term> links) {

		if ( links == null ) {
			throw new NullPointerException("null links");
		}

		for (final Term link : links) {

			final List<Term> terms=edges.get(link);

			if ( terms != null && !terms.isEmpty() ) {
				return terms.remove(0).getText();
			}
		}

		return "";
	}


	public Map<Term, List<Term>> collections(final Term link) {

		final List<Term> terms=edges.remove(link);

		if ( terms == null ) { return emptyMap(); } else {

			final List<Term> collections=new ArrayList<>();

			for (final Term resource : terms) {
				collections.add(resource.reverse()); // open as collection
			}

			return singletonMap(link(link), collections);

		}

	}

	public Map<Term, List<Term>> resources(final Term link) {

		if ( link == null ) {
			throw new NullPointerException("null link");
		}

		final List<Term> terms=edges.remove(link);

		if ( terms == null ) { return emptyMap(); } else {

			final List<Term> resources=new ArrayList<>();

			for (final String lang : Specs.Langs) { // look for preferred language localizations
				if ( resources.isEmpty() ) {
					for (final Term term : terms) {
						if ( term.isPlain(lang) ) { resources.add(term); }
					}
				}
			}

			for (final Term term : terms) { // include all non-localized terms
				if ( !term.isTagged() && !resources.contains(term) ) { // only if not already included as tagless localization
					resources.add(term);
				}
			}

			return singletonMap(link(link), resources);

		}
	}

	public Map<Term, List<Term>> resources(final Filter filter) {

		if ( filter == null ) {
			throw new NullPointerException("null filter");
		}

		final Map<Term, List<Term>> matches=new LinkedHashMap<>();

		for (final Map.Entry<Term, List<Term>> entry : edges.entrySet()) {

			final Term link=entry.getKey();
			final List<Term> terms=entry.getValue();

			for (final Iterator<Term> iterator=terms.iterator(); iterator.hasNext(); ) {

				final Term term=iterator.next();

				if ( filter.test(link, term) ) {

					List<Term> leaves=matches.get(link);

					if ( leaves == null ) {
						matches.put(link, leaves=new ArrayList<>());
					}

					leaves.add(term);
					iterator.remove();
				}
			}

		}

		return matches;
	}


	private Term link(final Term link) { // retrieve labelled link from result set

		final Term labelled=links.get(link);

		return labelled.getLabel().isEmpty() ? link : labelled; // fall back to well-known name if unlabelled // !!! l10n?
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static interface Filter {

		public boolean test(final Term link, final Term term);

	}

}
