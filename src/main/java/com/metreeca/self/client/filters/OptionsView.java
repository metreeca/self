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

package com.metreeca.self.client.filters;

import com.metreeca._tile.client.*;
import com.metreeca.self.client.views.Input;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.client.views.TermView;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.forms.Values;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.*;

import static com.metreeca._tile.client.Tile.$;

import static java.lang.Math.ceil;
import static java.lang.Math.log10;
import static java.lang.Math.max;
import static java.util.Collections.unmodifiableSet;


/**
 * Multi-valued facet filter.
 */
public final class OptionsView extends View {

	private static final int Detail=10; // the maximum number of values shown in expanded facets


	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("OptionsView.css") TextResource skin();

	}


	private Report report;

	private Values values;
	private Set<Term> selection;

	private String pattern="";
	private int page;

	private boolean sampling;
	private boolean slicing;


	private final Input input;

	private final Tile items;

	private final Tile status;
	private final Tile count;
	private final Tile prev;
	private final Tile next;


	public OptionsView() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append($(input=new Input().search(true).immediate(true))

						.change(new Action<Event>() {
							@Override public void execute(final Event e) {
								e.stop().cancel();
								pattern(input.value());
							}
						}))

				.append(items=$("<ul/>"))

				.append(status=$("<footer/>").is("status", true)

						.append(count=$("<span/>"))

						.append(prev=$("<button/>").is("prev", true)

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { page(page-1); }
								}))

						.append(next=$("<button/>").is("next", true)

								.click(new Action<Event>() {
									@Override public void execute(final Event e) { page(page+1); }
								})))

				.<Bus>as()

				.sampling(new Action<Boolean>() {
					@Override public void execute(final Boolean sampling) { sampling(sampling); }
				})

				.slicing(new Action<Boolean>() {
					@Override public void execute(final Boolean slicing) { slicing(slicing); }
				});
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public OptionsView report(final Report report) {

		this.report=report;

		return render();
	}


	public Values values() {
		return values;
	}

	public OptionsView values(final Values values) {

		this.values=values;

		this.pattern="";
		this.page=0;

		return render();
	}


	public Set<Term> selection() {
		return selection == null ? null : unmodifiableSet(selection);
	}

	public OptionsView selection(final Set<Term> selection) {

		this.selection=new LinkedHashSet<>(selection); // ensure is writable

		this.pattern="";
		this.page=0;

		return render();
	}


	public String pattern() {
		return pattern;
	}

	public OptionsView pattern(final String pattern) {

		if ( pattern == null ) {
			throw new NullPointerException("null pattern");
		}

		this.pattern=pattern;
		this.page=0;

		return render();
	}


	public int page() {
		return page;
	}

	public OptionsView page(final int page) {

		if ( page < 0 ) {
			throw new IllegalArgumentException("illegal page ["+page+"]");
		}

		this.page=page;

		return render();
	}


	private OptionsView sampling(final Boolean sampling) {

		this.sampling=sampling;

		return render();
	}

	private OptionsView slicing(final Boolean slicing) {

		this.slicing=slicing;

		return render();
	}


	//// Actions ///////////////////////////////////////////////////////////////////////////////////////////////////////

	private void select(final Term value, final boolean selected) {

		if ( selected ) {
			selection.add(value);
		} else {
			selection.remove(value);
		}

		root().change();
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private OptionsView render() {

		final Set<Term> selection=selection();
		final Values values=values();

		if ( selection != null && values != null ) {

			final Specs specs=values.getSpecs();
			final Path path=values.getPath();

			final Map<Term, Integer> entries=values.getEntries();

			final boolean filtered=entries.size() > Detail || !$(input).value().isEmpty();
			final boolean locked=report.isLocked();
			final boolean sliced=slicing && !path.isAggregate() && !specs.isProjected(path.getSteps()); // !!! review
			final String pattern=this.pattern.toUpperCase();
			final String width=width(entries);

			int options=0; // visible options count

			$(input).visible(filtered);

			items.clear();

			for (final Map.Entry<Term, Integer> entry : entries.entrySet()) { // matched user-selected options

				final Term term=entry.getKey();
				final Integer count=entry.getValue();

				if ( selection.contains(term) ) {

					items.append(item(term, count, width, locked, true, sampling, sliced));

					++options;
				}
			}

			for (final Term term : selection) { // unmatched user-selected options
				if ( !entries.containsKey(term) ) {

					items.append(item(term, 0, width, locked, true, sampling, sliced).is("empty", true));

					++options;
				}
			}

			for (final Map.Entry<Term, Integer> entry : entries.entrySet()) { // matched unselected options

				final Term term=entry.getKey();
				final Integer count=entry.getValue();

				if ( !selection.contains(term) ) {
					if ( term != null && (pattern.isEmpty() || term.label().toUpperCase().contains(pattern)) ) {

						if ( options >= page*Detail && options < (page+1)*Detail ) {
							items.append(item(term, count, width, locked, false, sampling, sliced));
						}

						++options;
					}
				}
			}

			status.visible(filtered && options > Detail);
			count.text(options == 0 ? "no options"
					: options == 1 ? "1 option"
					: entries.size() < values.getLimit() ? options+" options"
					: "more than "+NumberFormat.getDecimalFormat().format(values.getLimit())+" options");

			prev.enabled(page > 0);
			next.enabled(page < options/Detail);
		}

		return this;
	}

	private Tile item(final Term term, final int count, final String width,  // !!! refactor
			final boolean locked, final boolean checked, final boolean sampled, final boolean sliced) {

		return $("<li/>")

				.append($("<input/>")

						.attribute("type", "checkbox")

						.enabled(!locked)
						.checked(checked)

						.change(new Action<Event>() {
							@Override public void execute(final Event e) {
								e.stop().cancel(); // refired by select()
								select(term, e.current().checked());
							}
						}))

				.append(new TermView().inline(true).term(term))

				.append($("<var/>")
						.is("sampled", sampled)
						.is("sliced", sliced)
						.style("min-width", width)
						.text(String.valueOf(count)));
	}


	private String width(final Map<Term, Integer> entries) {

		// compute the allotted width for the count field
		// take into account all values to avoid accordion effects when altering selection

		int max=1;

		for (final Map.Entry<Term, Integer> entry : entries.entrySet()) {

			final Term term=entry.getKey();
			final Integer count=entry.getValue();

			if ( term != null ) {
				max=max(max, count);
			}
		}

		return (int)ceil(log10(max+1))+"em";
	}

}
