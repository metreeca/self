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

package com.metreeca.self.shared.sparql;


import com.metreeca.self.shared.beans.Term;
import com.metreeca.self.shared.beans.schemas.XSD;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static java.lang.Float.isNaN;


/**
 * Stats about a field in a results set.
 */
public final class Stats {

	public static final Stats Nil=new Stats("");


	private final String type; // the most common type for the field

	private int samples; // the total number of sampled terms

	private int labels; // the number of labelled terms in the field
	private int notes; // the number of annotated terms in the field
	private int images; // the number of visual terms in the field
	private int points; // the number of spatial terms in the field

	private Object min; // the minimum type value in the field
	private Object max; // the maximum type value in the field


	public Stats(final String type) {

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		this.type=type;
	}

	public Stats(final String type, final int samples, final Object min, final Object max) {

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		if ( samples < 0 ) {
			throw new IllegalArgumentException("illegal samples ["+samples+"]");
		}

		this.type=type;
		this.samples=samples;
		this.min=min;
		this.max=max;
	}


	public boolean isNumeric() {
		return isInteger() || isDecimal()
				|| isShort() || isInt() || isLong()
				|| isFloat() || isDouble();
	}

	public boolean isInteger() {
		return type.equals(XSD.XSDInteger);
	}

	public boolean isDecimal() {
		return type.equals(XSD.XSDDecimal);
	}

	public boolean isByte() {
		return type.equals(XSD.XSDByte);
	}

	public boolean isShort() {
		return type.equals(XSD.XSDShort);
	}

	public boolean isInt() {
		return type.equals(XSD.XSDInt);
	}

	public boolean isLong() {
		return type.equals(XSD.XSDLong);
	}

	public boolean isFloat() {
		return type.equals(XSD.XSDFloat);
	}

	public boolean isDouble() {
		return type.equals(XSD.XSDDouble);
	}


	public boolean isTemporal() {
		return isDate() || isTime() || isDateTime() || isYear();
	}

	public boolean isDate() {
		return type.equals(XSD.XSDDate);
	}

	public boolean isTime() {
		return type.equals(XSD.XSDTime);
	}

	public boolean isDateTime() {
		return type.equals(XSD.XSDDateTime);
	}

	public boolean isYear() {
		return type.equals(XSD.XSDGYear);
	}


	public boolean isOther() {
		return !isNumeric() && !isTemporal();
	}


	public String getType() {
		return type;
	}


	public int getSamples() {
		return samples;
	}


	public int getLabels() {
		return labels;
	}

	public int getNotes() {
		return notes;
	}

	public int getImages() {
		return images;
	}

	public int getPoints() {
		return points;
	}


	public Object getMin() {
		return min;
	}

	public Object getMax() {
		return max;
	}


	@SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "unchecked"})
	public static final class Builder {

		private static final Comparator<Stats> ByCount=new Comparator<Stats>() {
			@Override public int compare(final Stats x, final Stats y) {
				return x.samples > y.samples ? -1 : x.samples < y.samples ? 1 : 0;
			}
		};


		private final Map<String, Stats> bins=new LinkedHashMap<>(); // term type > stats (preserve insertion priority)


		public Stats stats() {

			final List<Stats> stats=new ArrayList<>(bins.values());

			Collections.sort(stats, ByCount);

			if ( stats.isEmpty() ) { return Nil; } else {

				final Stats target=stats.get(0);

				for (int i=1; i < stats.size(); ++i) {
					merge(target, stats.get(i)); // handle promotions to the most frequent compatible type
				}

				return target;

			}
		}

		private void merge(final Stats target, final Stats source) {

			final String to=target.type;
			final String from=source.type;

			final Object min=source.min;
			final Object max=source.max;

			if ( to.equals(XSD.XSDString) && from.equals(Term.Simple)
					|| to.equals(Term.Simple) && from.equals(XSD.XSDString) ) {

				merge(target, source, (String)min, (String)max);

			} else if ( to.equals(XSD.XSDDecimal)
					&& from.equals(XSD.XSDInteger) ) {

				merge(target, source,
						min == null ? null : new BigDecimal((BigInteger)min),
						max == null ? null : new BigDecimal((BigInteger)max));

			} else if ( to.equals(XSD.XSDDecimal)
					&& (from.equals(XSD.XSDDouble) || from.equals(XSD.XSDFloat)) ) {

				final Double dmin=min == null ? null : ((Number)min).doubleValue();
				final Double dmax=max == null ? null : ((Number)max).doubleValue();

				merge(target, source,
						dmin == null || Double.isInfinite(dmin) || Double.isNaN(dmin) ? null : new BigDecimal(dmin),
						dmax == null || Double.isInfinite(dmax) || Double.isNaN(dmax) ? null : new BigDecimal(dmax));

			} else if ( to.equals(XSD.XSDDecimal)
					&& from.equals(XSD.XSDLong) ) {

				merge(target, source,
						min == null ? null : new BigDecimal((Long)min),
						max == null ? null : new BigDecimal((Long)max));

			} else if ( to.equals(XSD.XSDDecimal)
					&& (from.equals(XSD.XSDInt) || from.equals(XSD.XSDShort) || from.equals(XSD.XSDByte)) ) {

				merge(target, source,
						min == null ? null : new BigDecimal(((Integer)min).intValue()),
						max == null ? null : new BigDecimal(((Integer)max).intValue()));

			} else if ( to.equals(XSD.XSDInteger)
					&& (from.equals(XSD.XSDLong) || from.equals(XSD.XSDInt) || from.equals(XSD.XSDShort) || from.equals(XSD.XSDByte)) ) {

				merge(target, source,
						min == null ? null : BigInteger.valueOf(((Number)min).longValue()),
						max == null ? null : BigInteger.valueOf(((Number)max).longValue()));

			} else if ( to.equals(XSD.XSDLong)
					&& (from.equals(XSD.XSDInt) || from.equals(XSD.XSDShort) || from.equals(XSD.XSDByte)) ) {

				merge(target, source,
						min == null ? null : ((Number)min).longValue(),
						max == null ? null : ((Number)max).longValue());

			} else if ( to.equals(XSD.XSDInt)
					&& (from.equals(XSD.XSDShort) || from.equals(XSD.XSDByte)) ) {

				merge(target, source,
						min == null ? null : ((Number)min).intValue(),
						max == null ? null : ((Number)max).intValue());

			} else if ( to.equals(XSD.XSDShort)
					&& from.equals(XSD.XSDByte) ) {

				merge(target, source,
						min == null ? null : ((Number)min).shortValue(),
						max == null ? null : ((Number)max).shortValue());

			} else if ( to.equals(XSD.XSDDouble)
					&& from.equals(XSD.XSDFloat) ) {

				merge(target, source,
						min == null ? null : ((Number)min).doubleValue(),
						max == null ? null : ((Number)max).doubleValue());

			} else if ( to.equals(XSD.XSDDouble)
					&& (from.equals(XSD.XSDLong) || from.equals(XSD.XSDInt) || from.equals(XSD.XSDShort) || from.equals(XSD.XSDByte)) ) {

				merge(target, source,
						min == null ? null : ((Number)min).doubleValue(),
						max == null ? null : ((Number)max).doubleValue());

			} else if ( to.equals(XSD.XSDFloat)
					&& (from.equals(XSD.XSDLong) || from.equals(XSD.XSDInt) || from.equals(XSD.XSDShort) || from.equals(XSD.XSDByte)) ) {

				merge(target, source,
						min == null ? null : ((Number)min).floatValue(),
						max == null ? null : ((Number)max).floatValue());

			}
		}

		private <T extends Comparable<? super T>> void merge(
				final Stats target, final Stats source, final T min, final T max) {

			target.samples+=source.samples;

			target.labels+=source.labels;
			target.notes+=source.notes;
			target.images+=source.images;
			target.points+=source.points;

			target.min=min((T)target.min, min);
			target.max=max((T)target.max, max);
		}


		public Builder sample(final Term term) {

			if ( term != null ) {

				final String type=term.getType();
				final Object value=term.getValue();

				Stats stats=bins.get(type);

				if ( stats == null ) {
					bins.put(type, stats=new Stats(type));
				}

				++stats.samples;

				if ( !term.getLabel().isEmpty() ) { ++stats.labels; }
				if ( !term.getNotes().isEmpty() ) { ++stats.notes; }
				if ( !term.getImage().isEmpty() ) { ++stats.images; }

				if ( !isNaN(term.getLat()) && !isNaN(term.getLng()) ) { ++stats.points; }

				if ( value instanceof Comparable ) {
					stats.min=min((Comparable<Object>)stats.min, (Comparable<Object>)value);
					stats.max=max((Comparable<Object>)stats.max, (Comparable<Object>)value);
				}

			}

			return this;
		}


		private <T extends Comparable<? super T>> T min(final T x, final T y) {
			return x == null ? y : y == null ? x : x.compareTo(y) <= 0 ? x : y;
		}

		private <T extends Comparable<? super T>> T max(final T x, final T y) {
			return x == null ? y : y == null ? x : x.compareTo(y) >= 0 ? x : y;
		}

	}

}
