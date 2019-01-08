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

package com.metreeca.self.client.filters;

import com.metreeca._jeep.shared.async.Handler;
import com.metreeca._jeep.shared.async.Morpher;
import com.metreeca._jeep.shared.async.Promise;
import com.metreeca._tile.client.Action;
import com.metreeca._tile.client.Event;
import com.metreeca._tile.client.View;
import com.metreeca._tool.client.views.Input;
import com.metreeca.self.client.Self.Bus;
import com.metreeca.self.shared.Report;
import com.metreeca.self.shared.beans.Path;
import com.metreeca.self.shared.beans.Specs;
import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.beans.schemas.XSD;
import com.metreeca.self.shared.forms.Ranges;
import com.metreeca.self.shared.forms.Values;
import com.metreeca.self.shared.sparql.Stats;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

import java.util.Date;

import static com.metreeca._jeep.shared.async.Promises.promise;
import static com.metreeca.self.shared.beans.Term.typed;

import static java.lang.Math.abs;


public final class RangeView extends View {

	public static final double MinDecimal=10.0e-2; // the smallest number to be formatted as decimal
	public static final double MaxDecimal=10.0e+6; // the largest number to be formatted as decimal

	// ;(ie) test comma-spaced numbers before plain ones

	private static final String TextPattern="";

	private static final String IntegerPattern="[-+]?(\\d{1,3}(,\\d{3})+|\\d+)"; // !!! i18n
	private static final String DecimalPattern="[-+]?(\\d{1,3}(,\\d{3})+|\\d+)(\\.\\d+)?"; // !!! i18n
	private static final String DoublePattern="[-+]?(\\d{1,3}(,\\d{3})+|\\d+)(\\.\\d+)?([eE][-+]?\\d+)?"; // !!! i18n

	private static final String DatePattern="\\d{4}-\\d{2}-\\d{2}";
	private static final String YearPattern="[12]\\d{3}";

	// !!! share formats with TermView

	private static final NumberFormat DecimalFormat=NumberFormat.getFormat("#,##0.##");
	private static final NumberFormat IntegerFormat=NumberFormat.getFormat("#,##0");
	private static final NumberFormat ScientificFormat=NumberFormat.getScientificFormat();
	private static final DateTimeFormat YearFormat=DateTimeFormat.getFormat("yyyy");
	private static final DateTimeFormat DateFormat=DateTimeFormat.getFormat("yyyy-MM-dd");
	private static final DateTimeFormat TimeFormat=DateTimeFormat.getFormat("yyyy-MM-ddyyyy-MM-dd'T'hh:mm:ss");


	private static final Resources resources=GWT.create(Resources.class);


	public static interface Resources extends ClientBundle {

		@Source("RangeView.css") TextResource skin();

	}


	private Report report;

	private Term lower;
	private Term upper;

	private boolean sampling;
	private boolean slicing;

	private Promise<Values> values;
	private Promise<Stats> stats;

	private boolean changed;

	private final Input lowerInput;
	private final Input upperInput;

	private final Action<Event> update=new Action<Event>() {
		@Override public void execute(final Event e) {
			if ( changed ) {
				try {

					stats().then(new Handler<Stats>() {
						@Override public void value(final Stats stats) {
							filter(parse(stats, lowerInput.value()), parse(stats, upperInput.value()));
							root().fire("change"); // signal parent listeners
						}
					});

				} finally {
					changed=false;
				}
			}
		}
	};


	public RangeView() {
		root("<div/>")

				.skin(resources.skin().getText())

				.append(lowerInput=new Input())
				.append(upperInput=new Input())

				.bind("change", new Action<Event>() {
					@Override public void execute(final Event e) {
						if ( !e.target().is("range view") ) { // don't listen to refired events
							try {

								e.stop(); // trap the event: will be refired by update
								schedule();

							} finally {
								changed=true;
							}
						}

					}
				}, true) // capture change events before regular listeners see them

				.bind("keydown", new Action<Event>() { // ;(ff) won't generate keypress on return in input field

					@Override public void execute(final Event e) {
						if ( e.key("return") ) {
							e.target().blur(); // ;(ie) force change event on enter
							schedule();
						}
					}

				})

				.bind("blur", new Action<Event>() {

					@Override public void execute(final Event e) {
						schedule(); // losing focus from a child input > schedule updated
					}

				}, true) // capture non-bubbling focus events from children

				.bind("focus", new Action<Event>() {

					@Override public void execute(final Event e) {
						cancel(); // focusing to a child input > cancel pending updates
					}

				}, true) // capture non-bubbling focus events from children


				.<Bus>as()

				.sampling(new Action<Boolean>() {
					@Override public void execute(final Boolean sampling) { sampling(sampling); }

				})

				.slicing(new Action<Boolean>() {
					@Override public void execute(final Boolean slicing) { slicing(slicing); }
				});
	}


	private void schedule() {
		root().cancel(update).delay(100, update); // after a grace period for focusing events to children inputs
	}

	private void cancel() {
		root().cancel(update);
	}


	//// Model /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public RangeView report(final Report report) {

		this.report=report;
		this.values=null;
		this.stats=null;

		return render();
	}


	public Term lower() {
		return lower;
	}

	public RangeView lower(final Term lower) {

		this.lower=lower;

		return render();
	}


	public Term upper() {
		return upper;
	}

	public RangeView upper(final Term upper) {

		this.upper=upper;

		return render();
	}


	private RangeView sampling(final Boolean sampling) {

		this.sampling=sampling;

		return render();
	}

	private RangeView slicing(final Boolean slicing) {

		this.slicing=slicing;

		return render();
	}


	private Promise<Values> values() {

		if ( values == null ) {
			values=promise();
		}

		return values;
	}

	public RangeView values(final Values values) { // !!! private

		values().value(values); // !!! review
		this.stats=null;

		return render();
	}


	private Promise<Stats> stats() {

		if ( stats == null ) {
			stats=values().pipe(new Morpher<Values, Stats>() {
				@Override public Promise<Stats> value(final Values values) {

					final long entries=values.getEntries().size();
					final long limit=values.getLimit();

					return entries == 0 ? fallback(lower, upper) // no values stats: fallback to range limits
							: limit > 0 && entries == limit ? computed(values.getPath()) // clipped values: compute stats with ad-hoc query
							: promise(values.getStats());
				}
			});
		}

		return stats;
	}

	private static Promise<Stats> fallback(final Term lower, final Term upper) {
		return promise(new Stats.Builder().sample(lower).sample(upper).stats());
	}

	private Promise<Stats> computed(final Path path) {

		final Promise<Stats> stats=promise();

		root().fire(new Ranges()

				.setEndpoint(report.getEndpoint())
				.setSpecs(report.getSpecs())
				.setPath(path)

				.then(new Handler<Ranges>() {
					@Override public void value(final Ranges ranges) {
						stats.value(ranges.getEntries().get(0));
					}
				}));

		return stats;
	}


	//// View //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private RangeView render() {

		values().then(new Handler<Values>() {
			@Override public void value(final Values values) {
				stats().then(new Handler<Stats>() {
					@Override public void value(final Stats stats) {

						final Specs specs=values.getSpecs();
						final Path path=values.getPath();

						final boolean locked=report.isLocked();
						final boolean sliced=slicing && !path.isAggregate() && !specs.isProjected(path.getSteps()); // !!! review

						final Term lower=lower();
						final Term upper=upper();

						final boolean empty=values.getEntries().isEmpty();
						final boolean year=values.getPath().getTransform() == Path.Transform.Year; // !!! review

						final String pattern=pattern(stats, year);

						lowerInput
								.pattern(pattern)
								.enabled(!locked)
								.placeholder((sampling ? "≤ " : sliced ? "≥ " : "")
										+format(stats, lower != null ? lower.getValue() : !empty ? stats.getMin() : null, year))
								.value(format(stats, lower != null ? lower.getValue() : null, year));

						upperInput
								.pattern(pattern)
								.enabled(!locked)
								.placeholder((sampling ? "≥ " : sliced ? "≤ " : "")
										+format(stats, upper != null ? upper.getValue() : !empty ? stats.getMax() : null, year))
								.value(format(stats, upper != null ? upper.getValue() : null, year));
					}
				});
			}
		});


		return this;
	}


	private String pattern(final Stats stats, final boolean year) {
		return year ? YearPattern

				: stats.isInteger() ? IntegerPattern
				: stats.isDecimal() ? DecimalPattern

				: stats.isByte() ? IntegerPattern
				: stats.isShort() ? IntegerPattern
				: stats.isInt() ? IntegerPattern
				: stats.isLong() ? IntegerPattern
				: stats.isFloat() ? DoublePattern
				: stats.isDouble() ? DoublePattern

				: stats.isDate() ? DatePattern
				: stats.isDateTime() ? DatePattern
				: stats.isYear() ? YearPattern

				: TextPattern;
	}

	private String format(final Stats stats, final Object value, final boolean year) {
		try {

			return value == null ? ""

					: year ? String.valueOf(value)

					: stats.isInteger() ? IntegerFormat.format((Number)value)
					: stats.isDecimal() ? DecimalFormat.format((Number)value)

					// lenient formatting: accept numeric super-types

					: stats.isByte() ? IntegerFormat.format((Number)value)
					: stats.isShort() ? IntegerFormat.format((Number)value)
					: stats.isInt() ? IntegerFormat.format((Number)value)
					: stats.isLong() ? IntegerFormat.format((Number)value)
					: stats.isFloat() ? real(((Number)value).doubleValue())
					: stats.isDouble() ? real(((Number)value).doubleValue())

					: stats.isDate() ? DateFormat.format((Date)value) // !!! timezone
					: stats.isDateTime() ? DateFormat.format((Date)value) // !!! timezone // !!! time
					: stats.isYear() ? YearFormat.format((Date)value) // !!! timezone

					: String.valueOf(value);

		} catch ( final IllegalArgumentException ignored ) {
			return null;
		}
	}


	private String real(final double value) {
		return value == 0.0 || abs(value) >= MinDecimal && abs(value) <= MaxDecimal
				? DecimalFormat.format(value)
				: ScientificFormat.format(value);
	}


	private Term parse(final Stats stats, final String text) {
		return text.isEmpty() ? null

				: stats.isInteger() ? typed(clean(text), XSD.XSDInteger)
				: stats.isDecimal() ? typed(clean(text), XSD.XSDDecimal)

				: stats.isByte() ? typed(clean(text), XSD.XSDByte)
				: stats.isShort() ? typed(clean(text), XSD.XSDShort)
				: stats.isInt() ? typed(clean(text), XSD.XSDInt)
				: stats.isLong() ? typed(clean(text), XSD.XSDLong)
				: stats.isFloat() ? typed(clean(text), XSD.XSDFloat)
				: stats.isDouble() ? typed(clean(text), XSD.XSDDouble)

				: stats.isDate() ? typed(text, XSD.XSDDate) // !!! timezone
				: stats.isDateTime() ? typed(text+"T00:00:00Z", XSD.XSDDateTime) // !!! timezone
				: stats.isYear() ? typed(text, XSD.XSDGYear) // !!! timezone

				: null;
	}


	private String clean(final String text) {
		return text.replace(",", ""); // !!! i18n
	}


	//// Action ////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void filter(final Term lower, final Term upper) {

		this.lower=lower;
		this.upper=upper;

	}

}
