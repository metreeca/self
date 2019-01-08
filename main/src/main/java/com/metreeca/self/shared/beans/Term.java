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

package com.metreeca.self.shared.beans;


import com.metreeca._bean.shared.Bean;
import com.metreeca.self.shared.beans.schemas.RDF;
import com.metreeca.self.shared.beans.schemas.RDFS;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Date;

import static com.metreeca.self.shared.beans.schemas.XSD.*;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Character.toLowerCase;
import static java.lang.Float.NaN;
import static java.lang.Float.isNaN;
import static java.lang.Integer.parseInt;


@Bean({"text", "type"}) public final class Term {

	private static final String Blank="blank";
	private static final String Recto="recto";
	private static final String Verso="verso";

	// ;(graphdb 6.1) doesn't parse simple literals as xsd:string as per RDF 1.1

	public static final String Simple="";
	public static final String Tagged="@";


	private static final String NamedURI="^(.*?)([/#:])([^/#:]+)(/|#|#_|#id|#this)?$"; // named slash/hash uris pattern

	private static final long DateOrigin=new Date(-1899, 0, 1).getTime();


	public static Term blank(final String id) {
		return new Term(id, Blank);
	}

	public static Term named(final String iri) {
		return new Term(iri, Recto);
	}

	public static Term named(final String iri, final String name) {
		return new Term(iri+name, Recto);
	}

	public static Term plain(final String text) {
		return new Term(text, Simple);
	}

	public static Term plain(final String text, final String lang) {
		return new Term(text, lang == null || lang.isEmpty() ? Simple : lang.startsWith(Tagged) ? lang : '@'+lang);
	}

	public static Term typed(final String text, final String type) {
		return new Term(text, type);
	}


	public static Term typed(final String value) { return new Term(value, XSDString); }

	public static Term typed(final boolean value) { return new Term(String.valueOf(value), XSDBoolean); }

	public static Term typed(final byte value) { return new Term(String.valueOf(value), XSDByte); }

	public static Term typed(final short value) { return new Term(String.valueOf(value), XSDShort); }

	public static Term typed(final int value) { return new Term(String.valueOf(value), XSDInt); }

	public static Term typed(final long value) { return new Term(String.valueOf(value), XSDLong); }

	public static Term typed(final float value) { return new Term(String.valueOf(value), XSDFloat); }

	public static Term typed(final double value) { return new Term(String.valueOf(value), XSDDouble); }

	public static Term typed(final BigInteger value) { return new Term(String.valueOf(value), XSDInteger); }

	public static Term typed(final BigDecimal value) { return new Term(String.valueOf(value), XSDDecimal); }

	@SuppressWarnings("deprecation")
	// Date.UTC > Calendar not portable to GWT // !!! replace with client/server solution
	public static Term typed(final Date value) {

		final int year=value.getYear()+1900;
		final int month=value.getMonth()+1;
		final int day=value.getDate();
		final int hours=value.getHours();
		final int minutes=value.getMinutes();
		final int seconds=value.getSeconds();

		return new Term((value.getTime() < DateOrigin ? "-" : "")

				+(year < 10 ? "000" : year < 100 ? "00" : year < 1000 ? "0" : "")+year+'-'
				+(month < 10 ? "0" : "")+month+'-'
				+(day < 10 ? "0" : "")+day+'T'

				+(hours < 10 ? "0" : "")+hours+':'
				+(minutes < 10 ? "0" : "")+minutes+':'
				+(seconds < 10 ? "0" : "")+seconds+'Z',

				XSDDateTime);
	}


	/**
	 * Human-readable term ordering.
	 */
	public static final Comparator<Term> TextualOrder=new Comparator<Term>() {

		@Override public int compare(final Term x, final Term y) { // keep in sync with Editor.criterion()

			final int i=(x == null) ? 0 : x.isBlank() ? 1 : x.isRecto() ? 3 : x.isVerso() ? 4 : 2;
			final int j=(y == null) ? 0 : y.isBlank() ? 1 : y.isRecto() ? 3 : y.isVerso() ? 4 : 2;

			return i < j ? -1 : i > j ? 1
					: i == 1 ? blank(x, y)
					: i == 2 ? literal(x, y)
					: i == 3 ? named(x, y)
					: i == 4 ? named(x, y)
					: 0;

		}

		private int blank(final Term x, final Term y) {
			return x.text.compareTo(y.text);
		}

		private int literal(final Term x, final Term y) {

			final Object v=x.getValue();
			final Object w=y.getValue();

			// !!! handle numeric conversions

			return v instanceof Comparable && w instanceof Comparable && v.getClass().equals(w.getClass()) ? ((Comparable<Object>)v).compareTo(w)
					: x.text.compareTo(y.text);

		}

		private int named(final Term x, final Term y) {

			final int i=x.label.isEmpty() ? 1 : 0;
			final int j=y.label.isEmpty() ? 1 : 0;

			return i < j ? -1 : i > j ? 1
					: i == 0 ? x.label.compareToIgnoreCase(y.label)
					: i == 1 ? x.text.compareTo(y.text)
					: 0;

		}

	};


	private final String text;
	private final String type;

	private String label="";
	private String notes="";
	private String image="";

	private float lat=NaN; // !!! review default value to avoid serialization pollution
	private float lng=NaN;

	private Object value;


	public Term(final String text, final String type) {

		if ( text == null ) {
			throw new NullPointerException("null text");
		}

		if ( type == null ) {
			throw new NullPointerException("null type");
		}

		this.type=type;
		this.text=text;
	}

	public Term copy() {

		final Term copy=new Term(text, type);

		copy.label=label;
		copy.notes=notes;
		copy.image=image;

		copy.lat=lat;
		copy.lng=lng;

		copy.value=value;

		return copy;
	}


	public boolean isBlank() {
		return type.equals(Blank);
	}

	public boolean isNamed() {
		return type.equals(Recto) || type.equals(Verso);
	}

	public boolean isRecto() {
		return type.equals(Recto);
	}

	public boolean isVerso() {
		return type.equals(Verso);
	}

	public boolean isLiteral() {
		return !isBlank() && !isNamed();
	}

	public boolean isPlain() {
		return isSimple() || isTagged();
	}

	public boolean isPlain(final String lang) {

		if ( lang == null ) {
			throw new NullPointerException("null lang");
		}

		return lang.isEmpty() && isSimple() || isTagged(lang);
	}

	public boolean isSimple() {
		return type.equals(Simple); // xsd:string must be handled as typed as not all backends implement RDF 1.1
	}

	public boolean isTagged() {
		return type.startsWith(Tagged);
	}

	public boolean isTagged(final String lang) {

		if ( lang == null ) {
			throw new NullPointerException("null lang");
		}

		return type.length() == Tagged.length()+lang.length() && type.startsWith(Tagged) && type.endsWith(lang);
	}

	public boolean isTyped() {
		return !isBlank() && !isNamed() && !isPlain();
	}

	public boolean isTyped(final String type) {
		return this.type.equals(type);
	}


	public boolean isMeta() {
		return equals(RDFS.RDFSLabel) || equals(RDFS.RDFSComment);
	}

	public boolean isExternal() {
		return text.startsWith("http://")
				|| text.startsWith("https://")
				|| text.startsWith("mailto:")
				|| type.equals(XSDAnyURI);
	}

	public boolean isVisual() {
		if ( text.startsWith("data:image/") ) {

			return true;

		} else if ( text.matches("^\\w+:.*$") ) { // absolute IRI

			final int query=text.lastIndexOf('?'); // ignore query string (e.g. in DBpedia thumbnails)

			final String lower=(query < 0 ? text : text.substring(0, query)).toLowerCase();

			return lower.endsWith(".png")
					|| lower.endsWith(".gif")
					|| lower.endsWith(".jpg")
					|| lower.endsWith(".svg");

		} else {
			return false;
		}
	}

	public boolean isNumeric() {
		return type.equals(XSDInteger) || type.equals(XSDDecimal)
				|| type.equals(XSDByte) || type.equals(XSDShort) || type.equals(XSDInt) || type.equals(XSDLong)
				|| type.equals(XSDFloat) || type.equals(XSDDouble);
	}

	public boolean isTemporal() {
		return type.equals(XSDDate) || type.equals(XSDTime) || type.equals(XSDDateTime)
				|| type.equals(XSDGYear) || type.equals(XSDGMonth) || type.equals(XSDGDay);
	}

	public boolean isSpatial() {
		return !isNaN(lat) || !isNaN(lng);
	}


	public String getText() {
		return text;
	}

	public String getType() {
		return type;
	}

	public String getLang() {
		return type.startsWith(Tagged) ? type.substring(Tagged.length()) : "";
	}


	public String getLabel() {
		return label;
	}

	public Term setLabel(final String label) {

		if ( label == null ) {
			throw new NullPointerException("null label");
		}

		this.label=label;

		return this;
	}


	public String getNotes() {
		return notes;
	}

	public Term setNotes(final String notes) {

		if ( notes == null ) {
			throw new NullPointerException("null notes");
		}

		this.notes=notes;

		return this;
	}


	public String getImage() {
		return image;
	}

	public Term setImage(final String image) {

		if ( image == null ) {
			throw new NullPointerException("null image");
		}

		this.image=image;

		return this;
	}


	public float getLat() {
		return lat;
	}

	public Term setLat(final float lat) {

		this.lat=lat;

		return this;
	}

	public float getLng() {
		return lng;
	}

	public Term setLng(final float lng) {

		this.lng=lng;

		return this;
	}


	public Object getValue() {
		return value != null ? value

				: type.equals(XSDBoolean) ? (value=_boolean(text))

				: type.equals(XSDInteger) ? (value=_integer(text))
				: type.equals(XSDDecimal) ? (value=_decimal(text))

				: type.equals(XSDByte) ? (value=_byte(text))
				: type.equals(XSDShort) ? (value=_short(text))
				: type.equals(XSDInt) ? (value=_int(text))
				: type.equals(XSDLong) ? (value=_long(text))
				: type.equals(XSDFloat) ? (value=_float(text))
				: type.equals(XSDDouble) ? (value=_double(text))

				: type.equals(XSDDate) ? (value=_date(text))
				// !!! xsd:time
				: type.equals(XSDDateTime) ? (value=_dateTime(text))
				: type.equals(XSDGYear) ? (value=_year(text))
				// !!! xsd:gMonth/xsd:gDay

				: type.equals(XSDString) || type.equals(Simple) || type.startsWith(Tagged) ? (value=text)

				: null;
	}


	private Boolean _boolean(final String text) {
		return text == null ? null : text.equals("true") ? Boolean.TRUE : text.equals("false") ? Boolean.FALSE : null;
	}

	private BigInteger _integer(final String text) {
		try {
			return text == null ? null : new BigInteger(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private BigDecimal _decimal(final String text) {
		try {
			return text == null ? null : new BigDecimal(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Byte _byte(final String text) {
		try {
			return text == null ? null : Byte.valueOf(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Short _short(final String text) {
		try {
			return text == null ? null : Short.valueOf(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Integer _int(final String text) {
		try {
			return text == null ? null : Integer.valueOf(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Long _long(final String text) {
		try {
			return text == null ? null : Long.valueOf(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Float _float(final String text) {
		try {
			return text == null ? null : Float.valueOf(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	private Double _double(final String text) {
		try {
			return text == null ? null : Double.valueOf(text);
		} catch ( final NumberFormatException ignored ) {
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	// Date.UTC > Calendar not portable to GWT // !!! replace with client/server solution
	private Date _date(final String text) {

		try {

			return text == null ? null

					: text.matches("\\d{4}-\\d{2}-\\d{2}(Z|[-+]\\d{2}:\\d{2})?") ? new Date(Date.UTC( // !!! handle timezone
					parseInt(text.substring(0, 4))-1900,
					parseInt(text.substring(5, 7))-1,
					parseInt(text.substring(8, 10)),
					0,
					0,
					0))

					: null;

		} catch ( final IllegalArgumentException ignored ) {
			return null;
		}

	}

	@SuppressWarnings("deprecation")
	// Date.UTC > Calendar not portable to GWT // !!! replace with client/server solution
	private Date _dateTime(final String text) {

		try {

			return text == null ? null

					: text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z?") ? new Date(Date.UTC( // !!! timezone
					parseInt(text.substring(0, 4))-1900,
					parseInt(text.substring(5, 7))-1,
					parseInt(text.substring(8, 10)),
					parseInt(text.substring(11, 13)),
					parseInt(text.substring(14, 16)),
					parseInt(text.substring(17, 19))))

					: text.matches("\\d{4}-\\d{2}-\\d{2}") ? new Date(Date.UTC( // !!! timezone
					parseInt(text.substring(0, 4))-1900,
					parseInt(text.substring(5, 7))-1,
					parseInt(text.substring(8, 10)),
					0, 0, 0))

					: null;

		} catch ( final IllegalArgumentException ignored ) {
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	// Date.UTC > Calendar not portable to GWT // !!! replace with client/server solution
	private Date _year(final String text) {

		try {

			return text == null ? null

					// ;(virtuoso) xsd:gYear lexical includes constant month/day and timezone (e.g. as yyy-01-01+00:02)

					: text.matches("\\d{4}(-01-01)?(Z|[-+]\\d{2}:\\d{2})?") ? new Date(Date.UTC( // !!! handle timezone
					parseInt(text.substring(0, 4))-1900,
					0,
					1,
					0,
					0,
					0))

					: null;

		} catch ( final IllegalArgumentException ignored ) {
			return null;
		}

	}


	public Term reverse() {
		return (type.equals(Recto) ? typed(text, Verso) : type.equals(Verso) ? named(text) : this)
				.setLabel(label).setNotes(notes).setImage(image);
	}


	public String label() {
		return label(Catalog.Std);
	}

	public String label(final Catalog catalog) {

		if ( catalog == null ) {
			throw new NullPointerException("null prefixes");
		}

		return !label.isEmpty() ? label
				: getValue() instanceof String ? getValue().toString()
				: getValue() instanceof Boolean ? getValue().toString()
				: getValue() instanceof BigDecimal ? getValue().toString() // !!! format
				: getValue() instanceof BigInteger ? getValue().toString() // !!! format
				: getValue() instanceof Short ? getValue().toString()
				: getValue() instanceof Integer ? getValue().toString()
				: getValue() instanceof Long ? getValue().toString()
				: getValue() instanceof Float ? getValue().toString() // !!! precision/exponent
				: getValue() instanceof Double ? getValue().toString() // !!! precision/exponent
				: type.equals(XSDDate) ? text.matches("\\d{4}-\\d{2}-\\d{2}(Z|[-+]\\d{2}:\\d{2})?") ? text.substring(0, 10) : text // ignore timezone
				: type.equals(XSDTime) ? text.matches("\\d{2}:\\d{2}:\\d{2}(Z|[-+]\\d{2}:\\d{2})?") ? text.substring(0, 8) : text // ignore timezone
				: type.equals(XSDDateTime) ? text.matches("\\d{4}-\\d{2}-\\d{2}T00:00:00(Z|[-+]\\d{2}:\\d{2})?") ? text.substring(0, 10) : text // ignore timezone
				: type.equals(XSDGYear) ? text.matches("(\\d{4}(Z|[-+]\\d{2}:\\d{2})?)|(\\d{4}-01-01[-+]\\d{2}:\\d{2})") ? text.substring(0, 4) : text  // ignore timezone // ;(virtuoso) xsd:gYear lexicals always include constant day/month and timezoe offset
				: type.equals(XSDGMonth) ? text
				: type.equals(XSDGDay) ? text
				: text.startsWith("mailto:") ? text.substring(7)
				: text.startsWith("callto:") ? text.substring(7)
				: text.startsWith("tel:") ? text.substring(4)
				: type.equals(XSDAnyURI) ? text
				: equals(RDF.RDFType) ? "collection"
				: (type.equals(Recto) || type.equals(Verso)) && text.matches(NamedURI) ? label(text.replaceAll(NamedURI, "$3"))
				: isVerso() ? catalog.qualify(text) // no leading '^' when labelling
				: isTyped() ? text+" ["+(type.matches(NamedURI) ? label(type.replaceAll(NamedURI, "$3")) : catalog.qualify(type))+"]"
				: format(catalog);
	}

	private String label(final String name) { // de-hyphenate/camelize

		final int length=name.length();
		final boolean title=length > 0 && isUpperCase(name.charAt(0)); // use title case

		final StringBuilder label=new StringBuilder();

		for (int i=0; i < length; ++i) {

			final char c=name.charAt(i);

			if ( c == '%' && i+2 < length ) { // URL-encoded chars (must be portable across jdk/gwt)

				final byte[] bytes=new byte[(length-i)/3];

				int n=0;

				for (int j=i; j+2 < length && name.charAt(j) == '%'; j+=3, ++n) {
					try {
						bytes[n]=(byte)parseInt(name.substring(j+1, j+3), 16);
					} catch ( final NumberFormatException ignored ) {
						bytes[n]=0;
					}
				}

				try {
					label.append(new String(bytes, 0, n, "UTF-8"));
				} catch ( final UnsupportedEncodingException ignored ) {}

				i+=3*n-1;

			} else if ( c == '-' || c == '_' || c == '+' ) {

				label.append(' ');

			} else if ( isUpperCase(c) && i+1 < length && isLowerCase(name.charAt(i+1)) ) {

				if ( i > 0 ) { label.append(' '); }
				label.append(title ? c : toLowerCase(c));

			} else if ( isUpperCase(c) && i > 0 && !isUpperCase(name.charAt(i-1)) ) {

				label.append(' ');
				label.append(c);

			} else {

				label.append(c);

			}
		}

		return label.toString();
	}


	public String format() {
		return format(Catalog.Empty);
	}

	public String format(final Catalog catalog) {

		if ( catalog == null ) {
			throw new NullPointerException("null prefixes");
		}

		final Object value=getValue();

		return isBlank() ? "_:"+text
				: isRecto() ? catalog.qualify(text)
				: isVerso() ? '^'+catalog.qualify(text)
				: isTyped(XSDBoolean) ? value.toString()
				: isTyped(XSDInteger) ? value.toString()
				: isTyped(XSDDecimal) ? format((BigDecimal)value)
				: type.equals(XSDGYear) ? text.matches("(\\d{4}Z?)|(\\d{4}-01-01[-+]\\d{2}:\\d{2})") ? format(text.substring(0, 4))+"^^"+catalog.qualify(type) : text // ;(virtuoso) xsd:gYear lexicals always include constant day/month and timezone offset
				: isTyped() ? format(text)+"^^"+catalog.qualify(type)
				: isTagged() ? format(text)+'@'+getLang()
				: format(text);
	}

	private String format(final BigDecimal value) { // force fractional part
		return (value.scale() <= 0 ? value.setScale(1, RoundingMode.UNNECESSARY) : value).toPlainString();
	}

	private String format(final CharSequence text) {

		final StringBuilder builder=new StringBuilder(text.length()+text.length()/10);

		builder.append('\'');

		for (int i=0, n=text.length(); i < n; ++i) {
			switch ( text.charAt(i) ) {
				case '\\':
					builder.append("\\\\");
					break;
				case '\'':
					builder.append("\\'");
					break;
				case '\"':
					builder.append("\\\"");
					break;
				case '\r':
					builder.append("\\r");
					break;
				case '\n':
					builder.append("\\n");
					break;
				case '\t':
					builder.append("\\t");
					break;
				default:
					builder.append(text.charAt(i));
					break;
			}
		}

		builder.append('\'');

		return builder.toString();
	}


	@Override public boolean equals(final Object object) {
		return object == this || object instanceof Term
				&& text.equals(((Term)object).text) && type.equals(((Term)object).type);
	}

	@Override public int hashCode() {
		return text.hashCode()^type.hashCode();
	}

	@Override public String toString() {
		return format(Catalog.Std);
	}

}
