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

package com.metreeca.self.shared.csv;

import java.util.Collection;
import java.util.List;


public final class CSVWriter {

	private char separator=',';
	private char delimiter='"';


	public char getSeparator() {
		return separator;
	}

	public CSVWriter setSeparator(final char separator) {

		this.separator=separator;

		return this;
	}


	public char getDelimiter() {
		return delimiter;
	}

	public CSVWriter setDelimiter(final char delimiter) {

		this.delimiter=delimiter;

		return this;
	}


	public String write(final Table table) {

		if ( table == null ) {
			throw new NullPointerException("null table");
		}

		final Builder builder=new Builder();

		builder.values(table.labels());

		for (final List<String> record : table.records()) {
			builder.values(record);
		}

		return builder.toString();
	}


	public final class Builder {

		private final StringBuilder text=new StringBuilder();


		public Builder values(final Collection<String> values) {

			if ( values != null && !values.isEmpty() ) {

				int col=0;

				for (final String label : values) {

					if ( col++ > 0 ) {
						text.append(separator);
					}

					value(label);
				}

				text.append('\n');
			}

			return this;
		}

		private Builder value(final CharSequence value) {

			final int start=text.length();

			boolean quoted=false;

			if ( value != null ) {
				for (int i=0, n=value.length(); i < n; ++i) {

					final char c=value.charAt(i);

					if ( c == delimiter ) {
						text.append(delimiter).append(delimiter);
						quoted=true;
					} else if ( c == separator || c == '\n' || c == '\r' ) {
						text.append(c);
						quoted=true;
					} else {
						text.append(c);
					}
				}
			}

			if ( quoted ) {
				text.insert(start, delimiter).append(delimiter);
			}

			return this;
		}


		@Override public String toString() {
			return text.toString();
		}
	}
}
